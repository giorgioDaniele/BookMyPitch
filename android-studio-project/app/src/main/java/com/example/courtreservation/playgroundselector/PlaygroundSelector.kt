package com.example.courtreservation.playgroundselector

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SportSelector(
    sports: List<String>,
    sportPage: @Composable (String) -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(sports.size) {
        if (pagerState.currentPage >= sports.size) pagerState.scrollToPage(0)
    }

    if (sports.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shadowElevation = 3.dp,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.height(65.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val backEnabled = pagerState.currentPage > 0
                    val forwardEnable = pagerState.currentPage < sports.size - 1
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp

                    IconButton(
                        onClick = { coroutine.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        enabled = backEnabled
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = if (backEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.surfaceColorAtElevation(64.dp)
                        )
                    }
                    Text(
                        text = sports.getOrElse(pagerState.currentPage) { "" },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .offset(x = -screenWidth * pagerState.currentPageOffsetFraction)
                            .alpha(1 - abs(pagerState.currentPageOffsetFraction) * 2)
                    )
                    IconButton(
                        onClick = { coroutine.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        enabled = forwardEnable
                    ) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (forwardEnable) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.surfaceColorAtElevation(64.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(sports.size, state = pagerState) { pageIndex ->
                    sportPage(sports[pageIndex])
                }

                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(sports.size) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceColorAtElevation(64.dp)

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                                .clickable {
                                    coroutine.launch {
                                        pagerState.animateScrollToPage(iteration)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}