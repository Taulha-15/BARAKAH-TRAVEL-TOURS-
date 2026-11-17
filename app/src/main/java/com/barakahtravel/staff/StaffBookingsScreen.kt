
package com.barakahtravel.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StaffBookingsScreen(
    bookings: List<Booking>,
    applications: List<TravelApplication>,
    onCreateBooking: (Int, String) -> Unit,
    onUpdateStatus: (Booking, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedApplication by remember { mutableStateOf<TravelApplication?>(null) }
    var notes by remember { mutableStateOf("") }

    val statuses = listOf("Pending", "Confirmed", "Cancelled", "Completed")

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
                    "Bookings",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Text(
                    "${bookings.size} total bookings",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest
                )
            ) {
                Text("+ New")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Text(
                "No bookings have been created yet.",
                color = Muted,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings, key = { it.id }) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                booking.customer,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Reference: ${booking.ref}")
                            Text("Package: ${booking.packageName}")
                            Text("Booking date: ${booking.bookingDate}")
                            Text("Total: R${"%.2f".format(booking.totalAmount)}")

                            if (!booking.notes.isNullOrBlank()) {
                                Text("Notes: ${booking.notes}")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Booking status",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            var expanded by remember(booking.id) {
                                mutableStateOf(false)
                            }

                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true }
                                ) {
                                    Text(booking.status)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("▼")
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    statuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                expanded = false
                                                if (status != booking.status) {
                                                    onUpdateStatus(booking, status)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val approvedApplications = applications.filter {
            it.status.equals("Approved", ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                selectedApplication = null
                notes = ""
            },
            title = { Text("Create a booking") },
            text = {
                Column {
                    Text("Select an approved application:")

                    Spacer(modifier = Modifier.height(8.dp))

                    if (approvedApplications.isEmpty()) {
                        Text(
                            "There are no approved applications available to book.",
                            color = Muted
                        )
                    } else {
                        approvedApplications.forEach { application ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedApplication?.id == application.id,
                                    onClick = {
                                        selectedApplication = application
                                    }
                                )

                                Column {
                                    Text(
                                        application.customer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        application.packageName,
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedApplication != null,
                    onClick = {
                        selectedApplication?.let {
                            onCreateBooking(it.id, notes)
                        }
                        showCreateDialog = false
                        selectedApplication = null
                        notes = ""
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        selectedApplication = null
                        notes = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
