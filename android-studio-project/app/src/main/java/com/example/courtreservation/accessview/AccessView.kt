package com.example.courtreservation.accessview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.courtreservation.R
import com.example.courtreservation.addUsers
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


@Composable
fun AccessView(vm: AccessViewModel, onSuccess: (String) -> Unit) {
    val (login, setLogin) = remember {
        mutableStateOf(true)
    }
    if(login){
        LoginPage(vm = vm, setLogin, onSuccess = onSuccess)
    } else{
        RegistrationPage(vm = vm, setLogin, onSuccess = onSuccess)
    }
}
@Composable
fun LoginPage(vm: AccessViewModel, setLogin: (Boolean) -> Unit, onSuccess: (String) -> Unit) {

    Column(
        modifier = Modifier.background(brush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        // State for storing user input values
        val nickname   = remember { mutableStateOf("") }

        // State for message error
        val error: MutableState<String> = remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()

        val rainbowColorsBrush = remember {
            Brush.sweepGradient(
                listOf(
                    Color(0xFF9575CD),
                    Color(0xFFFFB74D),
                    Color(0xFFFFF176),
                    Color(0xFF9575CD)
                )
            )
        }

        Box(modifier = Modifier.size(200.dp, 200.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.application_logo), contentDescription = "", modifier = Modifier
                .size(200.dp)
                .border(
                    BorderStroke(5.dp, rainbowColorsBrush),
                    CircleShape
                )
                .padding(6.dp)
                .clip(CircleShape)
            )
        }

        Column {
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Nickname") },
                value = nickname.value,
                onValueChange = { error.value = ""; nickname.value = it},
                isError = nickname.value.isEmpty() && error.value == "Please fill in all required fields")
        }

        when(error.value) {
            "Please fill in all required fields" -> {
                Text(
                    text = "Please fill in all required fields", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "The nickname does not exist" -> {
                Text(
                    text = "The nickname does not exist", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "Success" -> onSuccess("@" + nickname.value)
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        if (nickname.value.isEmpty())
                            error.value = "Please fill in all required fields"
                        else coroutineScope.launch { vm.login (nickname.value) { error.value = it } }
                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Login")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Don't have an account? Register!",style = MaterialTheme.typography.bodyLarge, modifier = Modifier.clickable {
                    setLogin(false)
                })
            }

        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RegistrationPage(vm: AccessViewModel, setLogin: (Boolean) -> Unit, onSuccess: (String) -> Unit) {
    Column(
        modifier = Modifier.background(brush = Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        // State for storing user input values
        val nickname   = remember { mutableStateOf("") }
        val name  = remember { mutableStateOf("") }
        val surname = remember { mutableStateOf("") }
        val age   = remember { mutableStateOf("") }

        // State for message error
        val error: MutableState<String> = remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()

        val rainbowColorsBrush = remember {
            Brush.sweepGradient(
                listOf(
                    Color(0xFF9575CD),
                    Color(0xFFFFB74D),
                    Color(0xFFFFF176),
                    Color(0xFF9575CD)
                )
            )
        }

        Box(modifier = Modifier.size(200.dp, 200.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.application_logo), contentDescription = "", modifier = Modifier
                .size(200.dp)
                .border(
                    BorderStroke(5.dp, rainbowColorsBrush),
                    CircleShape
                )
                .padding(6.dp)
                .clip(CircleShape)
            )
        }

        Column() {
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Nickname") },
                value = nickname.value,
                onValueChange = { error.value = ""; nickname.value = it},
                isError = nickname.value.isEmpty() && error.value == "Please fill in all required fields")
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Name") },
                value = name.value,
                onValueChange = {  error.value = ""; name.value = it},
                isError = name.value.isEmpty() && error.value == "Please fill in all required fields")

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Surname") },
                value = surname.value,
                onValueChange = {  error.value = ""; surname.value = it},
                isError = surname.value.isEmpty() && error.value == "Please fill in all required fields")

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Age") },
                value = age.value,
                onValueChange = { error.value = ""; age.value = it},
                isError = age.value.isEmpty() && error.value == "Please fill in all required fields")
        }


        when(error.value) {
            "Please fill in all required fields" -> {
                Text(
                    text = "Please fill in all required fields", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "The nickname is already in use" -> {
                Text(
                    text = "The nickname is already in use", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "Success" -> onSuccess("@" + nickname.value)
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        if (name.value.isEmpty() || surname.value.isEmpty() || nickname.value.isEmpty())
                            error.value = "Please fill in all required fields"
                        else coroutineScope.launch { vm.register(nickname.value, name.value, surname.value, age.value) { error.value = it } }
                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Register")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Have already an account? Login!",style = MaterialTheme.typography.bodyLarge, modifier = Modifier.clickable {
                    setLogin(true)
                })
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

/*
fun LoginPage(vm: AccessViewModel, onSuccess: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.background(brush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        // State for storing user input values
        val firstName  = remember { mutableStateOf(vm.firstName) }
        val secondName = remember { mutableStateOf(vm.secondName) }
        val nickname   = remember { mutableStateOf(vm.nickname) }

        // State for message error
        val error: MutableState<String> = remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()

        val rainbowColorsBrush = remember {
            Brush.sweepGradient(
                listOf(
                    Color(0xFF9575CD),
                    Color(0xFFFFB74D),
                    Color(0xFFFFF176),
                    Color(0xFF9575CD)
                )
            )
        }

        Box(modifier = Modifier.size(200.dp, 200.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = "", modifier = Modifier.size(200.dp)
                .border(
                    BorderStroke(5.dp, rainbowColorsBrush),
                    CircleShape
                ).padding(6.dp).clip(CircleShape)
            )
        }

        Column() {
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "First Name") },
                value = firstName.value,
                onValueChange = {  error.value = ""; vm.firstName = it; firstName.value = it},
                isError = firstName.value.isEmpty() && error.value == "Please fill in all required fields")

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Second Name") },
                value = secondName.value,
                onValueChange = {  error.value = ""; vm.secondName = it; secondName.value = it},
                isError = secondName.value.isEmpty() && error.value == "Please fill in all required fields")

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                label = { Text(text = "Nickname") },
                value = nickname.value,
                onValueChange = { error.value = ""; vm.nickname = it; nickname.value = it},
                isError = nickname.value.isEmpty() && error.value == "Please fill in all required fields")

            Spacer(modifier = Modifier.height(20.dp))
        }


        when(error.value) {
            "Please fill in all required fields" -> {
                Text(
                    text = "Please fill in all required fields", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "The nickname is already in use" -> {
                Text(
                    text = "The nickname is already in use", color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "Success" -> onSuccess(true)
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    if (firstName.value.isEmpty() || secondName.value.isEmpty() || nickname.value.isEmpty())
                        error.value = "Please fill in all required fields"
                    else coroutineScope.launch { vm.backUpProfile { error.value = it } }
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Login")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
 */