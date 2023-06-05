package com.example.courtreservation.show_edit_reservations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.courtreservation.show_edit_reservations.documents.NotificationDocument
import com.example.courtreservation.show_edit_reservations.documents.NotificationDocument.Companion.toNotificationDocument
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**************************************************************************************************/
/**************************************************************************************************/
/*********************************** EDIT VIEW + RESULTS  *****************************************/
/**************************************************************************************************/
/**************************************************************************************************/
@Composable
fun TransactionResult(vm: ShowEditViewModel, message: String) {

    LaunchedEffect(Unit) {
        delay(2000)
        vm.updateCurrentTransaction("NOT STARTED")
    }

    TransactionResultDialog(message)
}

@Composable
fun TransactionResultDialog(message: String) {
    Dialog(onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {

        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), elevation = cardElevation()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if(message == "SUCCESS!") {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Icon",
                        tint = Color(25, 135, 84), // Green
                        modifier = Modifier.size(32.dp))
                }else{
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        tint = Color(223, 71, 89), // Red
                        modifier = Modifier.size(32.dp))
                }
                Column {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = message,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun OperationAlert(vm: ShowEditViewModel, message: String) {

    LaunchedEffect(Unit) {
        delay(2000)
        if(message == "RESERVATION EXPIRED!")
            vm.disableErrorReservationExpired()
        if(message == "YOU'RE NOT THE CREATOR\nCONTACT THE ADMIN!")
            vm.disableErrorUserNotAuthorized()
    }

    Dialog(onDismissRequest = {},
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), elevation = cardElevation()) {
            Column {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        tint = Color(223, 71, 89), // Red
                        modifier = Modifier.size(32.dp))
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = message,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
@Composable
fun EditDialog (vm: ShowEditViewModel) {

    // region: CARD STYLING
    val cardModifier  : (@Composable () -> Modifier) = {
        Modifier
            .fillMaxWidth()
            .padding(8.dp) }
    val cardShape     : (@Composable () -> Shape)    = { RoundedCornerShape(10.dp) }
    val cardElevation : (@Composable () -> CardElevation) = { CardDefaults.cardElevation(defaultElevation = 8.dp) }
    val padding : (@Composable (padx: Int, pady: Int) -> Modifier) = {
            padx, pady -> Modifier.padding(horizontal = padx.dp, vertical = pady.dp)
    }
    // endregion

    val dangerColor  = Color(223, 71, 89)
    val successColor = Color(25, 135, 84)


    val showConfirmation = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { vm.updateCurrentTransaction("FINISHED") }, DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(shape = cardShape(), modifier = cardModifier()) {
            Column(Modifier.background(Color.White)) {

                // Create a row for the sport icon
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Image(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(0.85f)
                            .clip(RoundedCornerShape(8.dp)),
                        painter = painterResource(id = vm.reminderInEditMode.reminder.image),
                        contentDescription = "Sport Picture",
                        contentScale = ContentScale.FillWidth
                    )
                }

                // Create a column...
                Column(modifier = padding(16, 8)) {
                    // ... the first row is the sport name
                    Text(
                        text = vm.reminderInEditMode.reminder.sportName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge)
                        // color = MaterialTheme.colorScheme.onPrimaryContainer)

                    // ... the second row is the court name
                    Text(text = vm.reminderInEditMode.reminder.courtName, fontWeight = FontWeight.Medium)
                        //color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                // Display a label to suggest the
                // type of underlying interaction...
                Text(
                    text = "Select a slot:",
                    modifier = padding(16, 8),
                    fontWeight = FontWeight.Thin)
                    //color = MaterialTheme.colorScheme.onPrimaryContainer)

                // ... the actual interaction: a number picker
                /*
                ListItemPicker(
                    dividersColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { value -> Log.d("EDIT", "LA") },
                    value = vm.pickedHour.value,
                    onValueChange = {  vm.changePickedHour(it) },
                    list = vm.availableSlots
                )*/
                val selected = remember { mutableStateOf("") }
                Row(modifier = Modifier.height(110.dp)) {
                    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                        items(vm.availableSlots) { item ->
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                onClick = { vm.changePickedHour(item); selected.value = item},
                                colors = if(selected.value == item) ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer) else ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                                Text(
                                    text = item,
                                    fontWeight =
                                    if(item == vm.currentSlot.value)
                                        FontWeight.Medium
                                    else
                                        FontWeight.Light

                                )
                            }
                        }
                    }
                }

                // Display a label to suggest the
                // type of underlying interaction...
                Text(
                    //color = MaterialTheme.colorScheme.onPrimaryContainer,
                    text = "Edit your request:",
                    modifier = padding(16, 8),
                    fontWeight = FontWeight.Thin)

                // ... the actual interaction: a input form
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .heightIn(min = 64.dp),
                        value = vm.inputText.value,
                        onValueChange = { value: String -> vm.changeInputText(value) })
                }

                Row(Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = { showConfirmation.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = dangerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(0.5f)
                    ) { Text(text = "Delete" , fontWeight = FontWeight.ExtraBold,
                        color = Color.White) }
                    OutlinedButton(
                        onClick = { vm.editReservation() },
                        colors = ButtonDefaults.buttonColors(containerColor = successColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(0.5f)) { Text(text = "Confirm" , fontWeight = FontWeight.ExtraBold,
                        color = Color.White) }
                }
            }
        }
    }

    if(showConfirmation.value) {

        Dialog(onDismissRequest = {}, DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Card(shape = cardShape(), modifier = cardModifier(), elevation = cardElevation()) {

                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    text = "Are you sure you want to proceed?",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium)


                Row(Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = { vm.deleteReservation()  },
                        colors = ButtonDefaults.buttonColors(containerColor = dangerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(0.5f)
                    ) {
                        Text(text = "YES", fontWeight = FontWeight.ExtraBold,
                            color = Color.White) }
                    OutlinedButton(
                        onClick = { showConfirmation.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = successColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(0.5f)) {
                        Text(text = "NO", fontWeight = FontWeight.ExtraBold,
                            color = Color.White) }
                }
            }
        }
    }
}


