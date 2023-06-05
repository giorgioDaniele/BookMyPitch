package com.example.courtreservation

import android.util.Log
import androidx.compose.ui.text.toLowerCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Calendar

import kotlin.math.roundToInt

//////////////////

data class Like(
    val id: String = "",
    val like: Int = 0 //-1 dislike, 0 nothing, 1 like
)
data class Reservation(
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val hour: Int = 0,
    val playgroundId: String = "",
    val customRequest: String? = null,
    val status: String = "",
    // active (i player si possono unire)
    // closed (se era confirmed ed è passato l'orario)
    // cancelled (se 2 ore precedenti all'ora non si sono raggiunti il numero giusto di player)
    // confirmed (se nelle ultime 24 ore si hanno raggiunti il numero giusto di player)

    //val teamA : List<UserBasicInfo>, //prefilled with the UserBasicInfo oh the user who created Reservation
    //val teamB : List<UserBasicInfo>,
    val team_0: List<String> = emptyList(),
    val team_1: List<String> = emptyList(),
    val result: Int? = null, //0 -> team_0 win, 1 -_ team_1 win
    val id: String? =null,
)
data class Playground(
    val name: String = "",
    val sport: String = "",
    val cost: Float = 0.0f,
    val num_players_per_team: Int = 0, // number of players
    val avg_rating: Float = 0.0f,
    val num_ratings: Int = 0,
    val num_comments: Int = 0
)
data class UserBasicInfo(
    val nickname: String = "",
    val name: String = "",
    val surname: String = "",
    val level: String = "", //level for that sport (maybe make nullable and use when show list of friends without level)
    val rating: Float = 0.0f,
    val link: String? = null,
    )
data class User(
    val nickname: String = "",
    val name: String = "",
    val surname: String = "",
    val age: Int = 0,
    val city: String = "",
    val bio: String = "",
    val rating: Float = 0.0f,
    val email: String = "",
    val sports: List<Sport> = emptyList(),
    val link: String? = null, //link to the profile image
    val friends: List<UserBasicInfo>?= emptyList()
    )
data class Sport(
    val name: String = "",
    val level: String = "",
    val rating: Float = 0.0f,
    val victories: Int = 0,
    val defeats: Int = 0,
    val meets: Int = 0,
    val like: Int = 0,
    val dislike: Int = 0,
    var active: Boolean = false,
    val visible: Boolean = false,
)
data class EvaluationPlaygrounds(
    val userId: String, //nickname
    val playgroundId: String,
    val rating: Int,
)

//////////////////
//data class Reservation(val day: Int, val month: Int, val year: Int, val hour: Int, val playgroundId: String, val customRequest: String?, val userId: String)
//data class Playground(val name: String, val sport: String)
/*
fun getReservations(index: Int): Reservation {
    val document = JSONObject(RESERVATIONS)
    val reservation = document.getJSONObject(index.toString())
    return Reservation(
        reservation.getInt("day"),
        reservation.getInt("month"),
        reservation.getInt("year"),
        reservation.getInt("hour"),
        reservation.getString("playground"),
        try { reservation.getString("custom_request") } catch (_: JSONException) { null },
        reservation.getString("username")
    )
}
*/
/*
fun getPlaygrounds(): List<Playground> {
    val document = JSONArray(PLAYGROUNDS)
    val playgrounds = mutableListOf<Playground>()

    val len = document.length()
    for (i in 0 until len) {
        val p = document.getJSONObject(i)

        playgrounds.add(Playground(
            p.getString("name"),
            p.getString("sport")
        ))
    }

    return playgrounds
}
*/
//////////////

suspend fun changeUserIdIntoNickname() {
    val db = Firebase.firestore
    val usersCollection = db.collection("users")

    val users = usersCollection
        .get()
        .await()
        .documents
        .forEach { document ->
            val user = document.data
            val id = document.id

            if (user != null && !id.startsWith("@")) {
                val nickname = user["nickname"]?.toString()

                if (!nickname.isNullOrEmpty())
                    usersCollection.document(nickname).set(user).await()

                usersCollection.document(id).delete().await()
            }
        }

}

