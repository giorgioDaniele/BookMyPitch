package com.example.courtreservation.show_edit_reservations

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(
                    TextStyle.SHORT, Locale.getDefault())
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    selectedDate: LocalDate?,
    updateSelectedDay: (LocalDate) -> Unit,
    daysWithEvents: List<LocalDate>,
    disablePastDays: Boolean = false
) {

    val currentMonth    = remember { YearMonth.now() }
    val startMonth      = remember { currentMonth.minusMonths(100) }
    val endMonth        = remember { currentMonth.plusMonths(100) }
    val daysOfWeek      = remember { daysOfWeek() }
    val coroutineScope = rememberCoroutineScope()


    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )


    HorizontalCalendar(
        state = state,
        monthHeader = { MonthHeader(it, daysOfWeek, {
            coroutineScope.launch {
                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
            }
        }, {
            coroutineScope.launch {
                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
            }
        }) },
        monthContainer = { _, container ->
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            Box(
                modifier = Modifier
                    .width(screenWidth)
                    .padding(bottom = 0.dp)) {
                container()
            }
        },
        monthBody = {_, content ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .clipScrollableContainer(Orientation.Vertical)
                .verticalScroll(rememberScrollState())) {
                content()
            }
        },
        dayContent = {
            Day(
                it,
                selectedDate == it.date.toKotlinLocalDate(),
                updateSelectedDay,
                daysWithEvents,
                disablePastDays
            )
        },
    )
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthHeader(month: CalendarMonth, daysOfWeek: List<DayOfWeek>, scrollBehind: () -> Unit, scrollAhead: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shadowElevation = 3.dp,
        tonalElevation = 3.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = scrollBehind) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                            + " '" + month.yearMonth.year.toString().substring(2),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = scrollAhead) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            }
            Divider(thickness = 1.dp)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    updateSelectedDay: (LocalDate) -> Unit,
    daysWithEvents: List<LocalDate>,
    disablePastDays: Boolean
) {
    val backgroundColor = if (day.date == java.time.LocalDate.now()) MaterialTheme.colorScheme.secondary
    else if (daysWithEvents.contains(day.date.toKotlinLocalDate())) Color.Yellow
    else Color.Transparent

    val textColor = if (day.date == java.time.LocalDate.now()) MaterialTheme.colorScheme.onSecondary
    else MaterialTheme.colorScheme.onPrimaryContainer

    val enabled = day.position == DayPosition.MonthDate &&
            (!disablePastDays || day.date >= java.time.LocalDate.now())

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = { updateSelectedDay(day.date.toKotlinLocalDate()) }
            )
            .then(
                if (isSelected) Modifier.border(
                    border = BorderStroke(1.dp, Color.DarkGray),
                    shape = CircleShape
                )
                else Modifier
            ),
        contentAlignment = Alignment.Center) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (enabled) textColor else Color.Gray
        )
    }
}