package com.example.courtreservation.show_edit_reservations.documents

import android.os.Parcelable
import com.example.courtreservation.show_edit_reservations.documents.PlaygroundDocument
import com.example.courtreservation.show_edit_reservations.documents.ReservationDocument
import com.example.courtreservation.utils.ImageProvider
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Reminder (
    val author: String,
    val status: String,
    val reservationID: String,
    val playgroundID: String,
    val customRequest: String,
    val hour: Long,
    val day: Long,
    val month: Long,
    val year: Long,
    val courtName: String,
    val sportName: String,
    val image: Int,
    val team0: List<String>,
    val team1: List<String>) : Parcelable {

    companion object {

        fun Pair<List<ReservationDocument>, List<PlaygroundDocument>>.toReminder(): List<Reminder> {
            return this.first.map { reservation ->
                val playground =
                    this.second.find { playground -> reservation.playgroundID == playground.id }
                reservation to playground
            }.map {
                Reminder(
                    it.first.author,
                    it.first.status,
                    it.first.id,
                    it.second?.id ?: "",
                    it.first.customRequest ?: "",
                    it.first.hour,
                    it.first.day,
                    it.first.month + 1,
                    it.first.year,
                    (it.second?.name  ?: ""),
                    (it.second?.sport ?: ""),
                    ImageProvider.provide(it.second?.let { item -> item.sport } ?: ""),
                    it.first.team0,
                    it.first.team1)
            }
        }
    }
}