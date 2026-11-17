
package com.barakahtravel.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StaffTravelScheduleScreen(
    schedules: List<TravelSchedule>,
    bookings: List<Booking>,
    onAdd: (
        Int, String, String, String, String, String,
        String, String, String, String, String
    ) -> Unit,
    onUpdate: (
        Int, Int, String, String, String, String,
        String, String, String, String, String, String
    ) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<TravelSchedule?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Travel Schedule",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Text(
                    "${schedules.size} travel schedules",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = {
                    editingSchedule = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest
                )
            ) {
                Text("+ Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (schedules.isEmpty()) {
            Text(
                "No travel schedules have been created yet.",
                color = Muted
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                schedule.customer,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Booking: ${schedule.bookingReference}")
                            Text("Package: ${schedule.packageName}")
                            Text("Departure: ${schedule.departureDate}")
                            Text("Return: ${schedule.returnDate}")
                            Text("Departure time: ${schedule.departureTime ?: "Not set"}")
                            Text("Return time: ${schedule.returnTime ?: "Not set"}")
                            Text("From: ${schedule.departureAirport ?: "Not set"}")
                            Text("To: ${schedule.arrivalAirport ?: "Not set"}")
                            Text("Airline: ${schedule.airline ?: "Not set"}")
                            Text("Flight: ${schedule.flightNumber ?: "Not set"}")
                            Text("Status: ${schedule.status}")

                            if (!schedule.notes.isNullOrBlank()) {
                                Text("Notes: ${schedule.notes}")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    editingSchedule = schedule
                                    showDialog = true
                                }
                            ) {
                                Text("Edit schedule")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        TravelScheduleDialog(
            bookings = bookings,
            existingSchedule = editingSchedule,
            onDismiss = {
                showDialog = false
                editingSchedule = null
            },
            onSave = { bookingId, departureDate, returnDate,
                       departureTime, returnTime, departureAirport,
                       arrivalAirport, airline, flightNumber, status, notes ->

                val existing = editingSchedule

                if (existing == null) {
                    onAdd(
                        bookingId,
                        departureDate,
                        returnDate,
                        departureTime,
                        returnTime,
                        departureAirport,
                        arrivalAirport,
                        airline,
                        flightNumber,
                        status,
                        notes
                    )
                } else {
                    onUpdate(
                        existing.id,
                        bookingId,
                        departureDate,
                        returnDate,
                        departureTime,
                        returnTime,
                        departureAirport,
                        arrivalAirport,
                        airline,
                        flightNumber,
                        status,
                        notes
                    )
                }

                showDialog = false
                editingSchedule = null
            }
        )
    }
}

@Composable
private fun TravelScheduleDialog(
    bookings: List<Booking>,
    existingSchedule: TravelSchedule?,
    onDismiss: () -> Unit,
    onSave: (
        Int, String, String, String, String, String,
        String, String, String, String, String
    ) -> Unit
) {
    var selectedBooking by remember {
        mutableStateOf(
            bookings.firstOrNull {
                it.id == existingSchedule?.bookingId
            }
        )
    }

    var departureDate by remember {
        mutableStateOf(existingSchedule?.departureDate ?: "")
    }
    var returnDate by remember {
        mutableStateOf(existingSchedule?.returnDate ?: "")
    }
    var departureTime by remember {
        mutableStateOf(existingSchedule?.departureTime ?: "")
    }
    var returnTime by remember {
        mutableStateOf(existingSchedule?.returnTime ?: "")
    }
    var departureAirport by remember {
        mutableStateOf(existingSchedule?.departureAirport ?: "")
    }
    var arrivalAirport by remember {
        mutableStateOf(existingSchedule?.arrivalAirport ?: "")
    }
    var airline by remember {
        mutableStateOf(existingSchedule?.airline ?: "")
    }
    var flightNumber by remember {
        mutableStateOf(existingSchedule?.flightNumber ?: "")
    }
    var status by remember {
        mutableStateOf(existingSchedule?.status ?: "Scheduled")
    }
    var notes by remember {
        mutableStateOf(existingSchedule?.notes ?: "")
    }

    var bookingExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val statuses = listOf(
        "Scheduled",
        "Confirmed",
        "Completed",
        "Cancelled"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingSchedule == null) {
                    "Add travel schedule"
                } else {
                    "Edit travel schedule"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { bookingExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedBooking?.let {
                                "${it.ref} - ${it.customer}"
                            } ?: "Select booking"
                        )
                    }

                    DropdownMenu(
                        expanded = bookingExpanded,
                        onDismissRequest = { bookingExpanded = false }
                    ) {
                        bookings.forEach { booking ->
                            DropdownMenuItem(
                                text = {
                                    Text("${booking.ref} - ${booking.customer}")
                                },
                                onClick = {
                                    selectedBooking = booking
                                    bookingExpanded = false
                                }
                            )
                        }
                    }
                }

                ScheduleField("Departure date (YYYY-MM-DD)", departureDate) {
                    departureDate = it
                }

                ScheduleField("Return date (YYYY-MM-DD)", returnDate) {
                    returnDate = it
                }

                ScheduleField("Departure time (HH:mm:ss)", departureTime) {
                    departureTime = it
                }

                ScheduleField("Return time (HH:mm:ss)", returnTime) {
                    returnTime = it
                }

                ScheduleField("Departure airport", departureAirport) {
                    departureAirport = it
                }

                ScheduleField("Arrival airport", arrivalAirport) {
                    arrivalAirport = it
                }

                ScheduleField("Airline", airline) {
                    airline = it
                }

                ScheduleField("Flight number", flightNumber) {
                    flightNumber = it
                }

                Box {
                    OutlinedButton(
                        onClick = { statusExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Status: $status")
                    }

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statuses.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    status = item
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                ScheduleField("Notes (optional)", notes) {
                    notes = it
                }

                if (error.isNotBlank()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = bookings.isNotEmpty(),
                onClick = {
                    error = when {
                        selectedBooking == null ->
                            "Please select a booking."

                        !isValidDate(departureDate) ->
                            "Enter a valid departure date."

                        !isValidDate(returnDate) ->
                            "Enter a valid return date."

                        returnDate < departureDate ->
                            "Return date cannot be before departure date."

                        !isValidTime(departureTime) ->
                            "Enter departure time as HH:mm or HH:mm:ss."

                        !isValidTime(returnTime) ->
                            "Enter return time as HH:mm or HH:mm:ss."

                        else -> ""
                    }

                    if (error.isBlank()) {
                        onSave(
                            selectedBooking!!.id,
                            departureDate,
                            returnDate,
                            normaliseTime(departureTime),
                            normaliseTime(returnTime),
                            departureAirport,
                            arrivalAirport,
                            airline,
                            flightNumber,
                            status,
                            notes
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun isValidDate(value: String): Boolean {
    if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        return false
    }

    return try {
        val formatter = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.US
        )
        formatter.isLenient = false
        val date = formatter.parse(value)
        date != null && formatter.format(date) == value
    } catch (e: Exception) {
        false
    }
}

private fun isValidTime(value: String): Boolean {
    if (value.isBlank()) return true

    if (!value.matches(Regex("\\d{2}:\\d{2}(:\\d{2})?"))) {
        return false
    }

    return try {
        val parts = value.split(":").map { it.toInt() }
        val hour = parts[0]
        val minute = parts[1]
        val second = if (parts.size == 3) parts[2] else 0

        hour in 0..23 && minute in 0..59 && second in 0..59
    } catch (e: Exception) {
        false
    }
}

private fun normaliseTime(value: String): String {
    if (value.isBlank()) return ""
    return if (value.length == 5) "$value:00" else value
}

@Composable
private fun ScheduleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
