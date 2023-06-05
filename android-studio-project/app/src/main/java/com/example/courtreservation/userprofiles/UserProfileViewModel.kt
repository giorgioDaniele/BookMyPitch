package com.example.courtreservation.userprofiles

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.courtreservation.Like
import com.example.courtreservation.Sport
import com.example.courtreservation.User
import com.example.courtreservation.UserBasicInfo
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val profileSharedPreferencesManager: SharedPreferencesManager,
    //private val repository: FirestoreManager,
    //private val storage: StorageManager
    ) : ViewModel() {
    private val db = Firebase.firestore

    val users = mutableStateOf(emptyList<UserBasicInfo>())
    @Synchronized
    fun setUsers(usersList: List<UserBasicInfo>){
        users.value=usersList.distinct().filter { it.nickname!=actualUser.value!!.nickname }
    }

    val textInput = mutableStateOf("")
    val user = mutableStateOf<User?>(null)
    val actualUser = mutableStateOf<User?>(null)
    private val userReg = db.collection("users").document(getNickname())
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                actualUser.value = snapshot.toObject(User::class.java)
            }
        }
    fun searchUsers(): Boolean {
        if(textInput.value.isNotEmpty()){
            return try {
                users.value = emptyList()
                db.collection("users") // search first by nickname
                    .whereGreaterThanOrEqualTo("nickname", "@${textInput.value}")
                    .whereLessThanOrEqualTo("nickname", "@${textInput.value}\uF7FF")
                    .get()
                    .addOnCompleteListener { documents ->
                        for (document in documents.result) {
                            setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))
                            //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("ciao:(", "Error getting documents: ", exception)
                    }
                db.collection("users")// finally by name
                    .whereGreaterThanOrEqualTo("name", textInput.value.replaceFirstChar { it.uppercase() })
                    .whereLessThanOrEqualTo("name", "${textInput.value.replaceFirstChar { it.uppercase() }}\uF7FF")
                    .get()
                    .addOnCompleteListener { documents ->
                        for (document in documents.result) {
                            setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                            //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("ciao:(", "Error getting documents: ", exception)
                    }

                db.collection("users") //then by surname
                    .whereGreaterThanOrEqualTo("surname", textInput.value.replaceFirstChar { it.uppercase() })
                    .whereLessThanOrEqualTo("surname", "${textInput.value.replaceFirstChar { it.uppercase() }}\uF7FF")
                    .get()
                    .addOnCompleteListener { documents ->
                        for (document in documents.result) {
                            setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                            //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("ciao:(", "Error getting documents: ", exception)
                    }
                ////////
                if(textInput.value.split(" ")[0].isNotEmpty()) {

                    db.collection("users")// finally by name
                        .whereGreaterThanOrEqualTo(
                            "name",
                            textInput.value.split(" ")[0].replaceFirstChar { it.uppercase() })
                        .whereLessThanOrEqualTo(
                            "name",
                            "${textInput.value.split(" ")[0].replaceFirstChar { it.uppercase() }}\uF7FF"
                        )
                        .get()
                        .addOnCompleteListener { documents ->
                            for (document in documents.result) {
                                setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                                //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("ciao:(", "Error getting documents: ", exception)
                        }

                    db.collection("users") //then by surname
                        .whereGreaterThanOrEqualTo(
                            "surname",
                            textInput.value.split(" ")[0].replaceFirstChar { it.uppercase() })
                        .whereLessThanOrEqualTo(
                            "surname",
                            "${textInput.value.split(" ")[0].replaceFirstChar { it.uppercase() }}\uF7FF"
                        )
                        .get()
                        .addOnCompleteListener { documents ->
                            for (document in documents.result) {
                                setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                                //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("ciao:(", "Error getting documents: ", exception)
                        }
                }
                ///////////
                if(textInput.value.split(" ")[1].isNotEmpty()){
                    db.collection("users")// finally by name
                        .whereGreaterThanOrEqualTo("name", textInput.value.split(" ")[1].replaceFirstChar { it.uppercase() })
                        .whereLessThanOrEqualTo("name", "${textInput.value.split(" ")[1].replaceFirstChar { it.uppercase() }}\uF7FF")
                        .get()
                        .addOnCompleteListener { documents ->
                            for (document in documents.result) {
                                setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                                //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("ciao:(", "Error getting documents: ", exception)
                        }
                    db.collection("users") //then by surname
                        .whereGreaterThanOrEqualTo("surname", textInput.value.split(" ")[1].replaceFirstChar { it.uppercase() })
                        .whereLessThanOrEqualTo("surname", "${textInput.value.split(" ")[1].replaceFirstChar { it.uppercase() }}\uF7FF")
                        .get()
                        .addOnCompleteListener { documents ->
                            for (document in documents.result) {
                                setUsers(users.value.plus(document.toObject(UserBasicInfo::class.java)))

                                //users.value = users.value.plus(document.toObject(UserBasicInfo::class.java))
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("ciao:(", "Error getting documents: ", exception)
                        }
                }

                return true
            } catch (e: Exception){
                false
            }
        }
        return false
    }
    /*private val userReg = db.collection("users").document("@$nickname.value")
        .addSnapshotListener { snapshot, err ->
            Log.d("UserProfileViewModel", nickname.value)
            if (err == null && snapshot != null) {
                user.value = snapshot.toObject(User::class.java)
            }
        }*/
    /*fun getUserNickname(): String {
        return "@aiden07"
        //return profileSharedPreferencesManager.getUserNickname()
    }
    fun setUserNickname(nickname: String){
        profileSharedPreferencesManager.setUserNickname(nickname)
    }*/
    suspend fun getUserByUsername(username: String): Boolean{
        return try {
            user.value = db.collection("users").document(username).get().await()
                .toObject(User::class.java)
            true
        } catch (e: Exception){
            false
        }
    }
    fun removeFriend(nickname: String) {
        db.collection("users").document(getNickname()).update(
            mapOf("friends" to actualUser.value!!.friends!!.filter { it.nickname != nickname }))
    }
    fun addFriend(friend: UserBasicInfo) {
        db.collection("users").document(getNickname()).update(
            if(actualUser.value!!.friends==null){
                mapOf("friends" to (listOf(friend)))
            } else
                mapOf("friends" to (actualUser.value!!.friends?.plus(friend)))
        )
    }
    private fun getNickname(): String {
        return profileSharedPreferencesManager.getNickname()
    }

    suspend fun getLike(name: String): Like? {
        return db.collection("likes").document("${user.value!!.nickname}${actualUser.value!!.nickname}$name").get().await()?.toObject(Like::class.java)
    }
    fun setLike(name: String, deleteDislike: Boolean=false){
        val eSports= mutableListOf<Sport>()
        var totVictories = 0f
        var totDefeats = 0f
        var totLikes = 0f
        var totDislikes = 0f
        db.collection("likes").document("${user.value!!.nickname}${actualUser.value!!.nickname}$name").set(Like("${user.value!!.nickname}${actualUser.value!!.nickname}$name",1))
        user.value!!.sports.forEach {
            if(it.name==name){
                if(deleteDislike){
                    eSports.add(it.copy(dislike = it.dislike-1, like = it.like+1))
                    totVictories += it.victories
                    totDefeats +=it.defeats
                    totLikes += (it.like+1)
                    totDislikes += (it.dislike-1)
                } else {
                    eSports.add(it.copy(like = it.like+1))
                    totVictories += it.victories
                    totDefeats +=it.defeats
                    totLikes += (it.like+1)
                    totDislikes += it.dislike
                }
            }else{
                eSports.add(it)
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += it.like
                totDislikes += it.dislike
            }
        }
        val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
        Log.d("setLike", rating.toString())
            db.collection("users").document(user.value!!.nickname).update(
            mapOf(
                "sports" to eSports,
                "rating" to rating
            )
        )
        user.value=user.value!!.copy(sports = eSports, rating = rating)
    }
    fun setDislike(name: String, deleteLike: Boolean=false) {
        val eSports = mutableListOf<Sport>()
        var totVictories = 0f
        var totDefeats = 0f
        var totLikes = 0f
        var totDislikes = 0f
        db.collection("likes").document("${user.value!!.nickname}${actualUser.value!!.nickname}$name").set(Like("${user.value!!.nickname}${actualUser.value!!.nickname}$name",-1))
        user.value!!.sports.forEach {
            if(it.name==name){
                if(deleteLike){
                    eSports.add(it.copy(like = it.like-1, dislike = it.dislike+1))
                    totVictories += it.victories
                    totDefeats +=it.defeats
                    totLikes += (it.like-1)
                    totDislikes += (it.dislike+1)
                }else{
                    eSports.add(it.copy(dislike = it.dislike+1))
                    totVictories += it.victories
                    totDefeats +=it.defeats
                    totLikes += it.like
                    totDislikes += (it.dislike+1)
                }
            }else{
                eSports.add(it)
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += it.like
                totDislikes += it.dislike
            }
        }
        val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
        db.collection("users").document(user.value!!.nickname).update(
            mapOf(
                "sports" to eSports,
                "rating" to rating
            )
        )
        user.value=user.value!!.copy(sports = eSports, rating = rating)

    }
    fun deleteLike(name: String) {
        val eSports = mutableListOf<Sport>()
        var totVictories = 0f
        var totDefeats = 0f
        var totLikes = 0f
        var totDislikes = 0f
        db.collection("likes").document("${user.value!!.nickname}${actualUser.value!!.nickname}$name").delete()
        user.value!!.sports.forEach {
            if(it.name==name){
                eSports.add(it.copy(like = it.like-1))
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += (it.like-1)
                totDislikes += it.dislike
            }else{
                eSports.add(it)
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += it.like
                totDislikes += it.dislike
            }
        }
        val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
        db.collection("users").document(user.value!!.nickname).update(
            mapOf(
                "sports" to eSports,
                "rating" to rating
            )
        )
        user.value=user.value!!.copy(sports = eSports, rating = rating)
    }
    fun deleteDislike(name: String) {
        val eSports = mutableListOf<Sport>()
        var totVictories = 0f
        var totDefeats = 0f
        var totLikes = 0f
        var totDislikes = 0f
        db.collection("likes").document("${user.value!!.nickname}${actualUser.value!!.nickname}$name").delete()
        user.value!!.sports.forEach {
            if(it.name==name){
                eSports.add(it.copy(dislike = it.dislike-1))
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += it.like
                totDislikes += (it.dislike-1)
            }else{
                eSports.add(it)
                totVictories += it.victories
                totDefeats +=it.defeats
                totLikes += it.like
                totDislikes += it.dislike
            }
        }
        val rating = (if((totVictories+totDefeats)>0f) ((totVictories*50+totDefeats*10)/(totVictories+totDefeats)/50*2) else 2f) + 2f + (if((totDislikes+totLikes)>0f) ((totLikes-totDislikes)/(totDislikes+totLikes)) else (1f))
        db.collection("users").document(user.value!!.nickname).update(
            mapOf(
                "sports" to eSports,
                "rating" to rating
            )
        )
        user.value=user.value!!.copy(sports = eSports, rating = rating)
    }

    override fun onCleared() {
        super.onCleared()
        userReg.remove()
    }

    /*fun resetUserNickname(){
        profileSharedPreferencesManager.resetUserNickname()
        //accessVM.()
    }*/
    /*
    fun updateUserFriends(user: User){
        repository.updateFriends(user)
    }
    */
}