val cardColorsScheme : (@Composable () -> CardColors) = {
    CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}
val cardElevation    : (@Composable () -> CardElevation) = {
    CardDefaults.cardElevation(defaultElevation = 8.dp)
}
val cardPictureStyle : (@Composable () -> Modifier) = {
    Modifier
        .height(90.dp)
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
}
val padding          : (@Composable (padx: Int, pady: Int) -> Modifier) = {
        padx, pady -> Modifier.padding(horizontal = padx.dp, vertical = pady.dp)
}
val fill             : (@Composable (orientation: String) -> Modifier) = {
        orientation ->  when(orientation.uppercase()) {
    "HORIZONTAL" -> Modifier.fillMaxWidth()
    "VERTICAL"   -> Modifier.fillMaxWidth()
    else         -> Modifier.fillMaxSize()
    }
}


/**************************************************************************************************/
/**************************************************************************************************/
/****************************************** CHAT **************************************************/
/**************************************************************************************************/
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Chat(vm: ShowEditViewModel) {

    val listState = rememberLazyListState()
    // Remember a CoroutineScope to be able to launch
    val coroutineScope = rememberCoroutineScope()

    val messagesState = remember { mutableStateOf(emptyList<NotificationDocument>()) }
    lateinit var listener: ListenerRegistration
    
    LaunchedEffect(Unit) {
        val listenerRegistration = vm.observeNotifications()
            .addSnapshotListener { documents, error ->
                if (error != null) return@addSnapshotListener
                val messages = documents?.mapNotNull { it.toNotificationDocument() }
                if (messages != null) {
                    messagesState.value = messages.sortedBy { it.timestamp }
                    coroutineScope.launch {
                        // Animate scroll to the last item
                        if(messagesState.value.isNotEmpty()) {
                            listState.animateScrollToItem(index = messagesState.value.lastIndex)
                        }
                    }
                }
            }
        listener = listenerRegistration
    }
    DisposableEffect(Unit) {
        onDispose {
            listener.remove()
        }
    }

    Dialog( onDismissRequest = {vm.disableChatForReservation()},
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), elevation = cardElevation()) {
            Column(modifier = Modifier
                .size(300.dp, 500.dp)
                .background(Color.White)) {
                LazyColumn(modifier = Modifier.weight(0.7f), state = listState) {
                    if (messagesState.value.isEmpty()) {
                        item { EmptyState(msg = "No Messages") }
                    } else {
                        itemsIndexed(messagesState.value) { index, message ->
                            if(index > 0)
                                if(messagesState.value[index - 1].from == message.from)
                                    MessageCard(msg = message.message, sender = message.from, vm = vm, true)
                                else
                                    MessageCard(msg = message.message, sender = message.from, vm = vm, false)
                            else
                                MessageCard(msg = message.message, sender = message.from, vm = vm, false)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                //Spacer(modifier = Modifier.padding(vertical = 90.dp))
                Row(modifier = Modifier.weight(0.2f)) {
                    Column() {
                        TextField(
                            value = vm.ongoingMessage.value,
                            onValueChange = { value: String -> vm.updateOngoingMessage(value.replace("\n", "")) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .onKeyEvent {
                                    if (it.key == Key.Enter) {
                                        vm.sendMessage()
                                        vm.updateOngoingMessage("")
                                        true
                                    } else {
                                        false
                                    }
                                }
                            ,
                            placeholder = { Text("Type a message") })
                        OutlinedButton(
                            onClick = {
                                vm.sendMessage()
                                vm.updateOngoingMessage("")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
@Composable
fun MessageCard(msg: String, sender: String, vm: ShowEditViewModel, continuation: Boolean) {

    Row(modifier =
        if(!continuation)
            Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
        else
            Modifier
                .padding(all = 2.dp)
                .padding(horizontal = 8.dp)) {

        Column(modifier = Modifier.fillMaxWidth()) {
            if(!continuation) {
                    Text(
                        fontFamily = FontFamily.SansSerif,
                        text = sender,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                        if(sender == vm.getNickname())
                            Modifier.align(Alignment.End)
                        else
                            Modifier.align(Alignment.Start))
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color =
                    if(sender == vm.getNickname())
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .padding(1.dp)
                    .align(
                        if (sender == vm.getNickname())
                            Alignment.End
                        else Alignment.Start
                    )) {
                Text(text = msg,
                    modifier = Modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


