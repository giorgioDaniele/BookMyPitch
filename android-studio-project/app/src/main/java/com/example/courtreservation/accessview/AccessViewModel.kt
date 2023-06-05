package com.example.courtreservation.accessview

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.courtreservation.Sport
import com.example.courtreservation.User
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AccessViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {
/*
    private var _nickname = ""
    var nickname get() = _nickname
        set(value) {
            _nickname = value
        }
*/
    val userNickname = mutableStateOf<String?>("").apply { this.value = getNickname() }
    suspend fun login(nickname: String, onReturn: (String) -> Unit) {

        val document = Firebase.firestore.collection("users").document("@$nickname")
        if (document.get().await().exists()) {
            sharedPreferencesManager.setNickname("@$nickname")
            userNickname.value="@$nickname"
            onReturn("Success")
        } else {
            onReturn("The nickname does not exist")
        }
    }
    suspend fun register(nickname:String,name: String, surname: String, age: String, onReturn: (String) -> Unit) {

        val document = Firebase.firestore.collection("users").document("@$nickname")
        if (document.get().await().exists()) {
            onReturn("The nickname is already in use")
        } else {
            sharedPreferencesManager.setNickname("@$nickname")
            userNickname.value="@$nickname"
            document.set(
                User(
                "@$nickname", name, surname, Integer.parseInt(age), "","",0f,"",
                    listOf(
                        "Football", "Volleyball", "Basketball", "Tennis", "Cricket",
                        "Baseball", "Rugby", "Hockey", "Padel", "Badminton").map {
                        Sport(it, "beginner",0f,0,0,0,0,0,active = false,visible = true)
                    }
                )
            )
            onReturn("Success")
        }
    }
    fun logout(){
        userNickname.value=""
    }

    private fun getNickname(): String = sharedPreferencesManager.getNickname().ifEmpty { "" }
}



/*

@HiltViewModel
class AccessViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val firestoreManager: FirestoreManager) : ViewModel() {

    private var _firstName = ""
    var firstName get() = _firstName
        set(value) {
            _firstName = value
        }

    private var _secondName = ""
    var secondName get() = _secondName
        set(value) {
            _secondName = value
        }

    private var _nickname = ""
    var nickname get() = _nickname
        set(value) {
            _nickname = value
        }

    suspend fun backUpProfile(onReturn: (String) -> Unit) {
        val maps :  List<HashMap<String, Any>> = listOf(
            "Football", "VolleyBall", "Basketball", "Tennis", "Cricket",
            "Baseball", "Rugby", "Hockey", "Golf", "Badminton").map {
                buildSportMap(it, "beginner")
        }

        val data = hashMapOf(
            "age"   to 0, "bio" to "", "city" to "", "email" to "",
            "name"  to _firstName, "nickname" to _nickname, "rating" to 0, "sports" to maps, "surname" to _secondName,
        )

        val document = Firebase.firestore.collection("users").document("@$nickname")
        if (document.get().await().exists()) {
            onReturn("The nickname is already in use")
        } else {
            document.set(data)
            sharedPreferencesManager.update(_firstName, _secondName, "@$_nickname")
            onReturn("Success")
        }
    }

    fun userIsLogged() : Boolean {
        return sharedPreferencesManager.userIsLogged()
    }

    private fun buildSportMap(name: String, level: String): HashMap<String, Any> {
        return hashMapOf(
            "active"  to false,
            "defeats" to 0,
            "dislike" to 0,
            "level"   to level,
            "like"    to 0,
            "meets"   to 0,
            "name"    to name,
            "rating"  to 0,
            "victories" to 0,
            "visible" to true
        )
    }


    // For testing
    fun helloUser() : String = sharedPreferencesManager.getString("firstName", "") + " " +
                 sharedPreferencesManager.getString("secondName", "") + " " +
                 sharedPreferencesManager.getString("nickname", "")


}
 */