package com.example.courtreservation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
import com.example.courtreservation.accessview.AccessViewModel

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel(),
    accessVM: AccessViewModel,
    onNavigateToEditProfileScreen: () -> Unit
) {
    if(vm.user.value!=null){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn {
                item { Profile(
                    onNavigateToEditProfileScreen,
                    vm,
                    modifier = Modifier
                        .fillMaxHeight(1f/4f)
                ) }
                items(vm.user.value!!.sports) { sport ->
                    if (sport.visible && sport.active) {
                        SportCard(sport)
                    }
                }
                item{LogoutButton(vm, accessVM)}
            }
        }
    }

}

@Composable
fun Profile(
    onNavigateToEditProfileScreen: () -> Unit,
    vm: ProfileViewModel,
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
    ProfileInfo(onNavigateToEditProfileScreen, vm, modifier = Modifier)
}

@Composable
fun ProfileInfo(
    onNavigateToEditProfileScreen: () -> Unit,
    vm: ProfileViewModel,
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
            Button(modifier = Modifier.padding(0.dp,0.dp,0.dp,0.dp), onClick = onNavigateToEditProfileScreen,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary)
                ) {
                Text(text = "Edit profile", style = MaterialTheme.typography.bodyMedium, color= Color.White)
            }
        }
    }
}

@Composable
fun LogoutButton(vm: ProfileViewModel, accessVM: AccessViewModel){
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp), contentAlignment = Alignment.Center) {
        Button(modifier = Modifier.padding(18.dp,0.dp,0.dp,0.dp),onClick = {
            vm.resetNickname()
            accessVM.logout() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(223, 71, 89)))
            {
            Text(text = "Logout", style = MaterialTheme.typography.bodyMedium)
        }
    }
}




/*@Composable
fun Sports(vm:ProfileViewModel){
    LazyColumn {
        items(vm.user.value!!.sports) { sport ->
            if (sport.visible && sport.active) {
                SportCard(sport)
            }
        }
    }
}
*/