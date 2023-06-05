package com.example.courtreservation.show_edit_reservations.documents

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationDocument(
    val id: String,
    val reservationId: String,
    val from: String,
    val to: List<String>,
    val message: String,
    val timestamp: String) : Parcelable {

    companion object {
        private const val TAG = "Notification"
        fun DocumentSnapshot.toNotificationDocument(): NotificationDocument? {
            return try {
                val id = id
                val reservationId = getString("reservationId")
                val from = getString("from")
                val to = get("to") as List<*>
                val message = getString("message")
                val timestamp = getString("timestamp")
                NotificationDocument(id, reservationId ?: "", from ?: "",
                    to as List<String>, message ?: "", timestamp ?: "")
            } catch (e: Exception) {
                Log.d(TAG, "Error on converting playground document", e)
                null
            }
        }
    }
}