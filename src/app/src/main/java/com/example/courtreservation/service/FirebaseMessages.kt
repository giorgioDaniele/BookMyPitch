package com.example.courtreservation.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.courtreservation.R
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


class FirebaseMessages: FirebaseMessagingService() {


    companion object {

        var token     : String? = null
        const val key : String = "AAAA_97lQis:APA91bEuc7qX0K-AeygPfquKDm3DhM_bG_n0iXnfvo6RjAQm9gfJBCDxO-o5FLBROFTzsGkOp74RzyAhSvH4-VmrxwbbYXARRRTUAeB6y8q2BNw14YdWCrTvKEAzxM-rd6hiXLAMNL_e"

        fun subscribeTopic(context: Context, topic: String) {

            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener {
                //Toast.makeText(context, "Subscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                //Toast.makeText(context, "Failed to Subscribe $topic", Toast.LENGTH_LONG).show()
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tokenFromServer = task.result
                    // Save the token or use it for subscription
                    Log.d("TOKEN", tokenFromServer)
                    token = tokenFromServer
                }
            }
        }
        fun unsubscribeTopic(context: Context, topic: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener {
                Toast.makeText(context, "Unsubscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Unsubscribe $topic", Toast.LENGTH_LONG).show()
            }
        }

        fun sendMessage(data: Map<String, Any>, topic: String) {

            GlobalScope.launch(Dispatchers.IO) {

                // HTTP REQUESTS
                if(topic == "messages") {
                    val url = "https://fcm.googleapis.com/fcm/send"

                    val sender      : String       = data["from"] as String
                    val recipients  : List<String> = data["to"] as List<String>
                    val textMessage : String       = data["text"] as String

                    val place : String = data["where"] as String
                    val time : String  = data["when"] as String

                    val json = JSONObject()
                    json.put("to", "/topics/$topic")
                    json.put("data", JSONObject(
                        mapOf(
                            "sender"     to sender,
                            "recipients" to JSONArray(recipients),
                            "text"       to textMessage,
                            "where" to place,
                            "when" to time,
                            "topic" to "messages")))

                    Log.d("RESULT", json.toString())

                    val mediaType = "application/json".toMediaType()
                    val requestBody = json.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "key=$key")
                        .post(requestBody)
                        .build()

                    val client = OkHttpClient()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        Log.d("RESULT", "Notification sent successfully")
                    } else {
                        Log.d("RESULT", "Error: ${response.body.string()}")
                    }
                }
                if(topic == "reservations") {
                    val url = "https://fcm.googleapis.com/fcm/send"

                    Log.d("NOTIFICATIONS", data.toString())

                    val who   : String  = data["who"]   as String
                    val time  : String  = data["when"]  as String
                    val place : String  = data["where"] as String
                    val level : String  = data["level"] as String
                    val sport : String  = data["sport"] as String

                    val json = JSONObject()
                    json.put("to", "/topics/$topic")
                    json.put("data", JSONObject(
                        mapOf(
                            "who"      to who,
                            "time"     to time,
                            "place"    to place,
                            "level"    to level,
                            "sport"    to sport,
                            "topic"    to "reservations")))

                    val mediaType = "application/json".toMediaType()
                    val requestBody = json.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "key=$key")
                        .post(requestBody)
                        .build()

                    val client = OkHttpClient()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        Log.d("RESULT", "Notification sent successfully")
                    } else {
                        Log.d("RESULT", "Error: ${response.body.string()}")
                    }

                }
            }
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        // Extract the message data
        val data : MutableMap<String, String> = remoteMessage.data

        val db = FirebaseFirestore.getInstance()
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("profile", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("nickname", "")?: ""

        if(data.getValue("topic") == "messages") {
            if(userName != data.getValue("sender") &&
                data.getValue("recipients").contains(userName)) {
                showNotification("${data.getValue("where")}, ${data.getValue("when")}", data.getValue("text"))
            }
        }
        if(data.getValue("topic") == "reservations") {
            if(userName != data.getValue("who")) {

                GlobalScope.launch {

                    // LOOK FOR THE SPORTS MAP
                    val sportsMap : List<Map<String, Any>> = db
                        .collection("users")
                        .whereEqualTo("nickname", userName)
                        .get().await().documents.first()
                        .data
                        ?.get("sports") as List<Map<String, Any>>

                    // GET THE STATS FOR SUCH SPORT
                    val sportStats = sportsMap.find {
                        it.getValue("name") == data.getValue("sport") }

                    if(sportStats != null) {
                        // NOTIFY IF AND IF ONLY THE SENDER IS NOT ME
                        // AND I AM THE AT THE SAME LEVEL
                        if(sportStats["level"] == data.getValue("level") && sportStats["active"] == true) {
                            showNotification("${data.getValue("place")}, ${data.getValue("time")}",
                                "New ${data.getValue("sport")} event")
                        }
                    }
                }
            }
        }
    }

    // Function to show a notification
    fun showNotification(title: String, message: String) {

        // Notification channel ID
        val CHANNEL_ID = "CHANNEL_ID"

        // Notification ID
        val NOTIFICATION_ID = 1

        // Create a notification channel (required for Android 8.0 and above)
        createNotificationChannel(applicationContext)

        // Create a notification builder
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.application_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }


    // Function to create a notification channel
    private fun createNotificationChannel(context: Context) {
        // Check if the device is running Android 8.0 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel
            val channel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_ID",
                NotificationManager.IMPORTANCE_HIGH
            )

            // Optionally, you can customize the notification channel settings
            // For example, you can set the description, sound, vibration pattern, etc.
            // channel.description = "My Channel Description"

            // Register the notification channel with the system
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(p0: String) {
        // Get updated InstanceID token.
        token = p0
        super.onNewToken(p0)

    }

}