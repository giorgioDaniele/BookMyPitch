package com.example.courtreservation.userprofiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.courtreservation.R
import com.example.courtreservation.UserBasicInfo
import com.example.courtreservation.profile.ProfileViewModel

@Composable
fun FriendsScreen(
    vm: UserProfileViewModel = hiltViewModel(),
    vmProfile: ProfileViewModel = hiltViewModel(),
    onNavigateToUserProfile: (String) -> Unit
) {
    val textSearch   = remember { mutableStateOf("") }

    /*LaunchedEffect(key1 = textSearch.value, block = {
        if(vm.textInput.value!=""){
            CoroutineScope(Dispatchers.IO).launch{
                vm.searchUsers()
            }
        }
    })*/
    Column {
        OutlinedTextField(
            value = textSearch.value,
            onValueChange = { text ->
                textSearch.value = text
                vm.textInput.value = text
                vm.searchUsers()
            },
            label = { Text("Search") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                /*.onKeyEvent {
                    if (it.key == Key.Enter) {
                        vm.nickname.value = "@${textSearch.value.trim()}"
                        onNavigateToUserProfile()
                        true
                    } else {
                        false
                    }
                }*/
        )
//
//        Text(
//            text = error.value, color = Color.Red,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.align(Alignment.CenterHorizontally))
        Divider(modifier = Modifier.height(2.dp))
        if (vm.users.value.isEmpty() && vm.textInput.value.isNotEmpty()){
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No matches", style = MaterialTheme.typography.titleLarge)
            }
        }
        else{
            LazyColumn(
                contentPadding = PaddingValues(16.dp)
            ) {
                if (vm.users.value.isNotEmpty() && vm.textInput.value.isNotEmpty()) {
                    items(vm.users.value){user ->
                        FriendProfileCard(user, onNavigateToUserProfile)
                    }
                }

                else if(vmProfile.user.value?.friends != null && vm.textInput.value == ""){
                    items(vmProfile.user.value!!.friends!!) { friend ->
                        FriendProfileCard(friend, onNavigateToUserProfile)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendProfileCard(
    friend: UserBasicInfo,
    onNavigateToUserProfile: (String) -> Unit
) {
    Row(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .clickable {
            onNavigateToUserProfile(friend.nickname)
        }
        , verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter =  if(friend.link!=null){
                rememberAsyncImagePainter(friend.link)
            } else{
                painterResource(id = R.drawable.default_profile_image)
            },
            contentScale = ContentScale.Crop,
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(55.dp)
                .aspectRatio(1f)
                .clip(CircleShape)

        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "${friend.name} ${friend.surname}", style = MaterialTheme.typography.titleLarge)
            Text(text = friend.nickname, style = MaterialTheme.typography.bodyMedium)
            //Text(text = "Rating: ${friend.rating}", style = MaterialTheme.typography.titleLarge)
        }
    }
    Divider(modifier = Modifier.height(1.dp))
}