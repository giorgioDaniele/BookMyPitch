package com.example.courtreservation.addreservations

import android.icu.util.Calendar
import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.courtreservation.Playground
import com.example.courtreservation.Reservation
import com.example.courtreservation.User
import com.example.courtreservation.utils.Converters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class PlaygroundSelectorViewModel(
    private val nickname: String
): ViewModel() {
    class Factory(private val nickname: String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            return PlaygroundSelectorViewModel(nickname) as T
        }
    }

    private val db = Firebase.firestore

    private val _users = MutableLiveData<Map<String, User>>()
    private val usersCallback = db.collection("users")
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                _users.value = snapshot.associate { it.id to it.toObject(User::class.java) }
            }
        }

    private val _playgrounds = MutableLiveData<List<Pair<String, Playground>>>()
    private val playgroundsCallback = db.collection("playgrounds")
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                _playgrounds.value = snapshot.documents
                    .map { Pair(it.id, it.toObject(Playground::class.java)!!) }
            }
        }

    private val _reservations = MutableLiveData<List<Pair<String, Reservation>>>()
    private val reservationsCallback = db.collection("reservations")
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                _reservations.value = snapshot.documents
                    .map { Pair(it.id, it.toObject(Reservation::class.java)!!) }
            }
        }

    private val _combined = MediatorLiveData<Triple<Map<String, User>?, List<Pair<String, Playground>>?, List<Pair<String, Reservation>>?>>(Triple(null, null, null)).apply {
        addSource(_users) {
            value = Triple(it, value?.second, value?.third)
        }
        addSource(_playgrounds) {
            value = Triple(value?.first, it, value?.third)
        }
        addSource(_reservations) {
            value = Triple(value?.first, value?.second, it)
        }
    }

    private data class JoinableReservationAnimationData(
        val transitionState: MutableTransitionState<Boolean> = MutableTransitionState(false).apply { targetState = true },
        var teamField: String? = null
    )
    private val joinableReservationsAnimationState: MutableMap<String, JoinableReservationAnimationData> = mutableMapOf()

    val playgroundsByActiveSport: LiveData<Map<String, Pair<List<Pair<String, Playground>>, String>>> = _combined
        .map { (users, playgrounds, _) ->
            if (users != null && playgrounds != null) {
                val activeSports = (users[nickname] ?: return@map emptyMap())
                    .sports
                    .filter { it.active && it.visible }
                    .map { Pair(it.name, it.level) }

                playgrounds
                    .mapNotNull { (playgroundId, playground) ->
                        val level = activeSports.find {
                            it.first.equals(playground.sport, true)
                        }?.second ?: return@mapNotNull null

                        Triple(playgroundId, playground, level)
                    }
                    .groupBy { it.second.sport }
                    .mapValues { entry ->
                        entry.value.map {
                            it.first to it.second
                        } to entry.value.first().third
                    }
            } else {
                emptyMap()
            }
        }

    val joinableReservations: LiveData<List<Triple<Triple<String, Reservation, MutableTransitionState<Boolean>>, Playground, User>>> = _combined
        .map { (users, playgrounds, reservations) ->
            if (users != null && playgrounds != null && reservations != null) {
                val user = (users[nickname] ?: return@map listOf())
                val activeSports = user
                    .sports
                    .filter { it.active && it.visible }
                    .map { Pair(it.name, it.level) }
                val now = Calendar.getInstance().timeInMillis

                reservations
                    .mapNotNull { reservation ->
                        Triple(
                            reservation,
                            (playgrounds.find { it.first == reservation.second.playgroundId } ?: return@mapNotNull null).second,
                            users[reservation.second.team_0[0]] ?: return@mapNotNull null
                        )
                    }
                    .filter { (res, playground, owner) ->
                        // Check sport is active for current user
                        activeSports
                            .map { it.first }
                            .any { it.equals(playground.sport, true) } &&

                        // Check level of the owner is compatible with that of the current user for this sport
                        (owner.sports.find {
                            it.name.equals(playground.sport, true)
                        } ?: return@filter false).level == (user.sports.find {
                            it.name.equals(playground.sport, true)
                        } ?: return@filter false).level &&

                        // Check that the teams are not complete
                        (res.second.team_0.size + res.second.team_1.size) < playground.num_players_per_team &&

                        // Check that the user is not already part of any of the teams
                        !(res.second.team_0 + res.second.team_1).contains(nickname) &&

                        // Check that the reservation is in the future
                        Converters.createDate(res.second.year, res.second.month, res.second.day, res.second.hour).timeInMillis > now
                    }
                    .map { (res, playground, owner) ->
                        val transitionState = joinableReservationsAnimationState.getOrPut(res.first) {
                            JoinableReservationAnimationData()
                        }.transitionState

                        Triple(Triple(res.first, res.second, transitionState), playground, owner)
                    }
                    .sortedBy { (res, _, _) ->
                        Converters.createDate(res.second.year, res.second.month, res.second.day, res.second.hour).time
                    }
            } else {
                listOf()
            }
        }

    fun registerAddSelfToReservationTeam(
        reservationId: String,
        teamField: String
    ) {
        joinableReservationsAnimationState[reservationId]?.let {
            it.teamField = teamField
            it.transitionState.targetState = false
        }
    }

    suspend fun commitAddSelfToReservationTeam(
        reservationId: String
    ) {
        joinableReservationsAnimationState.remove(reservationId)?.let {
            try {
                db.collection("reservations")
                    .document(reservationId)
                    .update(it.teamField!!, FieldValue.arrayUnion(nickname))
                    .await()
                dialogState.value = BrowseAvailabilityViewModel.ProgressDialogState.SUCCESS
            } catch (e: Exception) {
                dialogState.value = BrowseAvailabilityViewModel.ProgressDialogState.FAILURE
            }
            delay(2000)
            dialogState.value = BrowseAvailabilityViewModel.ProgressDialogState.IDLE
        }
    }

    val dialogState = MutableLiveData(BrowseAvailabilityViewModel.ProgressDialogState.IDLE)

    override fun onCleared() {
        super.onCleared()
        usersCallback.remove()
        playgroundsCallback.remove()
        reservationsCallback.remove()
    }
}