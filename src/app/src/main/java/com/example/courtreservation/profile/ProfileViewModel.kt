package com.example.courtreservation.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.courtreservation.Sport
import com.example.courtreservation.User
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.example.courtreservation.model.remote.FirestoreManager
import com.example.courtreservation.model.remote.StorageManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileSharedPreferencesManager: SharedPreferencesManager,
    private val repository: FirestoreManager,
    private val storage: StorageManager
    ) : ViewModel() {
    //val user = mutableStateOf<User?>(null)
    private val db = Firebase.firestore

    val user = mutableStateOf<User?>(null)
    private val userReg = db.collection("users").document(getNickname())
        .addSnapshotListener { snapshot, err ->
            if (err == null && snapshot != null) {
                user.value = snapshot.toObject(User::class.java)
            }
        }

    suspend fun uploadImage(uri:Uri){
        val link:String? = try{
            storage.uploadImage(uri)
        } catch (e: Error){
            ""
        }
        updateUserLink(getUserByUsername(getNickname()).link, getUserByUsername(getNickname()).copy(link=link))
    }

    fun getNickname(): String {
        return profileSharedPreferencesManager.getNickname()
    }
    suspend fun getUserByUsername(username: String): User{
        return repository.getUserByUsername(username)
    }

    suspend fun getSports(username: String) : List<Sport>{
        return repository.getUserByUsername(username).sports
    }

    fun updateUserName(user: User){
        repository.updateUserName(user)
    }
    fun updateUserSurname(user: User){
        repository.updateUserSurname(user)
    }
    fun updateAge(user: User){
        repository.updateAge(user)
    }
    fun updateUserCity(user: User){
        repository.updateUserCity(user)
    }
    fun updateUserBio(user: User){
        repository.updateUserBio(user)
    }
    fun updateUserEmail(user: User){
        repository.updateEmail(user)
    }
    private fun updateUserLink(lastLink:String?, user: User){
        repository.updateLink(user, lastLink)
    }
    suspend fun updateSports(sport: Sport){
        val eSports= mutableListOf<Sport>()
        getUserByUsername(getNickname()).sports.forEach {
            if(it.name==sport.name){
                eSports.add(sport)
            }else{
                eSports.add(it)
            }
        }
        repository.updateSports(getUserByUsername(getNickname()).copy(sports = eSports))
    }

    suspend fun addSport(name: String, level: String){
        val eSports= mutableListOf<Sport>()
        getUserByUsername(getNickname()).sports.forEach {
            if(it.name==name){
                eSports.add(it.copy(active=true, level=level))
            }else{
                eSports.add(it)
            }
        }
        repository.updateSports(getUserByUsername(getNickname()).copy(sports = eSports))
    }
    fun resetNickname(){
        profileSharedPreferencesManager.resetNickname()
        //accessVM.()
    }
    /*
    fun updateUserFriends(user: User){
        repository.updateFriends(user)
    }
    */
}