/**
 * add n reservations
 */
fun addReservations(db: FirebaseFirestore){
    Log.d("SetUpData","addReservations")
    val n=20
    val year= mutableListOf<Int>(2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,2023,
        2023,2023,2023,2023,2023,2023,2023,)
    val month= mutableListOf<Int>(
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        5,5,5,5,5,5,5,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,4,4,4
    )
    val day= mutableListOf<Int>(2,2,2,7,8,9,5,2,20,20,20,24,26,26,26,26,26,28,28,28,2,2,2,7,8,9,5,2,
        20,20,20,24,26,26,26,26,26,28,28,28,
        27, 2, 24, 6, 21, 18, 7, 17, 12, 28, 8, 24, 10, 14, 18, 28, 11, 8, 24, 13, 6, 15, 2, 7, 13, 18,
        11, 22, 15, 17, 13, 26, 15, 11, 25, 3, 5, 2, 9, 26, 6, 24, 28, 1, 11, 29, 19, 4, 16, 20, 9, 24,
        18, 21, 23, 24, 6, 21, 1, 6, 13, 29, 2, 13, 5, 13, 15, 15, 12, 3, 26, 5, 11, 16, 22, 23, 13, 5,
        7, 20, 25, 28, 23, 25, 1, 25, 20, 13, 19, 19, 14, 14, 14, 24, 17, 9, 6, 19, 3, 29,1, 2, 17, 27,
        12, 20, 21, 9, 23, 28, 15, 11, 10, 16, 5, 17, 24, 26, 11, 28, 24, 21, 25, 13, 12, 22, 5, 29, 9,
        21, 4, 21, 26, 9, 27, 23, 8, 8, 17, 25, 14, 9, 12, 27, 22, 13, 9, 8, 2, 25, 1, 29, 25, 11, 5,
        7, 16, 2, 5, 15
    )
    val hour= mutableListOf<Int>(10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8,
        14, 16, 22,10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8, 14, 16, 22,
        10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8,
        14, 16, 22,10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8, 14, 16, 22,
        10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8,
        14, 16, 22,10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8, 14, 16, 22,
        10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8,
        14, 16, 22,10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8, 14, 16, 22,
        10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8,
        14, 16, 22,10, 22, 17, 19, 14, 9, 12, 21, 15, 8, 20, 16, 23, 11, 18, 13, 8, 14, 16, 22
    )
    /*val username= mutableListOf<String>(
        "ShadowWolf", "StarGazer", "ThunderStrike", "MoonlightDreamer", "FireFlyer", "NightHawk", "SilverArrow",
        "nico", "miolad", "balenablu",
        "TigerEye", "PhoenixRider", "StormChaser", "DragonHeart", "Frostbite", "JungleQueen", "CosmicTraveler",
        "WildSpirit", "Avalanche", "ElectricDream", "OceanSoul", "MidnightSorcerer", "GalacticExplorer", "SnowLeopard",
        "AmberFlame", "SolarFlare", "DiamondDust", "MysticShadow", "SerenitySeeker", "CrimsonFalcon", "EarthGuardian",
        "VelvetWhisper", "DreamCatcher", "EchoSerenade", "PhoenixFire", "ScarletEmber", "LunarEclipse", "AlpineJumper",
        "EmeraldEnigma", "StarlightStrider", "SwiftWinds", "CrimsonLotus", "NebulaRider", "DreamWeaver", "StormySeas",
        "WhisperingWillow", "Thunderbolt", "CrystalEyes", "EchoBlade", "AuroraBorealis", "BlazeSpecter", "VelvetSiren", "SapphireWanderer",
        "JazzySmith21", "OliverJohnson19", "SophisticatedSophia28", "EthanBrown23", "BellaJones24", "SophieTaylor19", "JakeMiller26",
        "AvaAnderson20", "MiaJackson20", "NoahWhite20", "LiamHarris36", "EmmaClark33", "CharlieLewis31", "LucasLee30", "AmelieWalker32",
        "BenYoung24", "IsabellaAllen34", "HarperWright25", "ElijahAdams22", "WilliamMartin39", "JamesHill25", "MichaelMoore20",
        "AlexScott27", "EvelynKing26", "AbbyBaker20", "DannyGonzalez22", "EmilyNelson30", "EllaCarter31", "SebMitchell29", "MattPerez35",
        "DavidRoberts20", "LoganTurner24", "GraciePhillips21", "JoeyCampbell25", "DannyParker21", "ScarlettEvans25", "AidenEdwards21",
        "HenryCollins25", "JackStewart21", "SamSanchez20", "ToriMorris38", "SofiaRogers37", "MasonReed22", "GabeCook37", "LizzyMorgan36",
        "EmilyBell21", "EthanMurphy21", "MadisonSmith22", "LilyJohnson23", "ZoeWilliams20"
    )*/
    //val numForPlayground= mutableListOf<Int>(10,10,15,15,20,20,25,25,30,30)
    val numForPlayground= mutableListOf<Int>(4,3,3,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0)

    var count=0
    db.collection("users").get().addOnSuccessListener {users ->
        val userList = mutableListOf<User>()
        users.forEach { userList.add(it.toObject(User::class.java)) }
        if(hour.size==year.size && hour.size==month.size && hour.size == day.size){
            db.collection("playgrounds").get().addOnSuccessListener{collection->
                collection.forEach{doc->
                    if(count<n){
                        for(i in 1..numForPlayground.removeLast()){
                            val team0 = mutableListOf<String>()
                            val team1 = mutableListOf<String>()
                            (0..doc.toObject(Playground::class.java).num_players_per_team).map {
                                var user=userList[(Math.random()*(userList.size-1)).roundToInt()]
                                var cnt = 0;
                                while (cnt<100 && (user.nickname in team0 || user.sports.none {
                                        (it.name == doc.toObject(
                                            Playground::class.java
                                        ).sport && it.active)
                                    })) {
                                    user=userList[(Math.random()*(userList.size-1)).roundToInt()]
                                    cnt+=1
                                }
                                team0.add(user.nickname)
                            }
                            (0..doc.toObject(Playground::class.java).num_players_per_team).map {
                                var user=userList[(Math.random()*(userList.size-1)).roundToInt()]
                                var cnt = 0;
                                while (cnt<100 && (user.nickname in team0 || user.nickname in team1 || user.sports.none {
                                        (it.name == doc.toObject(
                                            Playground::class.java
                                        ).sport && it.active)
                                    })) {
                                        user=userList[(Math.random()*(userList.size-1)).roundToInt()]
                                        cnt+=1
                                }
                                team1.add(user.nickname)
                            }
                            db.collection("reservations").add(Reservation(day.removeLast(), month.removeLast(), year.removeLast(),
                                hour.removeLast(), doc.id, null,"active",
                                team0,
                                team1
                            ))
                            Log.d("SetUpData", "added reservation")
                        }
                    }
                    count+=1
                }
            }
        }
    }
}
fun addPlaygrounds(db: FirebaseFirestore){
    val names= mutableListOf<String>("CUS Torino","CUS Torino","Centro Sportivo Robilant","Centro Sportivo Robilant",
        "Centro Sportivo Robilant","Centro Sportivo Colletta, TO","Ruffini Centro Sportivo","Centro Sportivo Colletta, TO",
        "Centro Sportivo Colletta, TO","CUS Torino"
    )
    val sports= mutableListOf<String>("Soccer","Volley","Soccer","Volley","Tennis","Soccer",
        "Basket","Soccer","Volley","Basket"
    )
}
/***
 * Script for generating 100 users
 */
