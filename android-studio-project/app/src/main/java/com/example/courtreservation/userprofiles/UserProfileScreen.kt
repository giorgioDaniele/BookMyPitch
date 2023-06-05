package com.example.courtreservation.userprofiles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.courtreservation.R
import com.example.courtreservation.User
import com.example.courtreservation.UserBasicInfo

@Composable
fun UserProfileScreen(
    username: String,
    vm: UserProfileViewModel = hiltViewModel(),
) {
    val user: User? by remember { mutableStateOf(null) }
    //var sports by remember { mutableStateOf(emptyList<Sport>()) }
    LaunchedEffect(user, username) {
            vm.getUserByUsername(username)
    }

    if(vm.user.value!=null){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
        LazyColumn {
            item {
                Profile(
                    vm,
                    modifier = Modifier
                        .fillMaxHeight(1f/4f)
                )
            }
            items(vm.user.value!!.sports) { sport ->
                if (sport.visible && sport.active) {
                    SportCard(sport, vm)
                }
            }
        }
    }} else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "The username does not exist", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun Profile(
    vm: UserProfileViewModel,
    modifier: Modifier
){
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF9575CD),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
                Color(0xFFFFB74D),
                Color(0xFFFFF176),
                Color(0xFFAED581),
                Color(0xFF4DD0E1),
                Color(0xFF9575CD)
            )
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier=modifier.fillMaxSize()
    ){

        Image(
            painter =  if(vm.user.value!!.link!=null){
                rememberAsyncImagePainter(vm.user.value!!.link)
            } else{
                painterResource(id = R.drawable.default_profile_image)
            },
            contentScale = ContentScale.Crop,
            contentDescription = "Profile photo",
            modifier = Modifier
                //.fillMaxSize()
                .size(150.dp)
                .border(
                    BorderStroke(4.dp, rainbowColorsBrush),
                    CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
        )
        Box(
            modifier
                .padding(70.dp, 0.dp, 0.dp, 0.dp)
                .scale(1.4f),
            contentAlignment = Alignment.Center,

            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    "star icon",
                    tint = Color(0xFFFFF000),
                    modifier = Modifier.size(55.dp))
                Text(text = String.format("%.1f",vm.user.value!!.rating), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
    ProfileInfo(vm, modifier = Modifier)
}
@Composable
fun ProfileInfo(
    vm: UserProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Text(text = vm.user.value!!.nickname, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${vm.user.value!!.name} ${vm.user.value!!.surname}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${vm.user.value!!.age} years old, ${vm.user.value!!.city}", style = MaterialTheme.typography.bodyLarge)
        Text(text = vm.user.value!!.bio, style = MaterialTheme.typography.bodyMedium)
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp), contentAlignment = Alignment.Center) {
            if (vm.actualUser.value!!.friends == null)
                Button(modifier = Modifier.padding(0.dp,0.dp,0.dp,0.dp), onClick = { vm.addFriend(
                    UserBasicInfo(vm.user.value!!.nickname,vm.user.value!!.name,vm.user.value!!.surname,"beginner",0.0f, link = vm.user.value!!.link)
                )} ) {
                    Text(text = "Add to friends", style = MaterialTheme.typography.bodyMedium)
                }
            else if(vm.actualUser.value!!.friends?.any{ it.nickname == vm.user.value!!.nickname }!!)
                Button(modifier = Modifier.padding(0.dp,0.dp,0.dp,0.dp), onClick = {
                    vm.removeFriend(vm.user.value!!.nickname)
                } ) {
                    Text(text = "Remove from friends", style = MaterialTheme.typography.bodyMedium)
                }
            else
                Button(modifier = Modifier.padding(0.dp,0.dp,0.dp,0.dp), onClick = { vm.addFriend(
                    UserBasicInfo(vm.user.value!!.nickname,vm.user.value!!.name,vm.user.value!!.surname,"beginner",0.0f, link = vm.user.value!!.link)
                )} ) {
                    Text(text = "Add to friends", style = MaterialTheme.typography.bodyMedium)
                }
        }
    }
}
