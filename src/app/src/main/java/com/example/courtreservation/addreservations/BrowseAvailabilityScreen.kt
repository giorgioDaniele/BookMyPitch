package com.example.courtreservation.addreservations

import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.courtreservation.show_edit_reservations.CalendarView
import com.example.courtreservation.show_edit_reservations.LoadingView
import com.example.courtreservation.show_edit_reservations.TransactionResultDialog
import com.example.courtreservation.utils.Converters
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseAvailabilityScreen(
    nickname: String,
    viewModel: BrowseAvailabilityViewModel = viewModel(
        factory = BrowseAvailabilityViewModel.Factory(nickname)
    )
) {
    val currentDate by viewModel.currentDate.observeAsState()
    val occupiedSlotsForCurrentDate by viewModel.occupiedSlotsByDate.observeAsState(listOf())
    val coroutineScope = rememberCoroutineScope()

    // Keep the state across configuration changes
    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var currentSlot by rememberSaveable {
        mutableStateOf(0)
    }

    val dialogState by viewModel.dialogState.observeAsState()

    when (dialogState) {
        BrowseAvailabilityViewModel.ProgressDialogState.LOADING -> LoadingView()
        BrowseAvailabilityViewModel.ProgressDialogState.SUCCESS -> TransactionResultDialog("SUCCESS!")
        BrowseAvailabilityViewModel.ProgressDialogState.FAILURE -> TransactionResultDialog("ERROR!")
        else -> { }
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
            .background(color = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box {
                CalendarView(currentDate, viewModel::setCurrentDate, emptyList(), true)
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
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items(23 - 8 + 1) { index ->
                        val slot = index + 8
                        val slotString = "${String.format("%02d", slot)}:00 - ${String.format("%02d", slot + 1)}:00"

                        val slotCalendar = currentDate?.let {
                            Converters.createDate(it.year, it.monthNumber - 1, it.dayOfMonth, slot)
                        }

                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            enabled = !occupiedSlotsForCurrentDate.contains(slot) && (slotCalendar == null || slotCalendar >= Calendar.getInstance()),
                            onClick = { showDialog = true; currentSlot = slot }
                        ) {
                            Text(slotString)
                        }

                        if (slot < 23) {
                            Divider(modifier = Modifier.padding(horizontal = 32.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DialogContents(
                onCancel = { showDialog = false },
                onConfirm = { customRequest ->
                    coroutineScope.launch {
                        showDialog = false
                        viewModel.addReservation(currentSlot, customRequest)
                    }
                }
            )
        }
    }
}

@Composable
private fun DialogContents(
    onCancel: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var customRequest by rememberSaveable {
        mutableStateOf("")
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        // Make the column scrollable to handle very large custom requests
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = customRequest,
                onValueChange = { customRequest = it },
                supportingText = {
                    Text(text = "Custom request")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }

                TextButton(
                    onClick = {
                        onConfirm(customRequest.ifEmpty { null })
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}