package com.example.courtreservation.rateplaygrounds

import android.icu.util.Calendar
import android.text.format.DateFormat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.courtreservation.Playground
import com.example.courtreservation.R
import com.example.courtreservation.playgroundselector.SportSelector
import com.example.courtreservation.utils.ImageProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatePlaygroundsScreen(
    nickname: String,
    viewModel: RatePlaygroundsViewModel = viewModel(factory = RatePlaygroundsViewModel.Factory(nickname))
) {
    val playgroundsBySportWithPersonalRating by viewModel.playgroundsBySportWithPersonalRatings
        .observeAsState(emptyMap())
    var playgroundIdForComments: String? by remember { mutableStateOf(null) }
    val comments by viewModel.playgroundComments.observeAsState()

    if (playgroundIdForComments != null) {
        AlertDialog(onDismissRequest = { playgroundIdForComments = null }) {
            PlaygroundCommentsAlertContent(comments) { comment ->
                viewModel.addCommentToPlayground(playgroundIdForComments!!, comment)
            }
        }
    }

    SportSelector(sports = playgroundsBySportWithPersonalRating.keys.toList()) { sport ->
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val playgrounds = playgroundsBySportWithPersonalRating[sport]!!

            items(playgrounds.size) { playgroundIndex ->
                val p = playgrounds[playgroundIndex]

                PlaygroundCard(
                    p.second,
                    p.third ?: 0,
                    { newRating -> viewModel.setRatingForPlayground(p.first, newRating) }
                ) {
                    viewModel.startListeningToPlaygroundComments(p.first)
                    playgroundIdForComments = p.first
                }
            }
        }
    }
}

@Composable
private fun PlaygroundCard(
    playground: Playground,
    personalRating: Int,
    updatePlaygroundRating: (Int) -> Unit,
    showComments: () -> Unit
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

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(String.format("%.01f", playground.avg_rating), color = Color.Gray)
                    Icon(Icons.Filled.Star, null, tint = Color.Gray)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "${playground.num_players_per_team}v${playground.num_players_per_team}",
                        color = Color.Gray
                    )
                    Icon(Icons.Filled.Person, null, tint = Color.Gray)
                }
            }
        }

        // Actual rating gets flushed to the db only when the finger is released from the screen
        val rating = remember { Animatable(personalRating.toFloat()) }
        val starSize = remember { Animatable(32f) }
        val coroutine = rememberCoroutineScope()

        LaunchedEffect(personalRating) {
            rating.animateTo(
                personalRating.toFloat(),
                tween(500)
            )
        }

        Box(
            modifier = Modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutine.launch {
                            rating.snapTo(min(max(rating.value + delta.dp.div(128.dp), 0f), 5f))
                        }
                    })
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        coroutine.launch {
                            starSize.animateTo(64f)
                        }
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })

                        coroutine.launch {
                            updatePlaygroundRating(rating.targetValue.roundToInt())
                            starSize.animateTo(32f)
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    Image(
                        painter = painterResource(
                            if (index < rating.value.roundToInt())
                                R.drawable.baseline_star_24
                            else R.drawable.baseline_star_outline_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(starSize.value.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            IconButton(
                onClick = showComments,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .alpha(1.0f - (starSize.value - 32.0f) / 32.0f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("${playground.num_comments}")
                    Icon(Icons.Filled.Reviews, contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlaygroundCommentsAlertContent(
    comments: List<PlaygroundComment>?,
    addComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(comments?.size) {
        if (comments != null)
            listState.animateScrollToItem(comments.lastIndex)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        modifier = Modifier.fillMaxHeight(0.75f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (comments.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.weight(1.0f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\uD83D\uDCAC",
                        fontSize = 120.sp
                    )
                    Text(
                        text = "No review yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                ) {
                    items(comments.size) { commentIndex ->
                        PlaygroundComment(comments[commentIndex])
                    }
                }
            }

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it.replace("\n", "") },
                    modifier = Modifier
                        .weight(1.0f)
                        .onKeyEvent {
                            if (it.key == Key.Enter) {
                                addComment(commentText.trim())
                                commentText = ""
                                true
                            } else {
                                false
                            }
                        }
                    ,
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                IconButton(
                    enabled = commentText.isNotEmpty(),
                    onClick = {
                        addComment(commentText.trim())
                        commentText = ""
                    }
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                }
            }
        }
    }
}

private fun Timestamp.formatToString(): String {
    val date = Calendar.getInstance().apply {
        time = toDate()
    }
    return DateFormat.format("dd/MM/yyyy - HH:mm", date.time).toString()
}

@Composable
private fun PlaygroundComment(comment: PlaygroundComment) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = comment.author_nickname,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = comment.timestamp.formatToString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 16.dp,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = comment.comment,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}