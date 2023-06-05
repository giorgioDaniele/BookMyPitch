package com.example.courtreservation.show_edit_reservations.documents

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaygroundDocument(
    val id: String,
    val name: String,
    val sport: String) : Parcelable {

    companion object {
        private const val TAG = "Playground"
        fun DocumentSnapshot.toPlaygroundDocument(): PlaygroundDocument? {
            return try {
                val id = id
                val name = getString("name")
                val sport = getString("sport")
                PlaygroundDocument(id, name ?: "", sport ?: "")
            } catch (e: Exception) {
                Log.d(TAG, "Error on converting playground document", e)
                null
            }
        }
    }
}