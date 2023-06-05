package com.example.courtreservation.profile

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.courtreservation.R
import com.example.courtreservation.Sport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EditProfileScreen(vm: ProfileViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (vm.user.value!=null){
            Box {
                var isAddSportDialogVisible by remember { mutableStateOf(false) }
                LazyColumn {
                    item{ProfileEditHeader(vm)}
                    item{ProfileEditForm(vm)}

                            items(vm.user.value!!.sports) { sport ->
                                if (sport.active) {
                                    EditSportCard(vm, sport)
                                }
                            }
                    item{Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
                        Button( onClick = { isAddSportDialogVisible=true }) {
                            Text(text = "Add a new sport")
                        }
                    }}
                    item{if (isAddSportDialogVisible) {
                        AddSportDialog(
                            onAddSport = { name, level ->
                                Log.d("Sports", "Adding sport with name: $name and level: $level")
                                CoroutineScope(IO).launch {
                                    vm.addSport(name, level)
                                }
                            },vm,
                            onDismiss = { isAddSportDialogVisible = false }
                        )
                    }}
                }
            }

        }

    }
}

@Composable
fun ProfileEditHeader(vm: ProfileViewModel) {

    var expandedDropdownMenu by rememberSaveable {
        mutableStateOf(false)
    }
    var link: String? by remember {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = vm.user.value!!.link, block = {
        link=vm.user.value!!.link
    })
    val context = LocalContext.current

    val cameraContentUri = remember {
        val parentDir = File(context.filesDir, "images")
        parentDir.mkdir()
        val cameraFile = File.createTempFile(vm.getNickname(), ".jpeg", parentDir)
        FileProvider.getUriForFile(context, "com.example.courtreservation.profile.MyFileProvider", cameraFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            CoroutineScope(IO).launch{
                vm.uploadImage(cameraContentUri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { currUri ->
        currUri?.let {
            CoroutineScope(IO).launch{
                vm.uploadImage(it)
            }
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Image(
            painter =  if (vm.user.value!!.link!=null) {
                rememberAsyncImagePainter(link)
            } else {
                painterResource(id = R.drawable.default_profile_image)
            },
            contentScale = ContentScale.Crop,
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(150.dp)
                .border(BorderStroke(4.dp, rainbowColorsBrush), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .clickable { expandedDropdownMenu = true }
        )

        DropdownMenu(
            expanded = expandedDropdownMenu,
            onDismissRequest = { expandedDropdownMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Take picture") },
                onClick = {
                    cameraLauncher.launch(cameraContentUri)
                    expandedDropdownMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Select from gallery") },
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    expandedDropdownMenu = false
                }
            )
        }
    }
}


@Composable
fun ProfileEditForm(
    vm: ProfileViewModel
) {

    //var nickname by remember { mutableStateOf(user.nickname) }
    var name by remember { mutableStateOf(vm.user.value!!.name) }
    var surname by remember { mutableStateOf(vm.user.value!!.surname) }
    var age by remember { mutableStateOf(vm.user.value!!.age) }
    var city by remember { mutableStateOf(vm.user.value!!.city) }
    var bio by remember { mutableStateOf(vm.user.value!!.bio) }
    var email by remember { mutableStateOf(vm.user.value!!.email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; vm.updateUserName(vm.user.value!!.copy(name=name))},
            label = { Text("name") },
            modifier = Modifier.padding(16.dp)
        )
        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it; vm.updateUserSurname(vm.user.value!!.copy(surname=surname)) },
            label = { Text("surname") },
            modifier = Modifier.padding(16.dp)
        )
        OutlinedTextField(
            value = age.toString(),
            onValueChange = { age = Integer.parseInt(it); vm.updateAge(vm.user.value!!.copy(age=age)) },
            label = { Text("age") },
            modifier = Modifier.padding(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = city,
            onValueChange = { city = it; vm.updateUserCity(vm.user.value!!.copy(city=city)) },
            label = { Text("city") },
            modifier = Modifier.padding(16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; vm.updateUserEmail(vm.user.value!!.copy(email = email)) },
            label = { Text("e-mail") },
            modifier = Modifier.padding(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it; vm.updateUserBio(vm.user.value!!.copy(bio=bio))},
            label = { Text("bio") },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun AddSportDialog(onAddSport: (name: String, level: String) -> Unit, vm: ProfileViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("beginner") }
    val levels = listOf("beginner", "intermediate", "advanced")
    if(vm.user.value!=null && vm.user.value!!.sports!= emptyList<Sport>()){
        name = vm.user.value!!.sports.filter { !it.active }[0].name
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add a new sport") },
            text = {
                Column {
                    Spinner(list = vm.user.value!!.sports.filter { !it.active }.map { it.name }, preselected = vm.user.value!!.sports.filter { !it.active }[0].name, onChangeValue = {
                        name = it
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    Spinner(list = levels, preselected = "beginner", onChangeValue = {
                        level = it
                    } )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAddSport(name, level)
                    onDismiss()
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Spinner(
    list: List<String>,
    preselected: String,
    modifier: Modifier = Modifier,
    onChangeValue: (level: String) -> Unit
) {

    var selected by remember { mutableStateOf (preselected) }
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.clickable {
            expanded = !expanded
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {

            androidx.compose.material3.Text(
                text = selected,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            androidx.compose.material3.Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.padding(8.dp))

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                list.forEach { listEntry ->

                    DropdownMenuItem(
                        onClick = {
                            selected = listEntry
                            expanded = false
                            onChangeValue(selected)
                        },
                        text = {
                            androidx.compose.material3.Text(
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


/*
@Composable
fun EditSports(
    modifier: Modifier,
    vm: ProfileViewModel
) {

    var isAddSportDialogVisible by remember { mutableStateOf(false) }
    Box(modifier = modifier){
        LazyColumn {
            items(vm.user.value!!.sports) { sport ->
                if (sport.active) {
                    EditSportCard(vm, sport)
                }
            }
        }
    }
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
        Button( onClick = { isAddSportDialogVisible=true }) {
            Text(text = "Add a new sport")
        }
    }
    if (isAddSportDialogVisible) {
        AddSportDialog(
            onAddSport = { name, level ->
                Log.d("Sports", "Adding sport with name: $name and level: $level")
                CoroutineScope(IO).launch {
                    vm.addSport(name, level)
                }
            },vm,
            onDismiss = { isAddSportDialogVisible = false }
        )
    }
}
*/