fun addUsers(db: FirebaseFirestore){
    Log.d("SetUpData","Entrano in addUser")
    val name= mutableListOf<String>("Jasmine", "Oliver", "Sophia", "Ethan", "Isabella", "Sophie", "Jacob",
        "Ava", "Mia", "Noah", "Liam", "Emma", "Charlotte", "Lucas", "Amelia", "Benjamin", "Olivia", "Harper",
        "Elijah", "William", "James", "Michael", "Alexander", "Evelyn", "Abigail", "Daniel", "Emily", "Ella",
        "Sebastian", "Matthew", "David", "Logan", "Grace", "Joseph", "Daniel", "Scarlett", "Aiden", "Henry",
        "Jackson", "Samuel", "Victoria", "Sofia", "Mason", "Gabriel", "Elizabeth", "Emily", "Ethan", "Madison",
        "Lily", "Zoe",
        "Liam", "Olivia", "Noah", "Emma", "Oliver", "Ava", "Sophia", "Isabella", "Mia", "Charlotte",
        "Amelia", "Harper", "Elijah", "Lucas", "Benjamin", "Evelyn", "James", "Michael", "Alexander",
        "William", "Daniel", "Matthew", "David", "Emily", "Logan", "Grace", "Joseph", "Sebastian", "Gabriel",
        "Scarlett", "Samuel", "Victoria", "Sofia", "Mason", "Aiden", "Jackson", "Henry", "Zoe", "Madison",
        "Lily", "Ella", "Chloe", "Penelope", "Luna", "Carter", "Eleanor", "Nora", "Riley", "Leo", "Eva"
    )
    val nickname= mutableListOf<String>(
        "ShadowWolf", "StarGazer", "ThunderStrike", "MoonlightDreamer", "FireFlyer", "NightHawk", "SilverArrow",
        "TigerEye", "PhoenixRider", "StormChaser", "DragonHeart", "Frostbite", "JungleQueen", "CosmicTraveler",
        "WildSpirit", "Avalanche", "ElectricDream", "OceanSoul", "MidnightSorcerer", "GalacticExplorer", "SnowLeopard",
        "AmberFlame", "SolarFlare", "DiamondDust", "MysticShadow", "SerenitySeeker", "CrimsonFalcon", "EarthGuardian",
        "VelvetWhisper", "DreamCatcher", "EchoSerenade", "PhoenixFire", "ScarletEmber", "LunarEclipse", "AlpineJumper",
        "EmeraldEnigma", "StarlightStrider", "SwiftWinds", "CrimsonLotus", "NebulaRider", "DreamWeaver", "StormySeas",
        "WhisperingWillow", "Thunderbolt", "CrystalEyes", "EchoBlade", "AuroraBorealis", "BlazeSpecter", "VelvetSiren", "SapphireWanderer",
        "JazzySmith21", "OliverJohnson19", "SophisticatedSophia28", "EthanBrown23", "BellaJones24", "SophieTaylor19", "JakeMiller26",
        "AvaAnderson20", "MiaJackson20", "NoahWhite20", "LiamHarris36", "EmmaClark33", "CharlieLewis31", "LucasLee30", "AmelieWalker32",
        "BenYoung24", "IsabellaAllen34", "HarperWright25", "ElijahAdams22", "WilliamMartin39", "JamesHill25", "MichaelMoore20",
        "AlexScott27", "EvelynKing26", "AbbyBaker20", "DannyGonzalez22", "EmilyNelson30", "EllaCarter31", "SebMitchell29", "MattPerez35",
        "DavidRoberts20", "LoganTurner24", "GraciePhillips21", "JoeyCampbell25", "DannyParker21", "ScarlettEvans25", "AidenEdwards21",
        "HenryCollins25", "JackStewart21", "SamSanchez20", "ToriMorris38", "SofiaRogers37", "MasonReed22", "GabeCook37", "LizzyMorgan36",
        "EmilyBell21", "EthanMurphy21", "MadisonSmith22", "LilyJohnson23", "ZoeWilliams20"
    )
    val surname= mutableListOf<String>("Smith", "Johnson", "Williams", "Brown", "Jones", "Taylor", "Miller",
        "Anderson", "Jackson", "White", "Harris", "Clark", "Lewis", "Lee", "Walker", "Hall", "Young", "Allen",
        "Wright", "Adams", "Martin", "Hill", "Moore", "Scott", "King", "Green", "Baker", "Gonzalez", "Nelson",
        "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans",
        "Edwards", "Collins", "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Smith",
        "Johnson", "Williams", "Brown", "Jones", "Taylor", "Miller", "Anderson", "Jackson", "White", "Harris",
        "Clark", "Lewis", "Lee", "Walker", "Hall", "Young", "Allen", "Wright", "Adams", "Martin", "Hill",
        "Moore", "Scott", "King", "Green", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez",
        "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart",
        "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Taylor", "Wilson"
        )
    val age= mutableListOf<Int>(21, 19, 28, 23, 24, 19, 26, 20, 20, 20, 36, 33, 31, 30, 32, 24, 34,
        25, 22, 39, 25, 20, 27, 26, 20, 22, 30, 31, 29, 35, 20, 24, 21, 25, 21, 25, 21, 21, 22, 23,
        20, 25, 21, 20, 38, 37, 22, 37, 36, 21,21, 19, 28, 23, 24, 19, 26, 20, 20, 20, 36, 33, 31,
        30, 32, 24, 34, 25, 22, 39, 25, 20, 27, 26, 20, 22, 30, 31, 29, 35, 20, 24, 21, 25, 21, 25,
        21, 21, 22, 23, 20, 25, 21, 20, 38, 37, 22, 37, 36, 21)
    val city= mutableListOf<String>("Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin","Turin",
        "Turin","Turin","Turin","Turin","Turin","Turin")
    val bio= mutableListOf<String>(
        "I'm a passionate adventurer, always seeking new thrills and challenges. From scaling mountains " +
                "to exploring the depths of the ocean, there's no limit to my curiosity. Join me on my journey" +
                " as I push my limits and embrace the wonders of the world.",
        "An artist at heart, I find inspiration in the beauty that surrounds us. With a paintbrush in hand, " +
                "I bring vibrant colors to life on canvas, capturing the essence of emotions and stories. " +
                "Step into my gallery and experience the magic of art.",
        "A dedicated food enthusiast with a flair for flavors, I embark on culinary adventures that tickle " +
                "the taste buds. From Michelin-starred restaurants to street food gems, I explore the world " +
                "one dish at a time, savoring every bite and sharing my culinary escapades.",
        "As a bookworm and wordsmith, I navigate the realms of literature with an insatiable thirst for knowledge." +
                " From classic novels to contemporary poetry, I dive into the pages that transport me to different" +
                " worlds, allowing words to paint vivid pictures in my mind.",
        "A fitness fanatic and wellness advocate, I strive for a balanced and active lifestyle. From sunrise yoga" +
                " sessions to high-intensity workouts, I push my body and mind to new heights. Join me on a journey " +
                "of self-discovery and personal transformation through fitness.",
        "Passionate about sports and fitness. Always up for a new challenge.",
        "Adventure seeker with a love for outdoor activities and team sports.",
        "Enthusiastic athlete with a competitive spirit. Ready to push my limits.",
        "Fitness junkie and health enthusiast. Believe in the power of a balanced lifestyle.",
        "Sports lover and adrenaline junkie. Constantly seeking new thrills.",
        "Dedicated to living an active life. Sports and fitness are my passion.",
        "Team player and sports enthusiast. Enjoying the journey towards greatness.",
        "Chasing dreams and breaking boundaries. Sports fuel my soul.",
        "Embracing the joy of movement. Finding happiness through sports.",
        "Pushing my limits and overcoming challenges. Sports are my escape.",
        "Exploring the world one sport at a time. Life is an adventure.",
        "Believer in the power of perseverance. Sports teach valuable life lessons.",
        "Finding solace in the rhythm of sports. Embracing the flow of life.",
        "Embracing the beauty of movement. Sports inspire me to be my best self.",
        "Passionate about the thrill of competition. Striving for greatness in every game.",
        "Adventurous soul with a love for extreme sports and daring adventures.",
        "Celebrating the joy of play. Sports bring out the child in me.",
        "Embracing the challenge of pushing beyond my limits. Sports are my catalyst.",
        "Finding balance and strength through sports. Mind, body, and soul.",
        "Passionate about the art of athleticism. Pursuing excellence in every move.",
        "Fueling my passion for sports with dedication and hard work.",
        "Embracing the power of teamwork. Together, we can achieve greatness.",
        "Inspired by the grace and elegance of sports. Striving for perfection.",
        "Unleashing the athlete within. Defying odds and shattering stereotypes.",
        "Championing a fit and active lifestyle. Sports are my way of life.",
        "Finding freedom in the rhythm of sports. Embracing the journey.",
        "Driven by the pursuit of victory. Embracing the challenges along the way.",
        "Living life to the fullest through sports and adventure. No regrets.",
        "Passionate about inspiring others to lead an active life. Be the change.",
        "Fueling my fire through the love of sports. There are no limits.",
        "Adventuring through life one sport at a time. Embracing the unknown.",
        "Believer in the power of perseverance and determination. Sports mold character.",
        "Celebrating the joy of movement. Embracing the beauty of sports.",
        "Finding strength and resilience through sports. Pushing boundaries.",
        "Passionate about the thrill of the game. Every moment counts.",
        "Embracing the journey of self-discovery through sports. Unleashing my potential.",
        "Inspired by the legends who paved the way. Leaving my mark in sports history.",
        "Fueling my drive with passion and dedication. Sports are my motivation.",
        "Finding joy in the simplicity of play. Sports bring out the best in me.",
        "Unleashing my inner athlete. Embracing the power of sports.",
        "Chasing dreams and conquering fears through sports. Anything is possible.",
        "Believer in the power of perseverance. Sports teach valuable life lessons.",
        "Embracing the spirit of competition. Sports fuel my hunger for success.",
        "Determined to leave a legacy in the world of sports. Making every moment count.",
        "Passionate about the transformative power of sports. It's more than just a game.",
        "I'm a passionate adventurer, always seeking new thrills and challenges. From scaling mountains " +
                "to exploring the depths of the ocean, there's no limit to my curiosity. Join me on my journey" +
                " as I push my limits and embrace the wonders of the world.",
        "An artist at heart, I find inspiration in the beauty that surrounds us. With a paintbrush in hand, " +
                "I bring vibrant colors to life on canvas, capturing the essence of emotions and stories. " +
                "Step into my gallery and experience the magic of art.",
        "A dedicated food enthusiast with a flair for flavors, I embark on culinary adventures that tickle " +
                "the taste buds. From Michelin-starred restaurants to street food gems, I explore the world " +
                "one dish at a time, savoring every bite and sharing my culinary escapades.",
        "As a bookworm and wordsmith, I navigate the realms of literature with an insatiable thirst for knowledge." +
                " From classic novels to contemporary poetry, I dive into the pages that transport me to different" +
                " worlds, allowing words to paint vivid pictures in my mind.",
        "A fitness fanatic and wellness advocate, I strive for a balanced and active lifestyle. From sunrise yoga" +
                " sessions to high-intensity workouts, I push my body and mind to new heights. Join me on a journey " +
                "of self-discovery and personal transformation through fitness.",
        "Passionate about sports and fitness. Always up for a new challenge.",
        "Adventure seeker with a love for outdoor activities and team sports.",
        "Enthusiastic athlete with a competitive spirit. Ready to push my limits.",
        "Fitness junkie and health enthusiast. Believe in the power of a balanced lifestyle.",
        "Sports lover and adrenaline junkie. Constantly seeking new thrills.",
        "Dedicated to living an active life. Sports and fitness are my passion.",
        "Team player and sports enthusiast. Enjoying the journey towards greatness.",
        "Chasing dreams and breaking boundaries. Sports fuel my soul.",
        "Embracing the joy of movement. Finding happiness through sports.",
        "Pushing my limits and overcoming challenges. Sports are my escape.",
        "Exploring the world one sport at a time. Life is an adventure.",
        "Believer in the power of perseverance. Sports teach valuable life lessons.",
        "Finding solace in the rhythm of sports. Embracing the flow of life.",
        "Embracing the beauty of movement. Sports inspire me to be my best self.",
        "Passionate about the thrill of competition. Striving for greatness in every game.",
        "Adventurous soul with a love for extreme sports and daring adventures.",
        "Celebrating the joy of play. Sports bring out the child in me.",
        "Embracing the challenge of pushing beyond my limits. Sports are my catalyst.",
        "Finding balance and strength through sports. Mind, body, and soul.",
        "Passionate about the art of athleticism. Pursuing excellence in every move.",
        "Fueling my passion for sports with dedication and hard work.",
        "Embracing the power of teamwork. Together, we can achieve greatness.",
        "Inspired by the grace and elegance of sports. Striving for perfection.",
        "Unleashing the athlete within. Defying odds and shattering stereotypes.",
        "Championing a fit and active lifestyle. Sports are my way of life.",
        "Finding freedom in the rhythm of sports. Embracing the journey.",
        "Driven by the pursuit of victory. Embracing the challenges along the way.",
        "Living life to the fullest through sports and adventure. No regrets.",
        "Passionate about inspiring others to lead an active life. Be the change.",
        "Fueling my fire through the love of sports. There are no limits.",
        "Adventuring through life one sport at a time. Embracing the unknown.",
        "Believer in the power of perseverance and determination. Sports mold character.",
        "Celebrating the joy of movement. Embracing the beauty of sports.",
        "Finding strength and resilience through sports. Pushing boundaries.",
        "Passionate about the thrill of the game. Every moment counts.",
        "Embracing the journey of self-discovery through sports. Unleashing my potential.",
        "Inspired by the legends who paved the way. Leaving my mark in sports history.",
        "Fueling my drive with passion and dedication. Sports are my motivation.",
        "Finding joy in the simplicity of play. Sports bring out the best in me.",
        "Unleashing my inner athlete. Embracing the power of sports.",
        "Chasing dreams and conquering fears through sports. Anything is possible.",
        "Believer in the power of perseverance. Sports teach valuable life lessons.",
        "Embracing the spirit of competition. Sports fuel my hunger for success.",
        "Determined to leave a legacy in the world of sports. Making every moment count.",
        "Passionate about the transformative power of sports. It's more than just a game.",
    )
    val friends= mutableListOf<Int>(0, 1, 8, 10, 26, 7, 11, 18, 3, 22, 12, 27, 15, 30, 5, 29, 25, 2, 24,
        13, 19, 14, 17, 28, 16, 23, 9, 6, 21, 20, 4, 7, 5, 6, 30, 6, 5, 7, 10, 8, 9, 4, 3, 8, 14, 7,
        13, 18, 22, 9, 12, 19, 16, 28, 25, 27, 29, 15, 26, 17, 11, 2, 24, 20, 21, 23, 3, 0, 1, 2, 25,
        19, 12, 21, 6, 26, 8, 7, 4, 29, 9, 30, 5, 16, 23, 27, 28, 11, 14, 15, 13, 10, 17, 18, 5,
        21, 6, 26, 8, 7
    )
    val numberOfSports= mutableListOf<Int>(1, 2, 1, 2, 1, 3, 1, 1, 1, 3, 1, 1, 2, 1, 2, 1, 1, 2, 1,
        2, 1, 1, 2, 1, 1, 1, 1, 4, 1, 1, 1, 1, 3, 1, 1, 1, 5, 1, 1, 4, 1, 1, 2, 6, 1, 1, 1, 1, 3, 1,
        1, 2, 1, 2, 1, 3, 1, 1, 1, 3, 1, 1, 2, 1, 2, 1, 1, 2, 1,
        2, 1, 1, 2, 1, 1, 1, 1, 4, 1, 1, 1, 1, 3, 1, 1, 1, 5, 1, 1, 4, 1, 1, 2, 6, 1, 1, 1, 1, 3, 1)
    // !!! data for the collection sport for each user
    val sports= mutableListOf<String>("Football", "Volleyball", "Basketball", "Tennis", "Cricket",
        "Baseball", "Rugby", "Hockey", "Padel", "Badminton")
    val allNickname=nickname.toMutableList()
    val allName=name.toMutableList()
    val allSurname=surname.toMutableList()
    val lFriends = mutableListOf<UserBasicInfo>()
    for (i in 1 .. 100){
        lFriends.add(
            UserBasicInfo(
            "@${allNickname.removeLast().toLowerCase()}",
            allName.removeLast(),
            allSurname.removeLast(),
            "beginner"
        )
        )
    }
    // level generated random. 20% = advanced, 45% = intermediate, 35% = beginner
    // var level= mutableListOf<String>("advanced", "intermediate", "intermediate", "beginner",
    // "beginner", "beginner","advanced", "intermediate", "intermediate", "beginner", "beginner", "beginner","advanced", "intermediate", "intermediate", "beginner", "beginner", "beginner","advanced", "intermediate", "intermediate", "beginner", "beginner", "beginner")
    if(name.size==friends.size && name.size==nickname.size && name.size==surname.size && name.size==nickname.size && name.size==age.size && name.size==city.size && name.size==bio.size && name.size==numberOfSports.size){
        for (i in 1..100){
            Log.d("SetUpData","Added user")
            val lSports = mutableListOf<Sport>()
            val sportsActive = mutableListOf<String>()
            for ( j in 1..numberOfSports.removeLast()){
                when((Math.random()*100).roundToInt()){ //1 + 4 + 9 + 16 + 25 + 36 + 49 + 64 + 81 + 100
                    // something like a leveraged quadratic probability to play a determinate sport.
                    in 82..100-> sportsActive.add(sports[0])
                    in 65..81 -> sportsActive.add(sports[1])
                    in 50..64 -> sportsActive.add(sports[2])
                    in 37..50 -> sportsActive.add(sports[3])
                    in 26..36 -> sportsActive.add(sports[4])
                    in 17..25 -> sportsActive.add(sports[5])
                    in 11..15 -> sportsActive.add(sports[6])
                    in 7.. 10 -> sportsActive.add(sports[7])
                    in 3..6   -> sportsActive.add(sports[8])
                    in 0..2   -> sportsActive.add(sports[9])
                }
            }
            sports.map {
                lSports.add(Sport(
                    it, when((Math.random()*100).roundToInt()){
                        in 0..20 -> "advanced"
                        in 21..65 -> "intermediate"
                        else -> "beginner"
                    },
                    0f,
                    0,
                    0,
                    0,
                    0,
                    0,
                    (it in sportsActive),
                    true // always visible but can be changed by user
                )) }
            val listaAmici = mutableListOf<UserBasicInfo>()
            (0 .. friends.removeLast()).map {
                listaAmici.add(lFriends[(Math.random()*99).roundToInt()])
            }
            db.collection("users").document("@${nickname.last().toLowerCase()}").set(
                User("@${nickname.removeLast().toLowerCase()}",
                    name.last(),
                    surname.last(),
                    age.last(),
                    city.removeLast(),
                    bio.removeLast(),
                    0f,
                    "${name.removeLast().toLowerCase()}.${surname.removeLast().toLowerCase()}${(2023-age.removeLast())}@gmail.com",
                    lSports,
                    link = null,
                    friends = listaAmici
                ))
        }
    } else{
        Log.d("Err SetUpData","Le liste hanno diverse lunghezze")
    }
}

/////////////////////



