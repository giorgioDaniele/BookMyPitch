package com.example.courtreservation.addreservations

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.courtreservation.Playground
import com.example.courtreservation.Reservation
import com.example.courtreservation.User
import com.example.courtreservation.playgroundselector.SportSelector
import com.example.courtreservation.show_edit_reservations.TransactionResultDialog
import com.example.courtreservation.utils.Converters
import com.example.courtreservation.utils.ImageProvider
import kotlinx.coroutines.launch

@Composable
fun AddReservationsScreen(
    nickname: String,
    viewModel: PlaygroundSelectorViewModel = viewModel(
        factory = PlaygroundSelectorViewModel.Factory(nickname)
    ),
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToBrowseAvailabilityScreen: (String, String, String, String) -> Unit,
    onNavigateToProfileScreen: () -> Unit
) {
    val sports by viewModel.playgroundsByActiveSport.observeAsState(mapOf())
    val joinableReservations by viewModel.joinableReservations.observeAsState(listOf())
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(joinableReservations.map {
        it.first.third.currentState to it.first.third.targetState
    }) {
        for ((res, _, _) in joinableReservations) {
            if (!res.third.currentState && !res.third.targetState) {
                coroutineScope.launch {
                    viewModel.commitAddSelfToReservationTeam(res.first)
                }
            }
        }
    }

    val dialogState by viewModel.dialogState.observeAsState()
    when (dialogState) {
        BrowseAvailabilityViewModel.ProgressDialogState.SUCCESS -> TransactionResultDialog("SUCCESS!")
        BrowseAvailabilityViewModel.ProgressDialogState.FAILURE -> TransactionResultDialog("ERROR!")
        else -> { }
    }

    if (sports.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83C\uDFD1",
                fontSize = 120.sp
            )
            Text(
                text = "Feels too empty in here...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            TextButton(
                onClick = onNavigateToProfileScreen,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Go to my profile")
            }
        }
    } else {
        SportSelector(sports.keys.toList()) { sport ->
            val playgrounds = sports[sport]!!
            val joinableReservationsForSport = joinableReservations
                .filter {
                    it.second.sport.equals(sport, true)
                }

            LazyColumn(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (joinableReservationsForSport.isNotEmpty()) {
                    val showHeaders = joinableReservationsForSport.any {
                        it.first.third.targetState
                    }

                    item {
                        AnimatedVisibility(
                            visible = showHeaders,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Join a match",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    items(joinableReservationsForSport.size) { reservationIndex ->
                        AnimatedVisibility(
                            visibleState = joinableReservationsForSport[reservationIndex].first.third,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            val res = joinableReservationsForSport[reservationIndex]
                            if (reservationIndex > 0) Divider()
                            JoinableReservationCard(
                                res.first.second,
                                res.first.first,
                                res.second,
                                res.third,
                                onNavigateToUserProfile
                            ) { reservationId, teamField ->
                                viewModel.registerAddSelfToReservationTeam(reservationId, teamField)
                            }
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = showHeaders,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Create a new reservation",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                items(playgrounds.first.size) { playgroundIndex ->
                    PlaygroundCard(playgrounds.first[playgroundIndex].second) {
                        onNavigateToBrowseAvailabilityScreen(
                            playgrounds.first[playgroundIndex].first,
                            sport,
                            playgrounds.second,
                            playgrounds.first[playgroundIndex].second.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaygroundCard(
    playground: Playground,
    onAddReservation: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(ImageProvider.provide(playground.sport)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playground.name,
                fontSize = 23.sp
            )
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(String.format("%.01f", playground.avg_rating), color = Color.Gray)
                    Icon(Icons.Filled.Star, null, tint = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${playground.num_players_per_team}v${playground.num_players_per_team}", color = Color.Gray)
                    Icon(Icons.Filled.Person, null, tint = Color.Gray)
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RectangleShape,
            onClick = { onAddReservation() }
        ) {
            Text(text = "Add Reservation: €" + String.format("%.02f", playground.cost))
        }
    }
}

@Composable
fun JoinableReservationCard(
    reservation: Reservation,
    reservationId: String,
    playground: Playground,
    author: User,
    onNavigateToUserProfile: (String) -> Unit,
    onAddSelfToReservationTeam: (String, String) -> Unit
) {
    val formattedTime = remember(reservation) {
        val date = Converters.createDate(reservation.year, reservation.month, reservation.day, reservation.hour)
        DateFormat.format("dd MMMM", date.time).toString() + ", " + Converters.dateToString(date)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Image(
                painter = painterResource(ImageProvider.provide(playground.sport)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .border(border = BorderStroke(1.dp, Color.DarkGray), shape = CircleShape)
                    .clip(CircleShape)
            )

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = playground.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = (author.sports.find { it.name.equals(playground.sport, true) }?.level ?: "").capitalize(Locale.current)
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayerList(
                reservation.team_0,
                Alignment.Start,
                Modifier.weight(1.0f),
                reservation.team_0.size < playground.num_players_per_team,
                onNavigateToUserProfile
            ) {
                onAddSelfToReservationTeam(reservationId, "team_0")
            }

            Text(
                text = "VS",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            PlayerList(
                reservation.team_1,
                Alignment.End,
                Modifier.weight(1.0f),
                reservation.team_1.size < playground.num_players_per_team,
                onNavigateToUserProfile
            ) {
                onAddSelfToReservationTeam(reservationId, "team_1")
            }
        }
    }
}

@Composable
fun PlayerList(
    players: List<String>,
    alignment: Alignment.Horizontal,
    modifier: Modifier,
    showJoinButton: Boolean,
    onNavigateToUserProfile: (String) -> Unit,
    onJoin: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        for (player in players) {
            Text(
                player,
                modifier = Modifier.clickable {
                    onNavigateToUserProfile(player)
                }
            )
        }

        if (showJoinButton) {
            TextButton(onClick = onJoin, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = "Join Team",
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}