package com.example.courtreservation.model.remote

import android.util.Log
import com.example.courtreservation.Sport
import com.example.courtreservation.User
import com.example.courtreservation.UserBasicInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class FirestoreManager @Inject constructor(private val firestore: FirebaseFirestore) {

    fun addDocumentInCollection(collectionName: String, data: Map<String, String>) : Task<DocumentReference> {
        return firestore.collection(collectionName).add(data)
    }
    fun deleteDocumentInCollection(collectionName: String, documentName: String): Task<Void> {
        return firestore.collection(collectionName).document(documentName).delete()
    }
    fun editDocumentInCollection(collectionName: String, documentName: String, data: Map<String, Any>): Task<Void> {
        return firestore.collection(collectionName).document(documentName).update(data)
    }
    fun observePlatformReservations(collectionName: String): Query {
        return firestore.collection(collectionName)
    }
    fun observePlatformPlaygrounds(collectionName: String): Query {
        return firestore.collection(collectionName)
    }
    fun observeNotifications(collectionName: String, to: String, username: String, id: String, value: String): Query {
        return firestore. collection(collectionName)
            //.whereArrayContains(to, username)
            .whereEqualTo(id, value)
            //.orderBy("timestamp", Query.Direction.ASCENDING)
    }


    suspend fun getUserByUsername(nickname: String): User{
        val doc = firestore.collection("users").document(nickname).get().await()
        return doc.toObject(User::class.java)!!
/*
        val sports = mutableListOf<Sport>()
        val friends = mutableListOf<UserBasicInfo>()
            for (i in 0 until JSONArray(doc["sports"].toString()).length()) {
                val jsonSport = JSONArray(doc["sports"].toString()).getJSONObject(i)
                sports.add(Sport(
                    jsonSport.getString("name"),
                    jsonSport.getString("level"),
                    jsonSport.getString("rating").toFloat(),
                    Integer.parseInt(jsonSport.getString("victories")),
                    Integer.parseInt(jsonSport.getString("defeats")),
                    Integer.parseInt(jsonSport.getString("meets")),
                    Integer.parseInt(jsonSport.getString("like")),
                    Integer.parseInt(jsonSport.getString("dislike")),
                    jsonSport.getString("active")=="true",
                    jsonSport.getString("visible")=="true",
                    ))
            }
            for(i in 0 until JSONArray(doc["friends"].toString()).length()){
                val jsonFriend = JSONArray(doc["friends"].toString()).getJSONObject(i)
                friends.add(
                    UserBasicInfo(
                    jsonFriend.getString("nickname"),
                    jsonFriend.getString("name"),
                    jsonFriend.getString("surname"),
                    jsonFriend.getString("level"),
                    jsonFriend.getString("rating").toFloat()
                )
                )
            }
            return User(
                doc["nickname"].toString(),
                doc["name"].toString(),
                doc["surname"].toString(),
                Integer.parseInt(doc["age"].toString()),
                doc["city"].toString(),
                doc["bio"].toString(),
                doc["rating"].toString().toFloat(),
                doc["email"].toString(),
                sports,
                link = doc["link"].toString(),
                friends = friends
            )*/
    }
    fun updateUserName(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("name" to user.name)
        )
    }
    fun updateUserSurname(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("surname" to user.surname)
        )
    }
    fun updateAge(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("age" to user.age)
        )
    }
    fun updateUserCity(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("city" to user.city)
        )
    }
    fun updateUserBio(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("bio" to user.bio)
        )
    }
    fun updateEmail(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("email" to user.email)
        )
    }
    fun updateLink(user: User, lastLink: String?){

        firestore.collection("users")
            .whereArrayContains("friends", UserBasicInfo(user.nickname, user.name, user.surname, "beginner", rating=0.0f, link = lastLink))
            .get()
            .addOnSuccessListener {
                it.documents.forEach{doc->
                    val friends = doc.toObject(User::class.java)?.friends?.map { friend->
                        if(friend.nickname==user.nickname){
                            friend.copy(link = user.link)
                        } else friend
                    }
                    firestore.collection("users").document(doc.id).update(
                        mapOf("friends" to friends)
                    )
                }
            }
        firestore.collection("users").document(user.nickname).update(
            mapOf("link" to user.link)
        )
    }
    fun updateSports(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("sports" to user.sports)
        )
    }
    /*
    fun updateFriends(user: User){
        firestore.collection("users").document(user.nickname).update(
            mapOf("friends" to user.friends)
        )
    }

     */
}