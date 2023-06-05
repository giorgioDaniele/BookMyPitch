package com.example.courtreservation.profile

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.courtreservation.Sport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


@Composable
fun EditSportCard(vm: ProfileViewModel,sport: Sport) {
    var visible by remember {
        mutableStateOf(sport.visible)
    }
    Card(colors = cardColorsScheme(), modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(1f / 5f), horizontalAlignment = Alignment.CenterHorizontally) {
            CardTitle(txt = sport.name)
            LevelSpinner(list = listOf("advanced", "intermediate", "beginner"), preselected = sport.level, vm, sport)
            if (visible){
                Button( colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                ) , onClick = { visible=false
                    CoroutineScope(IO).launch {
                        vm.updateSports(Sport(
                            name=sport.name,
                            level = sport.level,
                            rating = sport.rating,
                            victories = sport.victories,
                            defeats = sport.defeats,
                            meets = sport.meets,
                            like = sport.like,
                            dislike = sport.dislike,
                            active = sport.active,
                            visible = false
                        ))
                    }
                     }) {
                    Text(text = "Hide sport", color = Color.Black )
                }
            } else{
                Button( colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                ) , onClick = { visible=true
                        CoroutineScope(IO).launch {
                            vm.updateSports(Sport(sport.name, sport.level, sport.rating,
                                sport.victories, sport.defeats, sport.meets, sport.like, sport.dislike, active = sport.active,visible = true)
                            )
                        }
                    }) {
                    Text(text = "Show sport", color = Color.Black )
                }
            }
        }
    }
}

@Composable
fun LevelSpinner(
    list: List<String>,
    preselected: String,
    vm: ProfileViewModel,
    sport: Sport,
    modifier: Modifier = Modifier
) {

    var selected by remember { mutableStateOf (preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value

    OutlinedCard(
        modifier = modifier.clickable {
            expanded = !expanded
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {

            Text(
                text = selected,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.padding(8.dp))

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                list.forEach { listEntry ->

                    DropdownMenuItem(
                        onClick = {
                            selected = listEntry
                            expanded = false
                            Log.d("Spinner","Clicked")
                            CoroutineScope(IO).launch {
                                vm.updateSports(Sport(sport.name, selected, sport.rating,
                                    sport.victories, sport.defeats, sport.meets, sport.like,
                                    sport.dislike, visible = sport.visible, active =  true))
                            }
                        },
                        text = {
                            Text(
                                text = listEntry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Start)
                            )
                        },
                    )
                }
            }

        }
    }
}
