package com.example.courtreservation.show_edit_reservations.documents

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ReservationDocument(
    val id: String,
    val customRequest: String?,
    val day: Long,
    val hour: Long,
    val month: Long,
    val playgroundID: String,
    val status: String,
    val author: String,
    val team0: List<String>,
    val team1: List<String>,
    val year: Long) : Parcelable {
        companion object {
            private const val TAG = "Reservation"
            fun DocumentSnapshot.toReservationDocument() : ReservationDocument? {
                return try {
                    val id = id
                    val customRequest = getString("customRequest")
                    val day           = getLong("day")
                    val hour          = getLong("hour")
                    val month         = getLong("month")
                    val playgroundID  = getString("playgroundId")
                    val status        = getString("status")
                    val team0         = get("team_0") as? List<*>
                    val team1         = get("team_1") as? List<*>
                    val year          = getLong("year")
                    ReservationDocument(
                        id            = id,
                        customRequest = customRequest,
                        day           = day   ?: 1,
                        hour          = hour  ?: 8,
                        month         = month ?: 0,
                        playgroundID  = playgroundID ?: "",
                        status        = status ?: "",
                        author        = (team0?.get(0) ?: "") as String,
                        team0         = team0 as List<String>,
                        team1         = team1 as List<String>,
                        year          = year ?: 2023)
                }catch (e: Exception) {
                    Log.d(TAG, "Error on converting reservation document", e)
                    return null
                }
            }
        }
    }