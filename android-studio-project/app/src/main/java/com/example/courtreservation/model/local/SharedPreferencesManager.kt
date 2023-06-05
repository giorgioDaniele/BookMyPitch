package com.example.courtreservation.model.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import com.example.courtreservation.User
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(app: Application) {


    private val sharedPreferences: SharedPreferences =
        app.getSharedPreferences("profile", Context.MODE_PRIVATE)

    /********************************** GIORGIO VERSION *******************************************/
    fun userIsLogged() : Boolean {
        val allEntries = sharedPreferences.all
        return allEntries.isNotEmpty()
    }

    fun update(firstName: String, secondName: String, nickname: String) {
        val editor = sharedPreferences.edit()
        editor.putString("firstName", firstName)
        editor.putString("secondName", secondName)
        editor.putString("nickname", nickname)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue

    }



    /********************************** NICO VERSION ***********************************************/
/*
    companion object {
        private val DEFAULT_PROFILE : String = """{
            "user": {
                "nickname": "null",
                "name": "Name",
                "surname": "Surname",
                "city": "City",
                "bio": "Bio",
                "rating": 4.2,
                "age": 0,
                "email": "email@email.com"
            }
            }""".trimIndent()
    }
*/
    fun getImage(): Uri? {
        return try {
            sharedPreferences.getString("avatarUri", "")?.let {
                it.ifEmpty { null }
            }?.toUri()
        } catch (e: JSONException){
            null
        }
    }
    fun saveImage(uri: Uri){
        with (sharedPreferences.edit()) {
            putString("avatarUri", uri.toString())
            apply()
        }
    }
    /*
    fun getUser(): User {
        val profile = JSONObject(sharedPreferences.getString("profile", DEFAULT_PROFILE) ?: DEFAULT_PROFILE)
        val userJson: JSONObject = profile.getJSONObject("user")
        return User(
            userJson.get("nickname").toString(),
            userJson.get("name").toString(),
            userJson.get("surname").toString(),
            Integer.parseInt(userJson.get("age").toString()),
            userJson.get("city").toString(),
            userJson.get("bio").toString(),
            userJson.get("rating").toString().toFloat(),
            userJson.get("email").toString(),
            mutableListOf()
        )
    }*/
    fun getNickname(): String {
        return sharedPreferences.getString("nickname", "")?: ""
    }
    fun resetNickname(){
        with(sharedPreferences.edit()){
            putString("nickname", "")
            apply()
        }
    }
    fun setNickname(nickname: String){
        with(sharedPreferences.edit()){
            putString("nickname", nickname)
            apply()
        }
    }

    fun getUserNickname(): String {
        return sharedPreferences.getString("userNickname", "")?: ""
    }
    fun resetUserNickname(){
        with(sharedPreferences.edit()){
            putString("userNickname", "")
            apply()
        }
    }
    fun setUserNickname(nickname: String){
        with(sharedPreferences.edit()){
            putString("userNickname", nickname)
            apply()
        }
    }
    /*
        fun getSports(): List<Sport>{
            val sportList=mutableListOf<Sport>()
            var len= sharedPreferences.getString("sports_size","0")?.let { Integer.parseInt(it) }
            //Log.d("VM","ci sono: ${len} sport")
            //var len=3//Integer.parseInt(profile.getString("sports_size"))
            if (len != null) {
                while (len>0){
                    val sport= sharedPreferences.getString("sport_${len}","")?.let { JSONObject(it) }
                    //val sport= profile.getJSONObject("sport_$len")
                    if (sport != null) {
                        sportList.add(
                            Sport(
                            id = "sport_${len}",
                            name = sport.getString("name"),
                            level = sport.getString("level"),
                            rating = sport.getString("rating"),
                            victories = sport.getString("victories"),
                            defeats = sport.getString("defeats"),
                            meets = sport.getString("meets"),
                            like = sport.getString("like"),
                            dislike = sport.getString("dislike"),
                            visible = sport.getString("visible")
                        )
                        )
                    }
                    len-=1
                }
            }
            return sportList.sortedByDescending { it.rating }.toList()
        }
    */
    /*
    fun saveSport(sport: Sport){
        with (sharedPreferences.edit()) {
            putString(sport.id,
                """
                    {
                        "id": ${sport.id},
                        "name": "${sport.name}",
                        "level": "${sport.level}",
                        "rating": "${sport.rating}",
                        "victories": ${sport.victories},
                        "defeats": ${sport.defeats},
                        "meets": ${sport.meets},
                        "like": ${sport.like},
                        "dislike": ${sport.dislike},
                        "visible": "${sport.visible}"
                    }
            """.trimIndent())
            apply()
        }
    }
*/
    /*  fun getNumSports():Int{
          return Integer.parseInt(sharedPreferences.getString("sports_size","0") ?: "0")
      }
  */
    /*  fun addSport(sport: Sport){
          var len=Integer.parseInt(sharedPreferences.getString("sports_size","0") ?: "0")
          len +=1
          with (sharedPreferences.edit()) {
              putString("sports_size",len.toString())
              putString(sport.id,
                  """
                      {
                          "id": ${sport.id},
                          "name": "${sport.name}",
                          "level": "${sport.level}",
                          "rating": "${sport.rating}",
                          "victories": ${sport.victories},
                          "defeats": ${sport.defeats},
                          "meets": ${sport.meets},
                          "like": ${sport.like},
                          "dislike": ${sport.dislike},
                          "visible": "${sport.visible}"
                      }
              """.trimIndent())
              apply()
          }

      }
  */
    fun saveUser(user: User){
        with (sharedPreferences.edit()) {
            val userJSON= JSONObject()
            userJSON.put("nickname", user.nickname)
            /*val profile= """{
            "user": {
                "nickname": "${user.nickname}",
                "name": "${user.name}",
                "surname": "${user.surname}",
                "city": "${user.city}",
                "bio": "${user.bio}",
                "rating": ${user.rating},
                "age": ${user.age},
                "email": "${user.email}"
            }
            }""".trimIndent()
            putString("profile", profile)*/
            apply()
        }
    }
    /*
        //utility
        fun insertSports(vm: ProfileViewModel){
            vm.addSport(
                Sport("sport_1", "calcio","advanced","5.0",
                "20","4","104","50","10","true")
            )
            vm.addSport(
                Sport("sport_2", "basket","intermediate","4.1",
                "9","8","34","24","6","true")
            )
            vm.addSport(
                Sport("sport_3", "volley","beginner","3.7",
                "4","6","22","7","3","true")
            )
        }
    */
}