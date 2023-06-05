package com.example.courtreservation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.courtreservation.R
import com.example.courtreservation.Sport
import com.example.courtreservation.show_edit_reservations.padding


val cardColorsScheme : (@Composable () -> CardColors) = {
    CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
fun CardTitle(txt: String) {
    Text(text = txt.uppercase(),
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp,16.dp,16.dp,0.dp)
    )
}
/*
@Composable
fun SportCard(sport: Sport) {

    Card(colors = cardColorsScheme(), modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(1f / 5f), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
                /*Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.BottomStart){
                    androidx.compose.material.Icon(
                        Icons.Filled.Star,
                        "star icon",
                        tint = Color(0xFFFFF000),
                        modifier = Modifier.size(30.dp)
                    )
                    Box(modifier = Modifier.padding(30.dp,22.dp,110.dp,0.dp)){
                        Text(text = sport.rating.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }*/
                CardTitle(txt = sport.name)

            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Box(modifier = Modifier.padding(8.dp)) {
                Image(painter = painterResource(id = when(sport.level){
                    "advanced"-> R.drawable.advanced
                    "intermediate"->R.drawable.intermediate
                    "beginner"->R.drawable.beginner
                    else->R.drawable.beginner
                }), contentDescription = "level", modifier = Modifier
                    .size(60.dp)
                    .fillMaxWidth(1f / 4f))
            }
            Column(modifier = Modifier.fillMaxWidth(3f/4f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxHeight(1f/2f)){
                    Box(modifier = Modifier.padding(12.dp,0.dp,0.dp,0.dp), contentAlignment = Alignment.CenterStart) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = sport.victories.toString(), style = MaterialTheme.typography.bodyLarge)
                            Text(text = "victories", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Box(modifier = Modifier.padding(12.dp,0.dp,0.dp,0.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = sport.defeats.toString(), style = MaterialTheme.typography.bodyLarge)
                            Text(text = "defeats", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Box(modifier = Modifier.padding(12.dp,0.dp,0.dp,0.dp), contentAlignment = Alignment.CenterEnd) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = sport.meets.toString(), style = MaterialTheme.typography.bodyLarge)
                            Text(text = "meets", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxHeight(1f/2f)){
                    Box(modifier = Modifier.padding(22.dp,14.dp,40.dp,14.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = R.drawable.up), contentDescription = "likes",
                                modifier = Modifier.size(25.dp))
                            Text(text = sport.like.toString(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Box(modifier = Modifier.padding(12.dp,14.dp,0.dp,14.dp), contentAlignment = Alignment.CenterEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = R.drawable.down), contentDescription = "dislikes",
                                modifier = Modifier.size(25.dp))
                            Text(text = sport.dislike.toString(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

}
*/

@Composable
fun LogoWithBackground(level: String) {
    val gradientColors = listOf(
        Color(0xFFDBB247), // Start color
        Color(0xFFC4972F), // Middle color
        Color(0xFFA6751A)  // End color
    )
    val gradientBrush = Brush.verticalGradient(gradientColors)
    Box(
        modifier = Modifier
            .size(size = 100.dp)
            .clip(shape = CircleShape)
            .background(Color(
                when(level){
                    "advanced"-> 0xFFFFD700
                    "intermediate"->0xFFC0C0C0
                    "beginner"->0xFFCD7F32
                    else->0xFFCD7F32}
            )), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = when(level){
                "advanced"-> R.drawable.advanced
                "intermediate"->R.drawable.intermediate
                "beginner"->R.drawable.beginner
                else->R.drawable.beginner} ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(70.dp)
        )
    }
}

@Composable
fun SportCard(sport: Sport) {

    Card(colors =     CardDefaults.cardColors(
        containerColor = Color(0x68E2E5E6),
        contentColor   = MaterialTheme.colorScheme.onPrimaryContainer),
        modifier =  padding(16, 16), elevation = cardElevation()){
        Box(modifier= Modifier.fillMaxWidth(),contentAlignment = Alignment.TopCenter) {
            CardTitle(txt = sport.name)
        }
        /*
        Image(painter = painterResource(id = reminder.image), contentDescription = null,
            modifier = cardPictureStyle(), contentScale = ContentScale.Crop)*/
        //HEADER
        Row {
            Column(modifier = Modifier
                .padding(16.dp)//,0.dp,0.dp,0.dp)
                .weight(0.4f)) {
                //CardTitle(txt = sport.name)
            LogoWithBackground(sport.level)
            }

            Column(modifier = Modifier
                .weight(0.7f)
                .padding(top = 30.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Row(modifier = Modifier.fillMaxHeight(1f/2f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sport.victories.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                            )
                            Text(
                                text = "Victories",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sport.defeats.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Defeats",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sport.meets.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Meets",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxHeight(1f/2f)) {

                    Box(contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = R.drawable.up), contentDescription = "likes",
                                modifier = Modifier.size(25.dp))
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text(
                                text = sport.like.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 16.dp))

                    Box(contentAlignment = Alignment.CenterEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = R.drawable.down), contentDescription = "dislikes",
                                modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text(
                                text = sport.dislike.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

        }
    }
}