package com.example.courtreservation.utils

import android.icu.util.Calendar
import com.example.courtreservation.R

class Converters {

    companion object {

        fun createDate (year: Int, month: Int, day: Int, hour: Int) : Calendar = Calendar.getInstance().apply {
            this.set(Calendar.DAY_OF_MONTH, day)
            this.set(Calendar.MONTH, month)
            this.set(Calendar.YEAR, year)
            this.set(Calendar.HOUR_OF_DAY, hour)
            this.set(Calendar.MINUTE, 0)
            this.set(Calendar.SECOND, 0)
        }

        fun dateToString(date: Calendar) =
            "${String.format("%02d", date.get(Calendar.HOUR_OF_DAY))}:00 - ${String.format("%02d", date.get(
                Calendar.HOUR_OF_DAY) + 1)}:00"

    }
}

class ImageProvider {
    companion object {

        fun provide(sportName: String): Int {
//            Log.d("SPORT", sportName)
            if (sportName.uppercase() == "TENNIS") return R.drawable.tennisball
            if (sportName.uppercase() == "BASKETBALL") return R.drawable.basketball
            if (sportName.uppercase() == "VOLLEYBALL") return R.drawable.volleyball
            if (sportName.uppercase() == "FOOTBALL") return R.drawable.soccerball
            if (sportName.uppercase() == "BASEBALL") return R.drawable.base_ball
            if (sportName.uppercase() == "PADEL") return R.drawable.padel_ball
            if (sportName.uppercase() == "CRICKET") return R.drawable.cricket_ball
            if (sportName.uppercase() == "HOCKEY") return R.drawable.hockey_ball
            if (sportName.uppercase() == "RUGBY") return R.drawable.rugby_ball
            if (sportName.uppercase() == "BADMINTON") return R.drawable.badminton_ball
            return R.drawable.ic_launcher_foreground
        }
    }
}
