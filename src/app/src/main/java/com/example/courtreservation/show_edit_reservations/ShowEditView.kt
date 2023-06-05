package com.example.courtreservation.show_edit_reservations

import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.courtreservation.R
import com.example.courtreservation.server.ServerViewModel
import com.example.courtreservation.show_edit_reservations.documents.Reminder
import com.example.courtreservation.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**************************************************************************************************/
/**************************************************************************************************/
/************************************** PLACEHOLDERS **********************************************/
/**************************************************************************************************/
/**************************************************************************************************/
@Composable
fun EmptyState(msg: String) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(id = R.drawable.empty_state),
            contentDescription = null,
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 16.dp))
        Text(
            text =  msg,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp))
    }
}
@Composable
fun LoadingView() {

    Dialog(onDismissRequest = { }, DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier,
            elevation = cardElevation()) {
            Column(
                Modifier
                    .background(Color.White)
                    .padding(12.dp)) {
                Text(text = "Loading.. Please wait..",
                    Modifier.padding(8.dp), textAlign = TextAlign.Center)

                CircularProgressIndicator(strokeWidth = 4.dp, modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
                )
            }
        }
    }
}



/**************************************************************************************************/
/**************************************************************************************************/
/**************************************** REMINDER ************************************************/
/**************************************************************************************************/
/**************************************************************************************************/
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Agenda(vm: ShowEditViewModel) {

    LazyColumn(modifier = Modifier.animateContentSize() ) {
        if(vm.dailyReminders.isNotEmpty()) {
            items(vm.dailyReminders, key = { it.reservationID }) {
                ReminderCard(reminder = it, vm = vm)
            }
        }else {
            item { EmptyState("Nothing to do today") }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCard(reminder: Reminder, vm: ShowEditViewModel) {


    Card(colors = cardColorsScheme(), modifier =  padding(16, 16), elevation = cardElevation()) {

        Column(modifier = fill("HORIZONTAL")) {
            // IMAGE
            Image(painter = painterResource(id = reminder.image), contentDescription = null,
                modifier = cardPictureStyle(), contentScale = ContentScale.Crop)
            // TITLE
            CardTitle(txt = reminder.sportName)
            CardSubTitle(txt = reminder.courtName)
            CardBody(slot =
                Converters.createDate(
                    reminder.year.toInt(),
                    reminder.month.toInt(),
                    reminder.day.toInt(),
                    reminder.hour.toInt()), customRequest = reminder.customRequest)
            Row {
                OutlinedButton(
                    onClick = {
                        vm.initReminderToEdit(reminder)
                        if(!vm.isExpired()) {
                            if (reminder.author == vm.getNickname())
                                vm.updateCurrentTransaction("STARTED")
                            else
                                vm.enableErrorUserNotAuthorized()
                        } else {
                            vm.enableErrorReservationExpired()
                        }
                    },
                    colors =
                    //if(reminder.author == vm.getNickname())
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    //else
                        //ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.5f)) {
                    Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        vm.initReminderToEdit(reminder)
                        if (!vm.isExpired()) vm.enableChatForReservation(reminder.reservationID)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.5f)) {
                    Icon(imageVector = Icons.Rounded.Mail, contentDescription = null, tint = Color.White)
//                    Spacer(modifier = Modifier.padding(8.dp))
                    //Text(text = vm.numberOfNotifications().toString(), color = Color.White)
                }
            }
        }
    }
}
@Composable
fun CardTitle(txt: String) {
    Text(text = txt.uppercase(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, modifier = padding(16, 8))
}
@Composable
fun CardSubTitle (txt: String) {
    Text(text = txt, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
}
@Composable
fun CardBody (slot: Calendar, customRequest: String) {
    Text(modifier = Modifier.padding(horizontal = 16.dp), text = Converters.dateToString(slot), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    if(customRequest.isNotEmpty()) {
        Text(modifier = Modifier.padding(horizontal = 16.dp), text = customRequest, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
    }else {
        Text(modifier = Modifier.padding(horizontal = 16.dp), text = "", fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
    }
}


/**************************************************************************************************/
/**************************************************************************************************/
/*************************************** MAIN VIEW ************************************************/
/**************************************************************************************************/
/**************************************************************************************************/
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainView(vm: ShowEditViewModel, vmServer: ServerViewModel) {
    var isVisible by remember {
        mutableStateOf(true)
    }

    // DIALOGs
    if(vm.loadingData.value) { // LOADING VIEW
        LoadingView()
    }
    if(vm.transactionStage.value == "STARTED") { // INPUT FORM
        EditDialog(vm = vm)
    }
    if(vm.transactionStage.value == "COMMITTED") { // TRANSACTION RESULT
        TransactionResult(vm = vm, "SUCCESS!")
    }
    if(vm.transactionStage.value == "ABORTED") { // TRANSACTION RESULT
        TransactionResult(vm = vm, "ERROR!")
    }
    if(vm.enableChat.value) {
        Chat(vm = vm)
    }
    if(vm.showReservationExpired.value) { // RESERVATION EXPIRED
        OperationAlert(vm = vm, "RESERVATION EXPIRED!")
    }
    if(vm.showUserNotAuthorized.value) { // RESERVATION EXPIRED
        OperationAlert(vm = vm, "YOU'RE NOT THE CREATOR\nCONTACT THE ADMIN!")
    }

    val currentDate by vm.currentDate.observeAsState()
    val calendarEvents by vm.calendarEvents.observeAsState(emptyList())

    LaunchedEffect(vm.getNickname()) {
        vm.updateReservationDocuments()
    }



    Column {
        Row(modifier = Modifier
            .weight(0.5f, true)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primaryContainer
            )) {
            Box {
                CalendarView(currentDate, vm::updateCurrentDate, calendarEvents)
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.85f to Color.Transparent,
                                1.00f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    ))
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Row(modifier = Modifier.weight(0.5f, false)) {
            Column(modifier = Modifier.weight(0.5f, false)) {
                Agenda(vm = vm)
            }
        }
    }
    if(vmServer.completed.value.lastIndex>=0 && vmServer.playgrounds.value.lastIndex>=0 && isVisible){
        AddSportDialog(vmServer,onDismiss = { isVisible = false })
    }
}

@Composable
fun AddSportDialog(vm: ServerViewModel, onDismiss: () -> Unit) {
    if(vm.completed.value.lastIndex>=0 && vm.playgrounds.value.lastIndex>=0){
        Log.d("ShowDialog","OK")
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter){
                Text("Who is the winner?", style = MaterialTheme.typography.titleLarge)
            } },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "${vm.playgroundOne.value.name} - ${vm.playgroundOne.value.sport}", style = MaterialTheme.typography.bodyLarge)
                    //Text(text = vm.playgroundOne.value.sport, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${if(vm.reservationOne.value.day<10) "0"+vm.reservationOne.value.day else vm.reservationOne.value.day}" +
                            "/${if(vm.reservationOne.value.month+1<10) "0"+(vm.reservationOne.value.month+1).toString() else (vm.reservationOne.value.month+1)}/" +
                            "${vm.reservationOne.value.year} (${vm.reservationOne.value.hour}:00-" +
                            "${vm.reservationOne.value.hour+1}:00)",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(Modifier.height(2.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically){
                        OutlinedButton(onClick = { CoroutineScope(Dispatchers.IO).launch { vm.setWinner(0) } }, colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer
                            //Color(0xFF8FA385)
                        )) {
                            Text(text = "Team Blue", style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = { CoroutineScope(Dispatchers.IO).launch { vm.setWinner(1) }}, colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text(text = "Team Red", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    //Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    //Text("Cancel")
                }
            })
    }

}