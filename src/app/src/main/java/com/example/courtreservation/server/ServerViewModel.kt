package com.example.courtreservation.server

import android.icu.util.Calendar
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.courtreservation.Playground
import com.example.courtreservation.Reservation
import com.example.courtreservation.Sport
import com.example.courtreservation.User
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.example.courtreservation.model.remote.FirestoreManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val profileSharedPreferencesManager: SharedPreferencesManager,
    private val repository: FirestoreManager,
    ) : ViewModel() {
    //val user = mutableStateOf<User?>(null)
    private val db = Firebase.firestore
    val reservationOne = mutableStateOf(Reservation())
    val playgroundOne = mutableStateOf(Playground())
    val completed = mutableStateOf(emptyList<Reservation>())
    @Synchronized
    fun addCompleted(reservation: Reservation){
        if(completed.value.isEmpty()){
            reservationOne.value=reservation
        }
        completed.value=completed.value.plus(reservation)
    }
    @Synchronized
    fun removeFirstCompleted(){
        if(completed.value.lastIndex==0) {
            completed.value = emptyList()
        } else{
            reservationOne.value=completed.value[1]
            completed.value=completed.value.subList(1,completed.value.lastIndex)
        }
    }
    val playgrounds = mutableStateOf(emptyList<Playground>())
    @Synchronized
    fun addPlayground(playground: Playground) {
        if(playgrounds.value.isEmpty()){
            playgroundOne.value=playground
        }
        playgrounds.value=playgrounds.value.plus(playground)
    }
    @Synchronized
    fun removeFirstPlaygrounds(){
        if(playgrounds.value.lastIndex==0) {
            playgrounds.value= emptyList()
        } else{
            playgroundOne.value=playgrounds.value[1]
            playgrounds.value=playgrounds.value.subList(1,playgrounds.value.lastIndex)
        }
    }
    fun updateReservations() {
        //completed.value = emptyList()
        //playgrounds.value = emptyList()
        db.collection("reservations")
            .whereArrayContains(/*listOf("team_0","team_1")*/"team_0", profileSharedPreferencesManager.getNickname())
            .get()
            .addOnSuccessListener {
                it.forEach { doc ->
                    /*if (doc.get("status").toString() == "confirmed" &&
                        Calendar.getInstance().apply {
                            this.set(Calendar.YEAR, Integer.parseInt(doc.get("year").toString()))
                            this.set(Calendar.MONTH, Integer.parseInt(doc.get("month").toString()))
                            this.set(
                                Calendar.DAY_OF_MONTH,
                                Integer.parseInt(doc.get("day").toString())
                            )
                            this.set(
                                Calendar.HOUR_OF_DAY,
                                Integer.parseInt(doc.get("hour").toString())
                            )
                        }.timeInMillis > Calendar.getInstance().timeInMillis
                    ) {
                        db.collection("reservations").document(doc.id)
                            .update(
                                mapOf("status" to "closed")
                            )
                    } // se era confermata ed è passata diventa closed
                    else*/
                    if (
                        doc.get("status").toString() == "active" &&
                        Calendar.getInstance().apply {
                            this.set(Calendar.YEAR, Integer.parseInt(doc.get("year").toString()))
                            this.set(Calendar.MONTH, Integer.parseInt(doc.get("month").toString()))
                            this.set(
                                Calendar.DAY_OF_MONTH,
                                Integer.parseInt(doc.get("day").toString())
                            )
                            this.set(
                                Calendar.HOUR_OF_DAY,
                                Integer.parseInt(doc.get("hour").toString())
                            )
                        }.timeInMillis < Calendar.getInstance().timeInMillis
                    ) {
                        addCompleted(doc.toObject(Reservation::class.java).copy(id=doc.id))
                        db.collection("playgrounds").document(doc.toObject(Reservation::class.java).playgroundId)
                            .get().addOnSuccessListener {playground->
                                if(playground!=null){
                                    addPlayground(playground.toObject(Playground::class.java)!!)
                                }
                            }.addOnFailureListener{
                            }
                    }
                }
            }
        db.collection("reservations")
            .whereArrayContains(/*listOf("team_0","team_1")*/"team_1", profileSharedPreferencesManager.getNickname())
            .get()
            .addOnSuccessListener {
                it.forEach { doc ->
                    /*if (doc.get("status").toString() == "confirmed" &&
                        Calendar.getInstance().apply {
                            this.set(Calendar.YEAR, Integer.parseInt(doc.get("year").toString()))
                            this.set(Calendar.MONTH, Integer.parseInt(doc.get("month").toString()))
                            this.set(
                                Calendar.DAY_OF_MONTH,
                                Integer.parseInt(doc.get("day").toString())
                            )
                            this.set(
                                Calendar.HOUR_OF_DAY,
                                Integer.parseInt(doc.get("hour").toString())
                            )
                        }.timeInMillis > Calendar.getInstance().timeInMillis
                    ) {
                        db.collection("reservations").document(doc.id)
                            .update(
                                mapOf("status" to "closed")
                            )
                    } // se era confermata ed è passata diventa closed
                    else */
                    if (
                        doc.get("status").toString() == "active" &&
                        Calendar.getInstance().apply {
                            this.set(Calendar.YEAR, Integer.parseInt(doc.get("year").toString()))
                            this.set(Calendar.MONTH, Integer.parseInt(doc.get("month").toString()))
                            this.set(
                                Calendar.DAY_OF_MONTH,
                                Integer.parseInt(doc.get("day").toString())
                            )
                            this.set(
                                Calendar.HOUR_OF_DAY,
                                Integer.parseInt(doc.get("hour").toString())
                            )
                        }.timeInMillis < Calendar.getInstance().timeInMillis
                    ) {
                        addCompleted(doc.toObject(Reservation::class.java).copy(id=doc.id))
                        db.collection("playgrounds").document(doc.toObject(Reservation::class.java).playgroundId)
                            .get().addOnSuccessListener {playground->
                                if(playground!=null){
                                    addPlayground(playground.toObject(Playground::class.java)!!)
                                }
                            }.addOnFailureListener{
                            }
                    }
                }
            }
    }
    fun setWinner(team: Int) {
        if(team==0) {
            val sport=playgrounds.value[0].sport
            val reservation=completed.value[0]
            reservation.team_0.forEach { nickname->
                db.collection("users").document(nickname).get().addOnSuccessListener {user->
                    val eSports = mutableListOf<Sport>()
                    var totVictories = 0f
                    var totDefeats = 0f
                    var totLikes = 0f
                    var totDislikes = 0f
                    user.toObject(User::class.java)?.sports?.map {
                        if(it.name == sport){
                            eSports.add(it.copy(victories = it.victories+1, meets = it.meets+1))
                            totVictories += it.victories+1
                            totDefeats +=it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        } else {
                            eSports.add(it)
                            totVictories += it.victories
                            totDefeats +=it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        }
                    }
                    val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
                    db.collection("users").document(nickname).update(
                        mapOf(
                            "sports" to eSports,
                            "rating" to rating
                        )
                    )
                }.addOnFailureListener{
                }
            }
            reservation.team_1.forEach { nickname->
                db.collection("users").document(nickname).get().addOnSuccessListener {user->
                    val eSports = mutableListOf<Sport>()
                    var totVictories = 0f
                    var totDefeats = 0f
                    var totLikes = 0f
                    var totDislikes = 0f
                    user.toObject(User::class.java)?.sports?.map {
                        if(it.name == sport){
                            eSports.add(it.copy(defeats = it.defeats+1, meets = it.meets+1))
                            totVictories += it.victories
                            totDefeats += (it.defeats+1)
                            totLikes += it.like
                            totDislikes += it.dislike
                        } else {
                            eSports.add(it)
                            totVictories += it.victories
                            totDefeats += it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        }
                    }
                    val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
                    db.collection("users").document(nickname).update(
                        mapOf(
                            "sports" to eSports,
                            "rating" to rating
                        )
                    )
                }.addOnFailureListener{
                }
            }
            db.collection("reservations").document(reservation.id!!).update(
                mapOf(
                    "result" to 0,
                    "status" to "completed"
                )
            )
            removeFirstPlaygrounds()
            removeFirstCompleted()
        } else {
            val sport=playgrounds.value[0].sport
            val reservation=completed.value[0]

            reservation.team_1.forEach { nickname->
                db.collection("users").document(nickname).get().addOnSuccessListener {user->
                    val eSports = mutableListOf<Sport>()
                    var totVictories = 0f
                    var totDefeats = 0f
                    var totLikes = 0f
                    var totDislikes = 0f
                    user.toObject(User::class.java)?.sports?.map {
                        if(it.name == sport){
                            eSports.add(it.copy(victories = it.victories+1, meets = it.meets+1))
                            totVictories += (it.victories+1)
                            totDefeats += it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        } else {
                            eSports.add(it)
                            totVictories += it.victories
                            totDefeats += it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        }
                    }
                    val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
                    db.collection("users").document(nickname).update(
                        mapOf(
                            "sports" to eSports,
                            "rating" to rating
                        )
                    )
                }
            }
            reservation.team_0.forEach { nickname->
                db.collection("users").document(nickname).get().addOnSuccessListener {user->
                    val eSports = mutableListOf<Sport>()
                    var totVictories = 0f
                    var totDefeats = 0f
                    var totLikes = 0f
                    var totDislikes = 0f
                    user.toObject(User::class.java)?.sports?.map {
                        if(it.name == sport){
                            eSports.add(it.copy(defeats = it.defeats+1, meets = it.meets+1))
                            totVictories += it.victories
                            totDefeats +=(it.defeats+1)
                            totLikes += it.like
                            totDislikes += it.dislike
                        } else {
                            eSports.add(it)
                            totVictories += it.victories
                            totDefeats +=it.defeats
                            totLikes += it.like
                            totDislikes += it.dislike
                        }
                    }
                    val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
                    db.collection("users").document(nickname).update(
                        mapOf(
                            "sports" to eSports,
                            "rating" to rating
                        )
                    )
                }
            }
            db.collection("reservations").document(completed.value[0].id!!).update(
                mapOf(
                    "result" to 1,
                    "status" to "completed"
                )
            )
            removeFirstPlaygrounds()
            removeFirstCompleted()
        }
    }
}
