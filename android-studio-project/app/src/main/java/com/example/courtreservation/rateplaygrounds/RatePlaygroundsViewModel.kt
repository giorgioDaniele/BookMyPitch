package com.example.courtreservation.rateplaygrounds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.courtreservation.Playground
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.max

// Represents the individual rating of a single user for a specific playground
data class PlaygroundRating(
    val rating: Int = 0
)

data class PlaygroundComment(
    val comment: String = "",
    val author_nickname: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

class RatePlaygroundsViewModel(
    private val nickname: String
): ViewModel() {
    class Factory(private val nickname: String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            return RatePlaygroundsViewModel(
                nickname
            ) as T
        }
    }

    private val db = Firebase.firestore

    private val _playgroundsBySport = MutableLiveData<Map<String, List<Pair<String, Playground>>>>()
    private val _personalRatingsByPlayground = MutableLiveData<MutableMap<String, Int?>>()

    private val ratingsCallbacks: MutableMap<String, ListenerRegistration> = mutableMapOf()
    private val playgroundsCallback = db.collection("playgrounds")
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                _playgroundsBySport.value = snapshot.documents
                    .mapNotNull { document ->
                        val playgroundId = document.id

                        if (ratingsCallbacks[playgroundId] == null) {
                            ratingsCallbacks[playgroundId] = db.collection("playgrounds")
                                .document(playgroundId)
                                .collection("ratings")
                                .document(nickname)
                                .addSnapshotListener { snapshotInner, errInner ->
                                    if (errInner == null && snapshotInner != null) {
                                        _personalRatingsByPlayground.value = _personalRatingsByPlayground.value.let {
                                            val v = it ?: mutableMapOf()
                                            v[playgroundId] = snapshotInner.toObject(PlaygroundRating::class.java)?.rating
                                            v
                                        }
                                    }
                                }
                        }

                        Pair(playgroundId, document.toObject(Playground::class.java) ?: return@mapNotNull null)
                    }
                    .groupBy { it.second.sport }
            }
        }

    private val _combined = MediatorLiveData<Pair<Map<String, List<Pair<String, Playground>>>?, MutableMap<String, Int?>?>>().apply {
        addSource(_playgroundsBySport) {
            value = Pair(it, value?.second)
        }
        addSource(_personalRatingsByPlayground) {
            value = Pair(value?.first, it)
        }
    }

    val playgroundsBySportWithPersonalRatings: LiveData<Map<String, List<Triple<String, Playground, Int?>>>> = _combined
        .map { (playgrounds, ratings) ->
            playgrounds?.mapValues { entry ->
                entry.value.map { p ->
                    Triple(
                        p.first,
                        p.second,
                        ratings?.get(p.first)
                    )
                }
            } ?: emptyMap()
        }

    fun setRatingForPlayground(playgroundId: String, rating: Int) {
        val playgroundDocumentRef = db
            .collection("playgrounds")
            .document(playgroundId)
        val personalRatingDocumentRef = playgroundDocumentRef
            .collection("ratings")
            .document(nickname)

        db.runTransaction { transaction ->
            val previousPersonalRating = transaction.get(personalRatingDocumentRef)
                .getLong("rating")
            val previousAvgRating = transaction.get(playgroundDocumentRef)
                .getDouble("avg_rating") ?: 0.0
            val previousNumRatings = transaction.get(playgroundDocumentRef)
                .getLong("num_ratings") ?: 0

            transaction.set(personalRatingDocumentRef, mapOf("rating" to rating), SetOptions.merge())
            if (previousPersonalRating == null) {
                transaction.update(playgroundDocumentRef, "num_ratings", previousNumRatings + 1)
                transaction.update(
                    playgroundDocumentRef,
                    "avg_rating",
                    (previousAvgRating*previousNumRatings.toDouble() + rating.toDouble()) / (previousNumRatings + 1).toDouble()
                )
            } else {
                val avgRatingWithoutUser = (previousAvgRating*previousNumRatings.toDouble() - previousPersonalRating.toDouble()) /
                        max(1, (previousNumRatings - 1)).toDouble()
                transaction.update(
                    playgroundDocumentRef,
                    "avg_rating",
                    (avgRatingWithoutUser*(previousNumRatings - 1).toDouble() + rating.toDouble()) / previousNumRatings.toDouble()
                )
            }
        }
    }

    private val _playgroundComments = MutableLiveData<List<PlaygroundComment>?>(null)
    val playgroundComments: LiveData<List<PlaygroundComment>?> = _playgroundComments
    private var commentsCallback: ListenerRegistration? = null

    fun startListeningToPlaygroundComments(playgroundId: String) {
        _playgroundComments.value = null

        commentsCallback?.remove()
        commentsCallback = db.collection("playgrounds")
            .document(playgroundId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, err ->
                if (err == null && snapshot != null) {
                    _playgroundComments.value = snapshot.documents.mapNotNull {
                        it.toObject(PlaygroundComment::class.java)
                    }
                }
            }
    }

    fun addCommentToPlayground(playgroundId: String, comment: String) {
        val playgroundDocumentRef = db
            .collection("playgrounds")
            .document(playgroundId)

        playgroundDocumentRef
            .collection("comments")
            .add(PlaygroundComment(comment = comment, author_nickname = nickname))

        playgroundDocumentRef
            .update("num_comments", FieldValue.increment(1))
    }

    override fun onCleared() {
        super.onCleared()
        playgroundsCallback.remove()
        ratingsCallbacks.values.forEach { it.remove() }
        commentsCallback?.remove()
    }
}