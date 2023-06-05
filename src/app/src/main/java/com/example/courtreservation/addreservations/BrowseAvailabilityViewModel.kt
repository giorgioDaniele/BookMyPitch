package com.example.courtreservation.addreservations

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.courtreservation.Reservation
import com.example.courtreservation.service.FirebaseMessages
import com.example.courtreservation.show_edit_reservations.documents.PlaygroundDocument.Companion.toPlaygroundDocument
import com.example.courtreservation.show_edit_reservations.documents.ReservationDocument.Companion.toReservationDocument
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

class BrowseAvailabilityViewModel(
    private val playgroundId: String,
    private val sport: String,
    private val level: String,
    private val playgroundName: String,
    private val nickname: String
): ViewModel() {
    class Factory(private val nickname: String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val state = extras.createSavedStateHandle()

            return BrowseAvailabilityViewModel(
                state["playgroundId"]!!,
                state["sport"]!!,
                state["level"]!!,
                state["playgroundName"]!!,
                nickname
            ) as T
        }
    }

    enum class ProgressDialogState {
        IDLE, LOADING, SUCCESS, FAILURE
    }

    private val db = Firebase.firestore

    private val _currentDate = MutableLiveData(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    val currentDate: LiveData<LocalDate> = _currentDate
    fun setCurrentDate(date: LocalDate) {
        _currentDate.value = date
    }

    val occupiedSlotsByDate = currentDate
        .switchMap { date ->
            db.collection("reservations")
                .whereEqualTo("playgroundId", playgroundId)
                .whereEqualTo("year", date.year)
                .whereEqualTo("month", date.monthNumber - 1) // db's month is zero-based
                .whereEqualTo("day", date.dayOfMonth)
                .snapshots()
                .asLiveData()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { it.toObject(Reservation::class.java)?.hour }
                }
        }

    val dialogState = MutableLiveData(ProgressDialogState.IDLE)
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addReservation(hour: Int, customRequest: String?) {
        val date = currentDate.value

        if (date != null) {
            val reservation = Reservation(
                date.dayOfMonth,
                date.monthNumber - 1, // db's month is zero-based
                date.year,
                hour,
                playgroundId,
                customRequest,
                "active",
                listOf(nickname),
                emptyList()
            )

            val task = db.collection("reservations").add(reservation)
            dialogState.value = ProgressDialogState.LOADING
            delay(1000)
            try {
                task.await()
                dialogState.value = ProgressDialogState.SUCCESS
            } catch (e: Exception) {
                dialogState.value = ProgressDialogState.FAILURE
            }
            delay(2000)
            dialogState.value = ProgressDialogState.IDLE

            // FIREBASE MESSAGES
            /*
            val sport = db.
                collection("playgrounds").
                get().
                await().
                documents
                .mapNotNull { it.toPlaygroundDocument() }
                .filter { it.id == playgroundId }
                .map { Pair(it.name, it.sport) }.first()

            val  sportsMap : List<Map<String, Any>> = db
                .collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .await().documents.first()
                .data
                ?.get("sports") as List<Map<String, Any>>

            val  sportLevel = sportsMap.first {
                it.getValue("name") == sport.second
            }["level"]


            Log.d("NOTIFICATION", sport.toString())
            Log.d("NOTIFICATION", sportLevel.toString())
            */

            val notification = mapOf(
                "who" to nickname,
                "when" to date.let {
                    it.dayOfMonth.toString() + " " + (it.month).toString().lowercase()
                        .replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else it.toString() } + " " + it.year.toString()
                },
                "sport" to sport,
                "where" to playgroundName,
                "level" to level,
            )
            Log.d("NOTIFICATION", notification.toString())
            FirebaseMessages.sendMessage(notification as Map<String, Any>, "reservations")
        }
    }
}