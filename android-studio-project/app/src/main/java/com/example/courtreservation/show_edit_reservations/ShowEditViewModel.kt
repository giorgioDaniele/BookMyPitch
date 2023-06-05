package com.example.courtreservation.show_edit_reservations


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.courtreservation.model.local.SharedPreferencesManager
import com.example.courtreservation.model.remote.FirestoreManager
import com.example.courtreservation.service.FirebaseMessages
import com.example.courtreservation.show_edit_reservations.documents.PlaygroundDocument.Companion.toPlaygroundDocument
import com.example.courtreservation.show_edit_reservations.documents.Reminder.Companion.toReminder
import com.example.courtreservation.show_edit_reservations.documents.ReservationDocument.Companion.toReservationDocument
import com.example.courtreservation.show_edit_reservations.documents.PlaygroundDocument
import com.example.courtreservation.show_edit_reservations.documents.Reminder
import com.example.courtreservation.show_edit_reservations.documents.ReservationDocument
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShowEditViewModel  @Inject constructor(

    private val firestoreManager: FirestoreManager,
    private val sharedPreferencesManager: SharedPreferencesManager): ViewModel() {

    private val _playgroundDocuments  = mutableListOf<PlaygroundDocument>()
    private val _reservationDocuments = mutableListOf<ReservationDocument>()
    /**********************************************************************************************/
    /**********************************************************************************************/
    /*************************************** INIT *************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/


    init {

        Log.d("USER", "SHARED PREFERENCES CONTENT" + sharedPreferencesManager.getNickname())
        // IT OBSERVES THE "playgrounds" TABLE. AFTER RECEIVING SUCH SNAPSHOTS, IT MAKE
        // THEM ALL INTO Kotlin CLASS INSTANCES (PlaygroundDocument)
        firestoreManager
            .observePlatformPlaygrounds("playgrounds")
            .addSnapshotListener { documents, error ->
                if (error == null) documents?.mapNotNull { it.toPlaygroundDocument() }.run {
                    updatePlaygroundDocuments(this ?: listOf())
                } else Log.d("LISTEN", "LISTEN FAILED $error")
            }

        // IT OBSERVES THE "reservations" TABLE. AFTER RECEIVING SUCH SNAPSHOTS, IT MAKE
        // THEM ALL INTO Kotlin CLASS INSTANCES (ReservationDocument)
        firestoreManager
            .observePlatformReservations("reservations")
            .addSnapshotListener { documents, error ->
                if (error == null) documents?.mapNotNull { it.toReservationDocument() }.run {
                    this?.let {
                        _reservationDocuments.clear()
                        _reservationDocuments += this
                    }
                    updateReservationDocuments()
                } else Log.d("LISTEN", "LISTEN FAILED $error")
            }
    }

    /**********************************************************************************************/
    /**********************************************************************************************/
    /********************* BOOLEAN STATES FOR CONDITIONAL RENDERINGS  *****************************/
    /**********************************************************************************************/
    /**********************************************************************************************/

    private var _loadingData = mutableStateOf(false)
    val loadingData get() = _loadingData
    private val _enableChat = mutableStateOf(false)
    val enableChat get() = _enableChat

    private val _showReservationExpired = mutableStateOf(false)
    private val _showUserNotAuthorized  = mutableStateOf(false)
    val showReservationExpired get()    = _showReservationExpired
    val showUserNotAuthorized  get()    = _showUserNotAuthorized

    fun disableErrorUserNotAuthorized() {
        _showUserNotAuthorized.value = false
    }
    fun disableErrorReservationExpired() {
        _showReservationExpired.value = false
    }
    fun enableErrorUserNotAuthorized() {
        _showUserNotAuthorized.value = true
    }
    fun enableErrorReservationExpired() {
        _showReservationExpired.value = true
    }


    fun getNickname () : String {
        return sharedPreferencesManager.getNickname()
    }

    // IT STORES ALL THE REMINDERS (A REMINDER IS A PLAYGROUND + RESERVATION COMPOSITION)
    private val _reminders      = mutableListOf<Reminder>()
    // IT STORES ALL THE EVENTS FOR THE CALENDAR, AS A DATES COLLECTION
    private val _calendarEvents = MutableLiveData<List<LocalDate>>(emptyList())
    // IT HOLDS THE CURRENT DATE
    private val _currentDate    = MutableLiveData(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    // IT STORES JUST THE REMINDERS FOR TODAY
    private val _dailyReminders = mutableStateListOf<Reminder>()
    val dailyReminders get()    = _dailyReminders


    val calendarEvents: LiveData<List<LocalDate>> = _calendarEvents
    val currentDate: LiveData<LocalDate>          = _currentDate

    private val possibleValues = listOf(
        "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
        "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "16:00 - 17:00",
        "18:00 - 19:00", "20:00 - 21:00", "21:00 - 22:00", "22:00 - 23:00")
    // OUTER KEY: playgroundId
    // INNER KEY: date
    // GIVEN A PLAYGROUND, IT STORES FOR ALL THE DATES THE LIST OF THE AVAILABLE SLOTS
    private val _availableSlots = mutableMapOf<String, Map<LocalDate, List<String>>>()
    val availableSlots : List<String>
        get() {
            val playgroundMap = _availableSlots[_reminderInEditMode.reminder.playgroundID]
            return if (playgroundMap != null) {
                val date =  LocalDate(
                    _reminderInEditMode.reminder.year.toInt(),
                    _reminderInEditMode.reminder.month.toInt(),
                    _reminderInEditMode.reminder.day.toInt())
                if(!playgroundMap.containsKey(date))
                    return possibleValues
                else {
                    Log.d("EDT RES", playgroundMap[date].toString())
                    return playgroundMap[date]!!
                }
            } else
                possibleValues
        }

    /**********************************************************************************************/
    /**********************************************************************************************/
    /******************************* UPDATING FUNCTIONS  ******************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/

    private fun updateReminders(reservations : List<ReservationDocument>, playgrounds: List<PlaygroundDocument>) {
        _reminders.clear()
        _reminders += Pair(reservations, playgrounds).toReminder()
    }
    private fun updateCalendarEvents() {
        _calendarEvents.postValue(
            _reminders.map {
                LocalDate(it.year.toInt(), it.month.toInt(), it.day.toInt())
            }
        )
    }
    private fun updateDailyReminders() {
        _dailyReminders.clear()
        _dailyReminders += _reminders.filter {
            LocalDate(
                it.year.toInt(),
                it.month.toInt(),
                it.day.toInt()) == _currentDate.value }.sortedBy {
            it.hour }
    }
    private fun updatePlaygroundDocuments (value: List<PlaygroundDocument>) {
        _playgroundDocuments.clear().run {
            _playgroundDocuments += value
        }
    }
    fun updateReservationDocuments() {
        val playgroundIDKeys = _playgroundDocuments.map { it.id }

        _availableSlots.values.clear()
        _availableSlots.clear()

        playgroundIDKeys.forEach { playgroundIDKey ->
            val map = _reservationDocuments.filter { document ->
                document.playgroundID == playgroundIDKey
            }.groupBy { document ->
                LocalDate(document.year.toInt(), document.month.toInt() + 1, document.day.toInt())
            }.mapValues { entry ->
               possibleValues.minus(entry.value.
               map { it.hour }.
               sortedBy {it}.
               map { h ->
                   "${h.toString().padStart(2, '0')}:00 - ${(h + 1).toString().padStart(2, '0')}:00"
               }.toSet())
            }
            _availableSlots[playgroundIDKey] = map
        }

        // UPDATE A LIST OF REMINDERS
        updateReminders(_reservationDocuments.filter {
            it.team0.contains(sharedPreferencesManager.getNickname()) ||
                    it.team1.contains(sharedPreferencesManager.getNickname())
        }, _playgroundDocuments)
        // UPDATE THE CALENDAR EVENTS
        updateCalendarEvents()
        // UPDATE THE DAILY REMINDERS
        updateDailyReminders()
    }
    fun updateCurrentDate(newDate: LocalDate) {
        _currentDate.value = newDate
        updateDailyReminders()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun isExpired() : Boolean {

        if(LocalDateTime.of(
            _reminderInEditMode.reminder.year.toInt() ,
            _reminderInEditMode.reminder.month.toInt(),
            _reminderInEditMode.reminder.day.toInt(),
            _reminderInEditMode.reminder.hour.toInt(), 0) >= LocalDateTime.now()) {
            disableErrorReservationExpired()
            return false
        }
        else
            enableErrorReservationExpired()
        return true
    }
    fun isAuthorized() : Boolean {
        if(_reminderInEditMode.reminder.author == sharedPreferencesManager.getNickname()) {
            disableErrorUserNotAuthorized()
            return true
        }
        else
            enableErrorUserNotAuthorized()
        return false
    }

    /**********************************************************************************************/
    /**********************************************************************************************/
    /******************************* FIREBASE CRUD FUNCTIONS  *************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/

    data class ReminderInEditing(var reminder: Reminder, var pickedHour: Int, var inputText: String)
    private var _reminderInEditMode : ReminderInEditing =
        ReminderInEditing(
            Reminder(
            "", // RESERVATION AUTHOR
            "", // STATUS
            "", // RESERVATION ID
            "", // PLAYGROUND ID
            "", // CUSTOM REQUEST
            0,  // HOUR
            0,  // DAY
            0,  // MONTH
            0,  // YEAR
            "", // COURT NAME
            "", // SPORT NAME
            0,  // IMAGE
            mutableListOf(),  // TEAM 0
            mutableListOf(),  // TEAM 1
            ),
            0,  // SELECTED HOUR
            "") // INPUT TEXT FOR CUSTOM REQUEST

    val reminderInEditMode get() = _reminderInEditMode


    private val _currentSlot = mutableStateOf("")
    private val _inputText   = mutableStateOf("")
    val currentSlot get()    = _currentSlot
    val inputText get()      =  _inputText

    fun initReminderToEdit(reminder: Reminder) {
        _reminderInEditMode.reminder = reminder
        Log.d("EDIT", "${reminder.hour.toString().padStart(2, '0')}:00 - ${(reminder.hour + 1).toString().padStart(2, '0')}:00")
        _currentSlot.value = "${reminder.hour.toString().padStart(2, '0')}:00 - ${(reminder.hour + 1).toString().padStart(2, '0')}:00"
        _reminderInEditMode.pickedHour = _currentSlot.value.substring(0,2).toInt()
        Log.d("EDIT", _currentSlot.value)
        _inputText.value  = reminder.customRequest
    }
    fun changePickedHour(pickedHour: String) {
        _reminderInEditMode.pickedHour = pickedHour.substring(0,2).toInt()
    }
    fun changeInputText(inputText: String) {
        _inputText.value = inputText
        _reminderInEditMode.inputText = inputText
    }

    /**********************************************************************************************/
    /************************************** DELETE ************************************************/
    /**********************************************************************************************/
    fun deleteReservation () {
        _loadingData.value = true
        viewModelScope.launch {
            delay(1000)
            firestoreManager.deleteDocumentInCollection(
                "reservations",
                reminderInEditMode.reminder.reservationID).addOnSuccessListener {
                _loadingData.value = false
                _transactionStage.value = "COMMITTED"

            }.addOnFailureListener {
                _loadingData.value = false
                _transactionStage.value = "ABORTED"
            }

        }
    }
    /**********************************************************************************************/
    /*************************************** EDIT *************************************************/
    /**********************************************************************************************/
    fun editReservation() {
        val newReservation = mapOf(
            "customRequest" to _reminderInEditMode.inputText,
            "day"           to _reminderInEditMode.reminder.day,
            "hour"          to _reminderInEditMode.pickedHour,
            "month"         to _reminderInEditMode.reminder.month - 1,
            "playgroundId"  to _reminderInEditMode.reminder.playgroundID,
            "status"        to _reminderInEditMode.reminder.status,
            "team_0"        to _reminderInEditMode.reminder.team0,
            "team_1"        to _reminderInEditMode.reminder.team1,
            "year"          to _reminderInEditMode.reminder.year)

        viewModelScope.launch {
            _loadingData.value = true
            delay(1000)
            firestoreManager.editDocumentInCollection(
                "reservations",
                reminderInEditMode.reminder.reservationID, newReservation).addOnSuccessListener {
                _loadingData.value = false
                    _transactionStage.value = "COMMITTED"

            }.addOnFailureListener {
                _loadingData.value = false
                _transactionStage.value = "ABORTED"
            }
        }
    }

    private val _transactionStage = mutableStateOf("NOT STARTED")
    val transactionStage get() = _transactionStage


    fun updateCurrentTransaction(stage: String) {
        _transactionStage.value = stage
    }

    /**********************************************************************************************/
    /**********************************************************************************************/
    /************************************** CHAT **************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/


    private val _ongoingMessage = mutableStateOf("")
    val ongoingMessage get()    = _ongoingMessage
    fun updateOngoingMessage(value: String) {
        _ongoingMessage.value = value
    }

    fun sendMessage() {

        val message = mapOf(
            "from"          to sharedPreferencesManager.getNickname(),
            "to"            to (_reminderInEditMode.reminder.team0 + _reminderInEditMode.reminder.team1).toList(),
            "message"       to ongoingMessage.value,
            "reservationId" to _reminderInEditMode.reminder.reservationID,
            "timestamp"     to Clock.System.now().toString())

        viewModelScope.launch {
            firestoreManager.addDocumentInCollection("notifications", message as Map<String, String>)
                .addOnSuccessListener {}
        }

        val notification = mapOf(
            "from" to sharedPreferencesManager.getNickname(),
            "to"   to (_reminderInEditMode.reminder.team0 + _reminderInEditMode.reminder.team1).toList(),
            "text" to  ongoingMessage.value,
            "where" to _reminderInEditMode.reminder.courtName,
            "when"  to LocalDate(
                _reminderInEditMode.reminder.year.toInt(),
                _reminderInEditMode.reminder.month.toInt(),
                _reminderInEditMode.reminder.day.toInt()).let {
                    it.dayOfMonth.toString() + " " + it.month.toString().lowercase()
                        .replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else it.toString() } + " " + it.year.toString()
            }
        )

        // FIREBASE MESSAGING
        FirebaseMessages.sendMessage(notification, "messages")
    }


    fun observeNotifications(): Query {
        return firestoreManager.observeNotifications(
                "notifications", "", "",
                //"to",
                //sharedPreferencesManager.getNickname(),
                "reservationId",
                _reservationIdKey)

    }

    private var _reservationIdKey = ""
    fun enableChatForReservation(id: String) {
        _enableChat.value = true
        _reservationIdKey = id
    }
    fun disableChatForReservation() {
        _enableChat.value = false
    }
}
