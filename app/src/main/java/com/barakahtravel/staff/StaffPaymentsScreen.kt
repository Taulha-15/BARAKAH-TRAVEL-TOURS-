
package com.barakahtravel.staff

import androidx.compose.foundation.layout.*
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffPaymentsScreen(
    payments: List<Payment>,
    bookings: List<Booking>,
    onCreatePayment: (
        Int, Double, String, String, String, String
    ) -> Unit,
    onUpdateStatus: (Payment, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val statuses = listOf("Recorded", "Pending", "Verified", "Rejected")

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
                    "Payments",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Text(
                    "${payments.size} recorded payments",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest
                )
            ) {
                Text("+ Record")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (payments.isEmpty()) {
            Text(
                "No payments have been recorded yet.",
                color = Muted,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(payments, key = { it.id }) { payment ->
                    var expanded by remember(payment.id) {
                        mutableStateOf(false)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                payment.customer,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Booking: ${payment.bookingReference}")
                            Text("Amount: R${"%.2f".format(payment.amount)}")
                            Text("Payment date: ${payment.paymentDate}")
                            Text("Method: ${payment.paymentMethod ?: "Not specified"}")
                            Text("Reference: ${payment.referenceNumber ?: "Not supplied"}")

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Payment status",
                                fontWeight = FontWeight.SemiBold
                            )

                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true }
                                ) {
                                    Text(payment.status)
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
                                                if (status != payment.status) {
                                                    onUpdateStatus(payment, status)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            if (!payment.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Notes: ${payment.notes}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RecordPaymentDialog(
            bookings = bookings,
            onDismiss = { showDialog = false },
            onSave = { bookingId, amount, date, method, reference, notes ->
                onCreatePayment(
                    bookingId, amount, date, method, reference, notes
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun RecordPaymentDialog(
    bookings: List<Booking>,
    onDismiss: () -> Unit,
    onSave: (
        Int, Double, String, String, String, String
    ) -> Unit
) {
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var amount by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
    var method by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var bookingExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record a payment") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bookings.isEmpty()) {
                    Text(
                        "No bookings are available. Create a booking first.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
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
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (R)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Payment method") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotBlank()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = bookings.isNotEmpty(),
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()

                    error = when {
                        selectedBooking == null ->
                            "Please select a booking."
                        parsedAmount == null || parsedAmount <= 0 ->
                            "Enter a valid payment amount."
                        !paymentDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) ->
                            "Enter the date as YYYY-MM-DD."
                        else -> ""
                    }

                    if (error.isBlank()) {
                        onSave(
                            selectedBooking!!.id,
                            parsedAmount!!,
                            paymentDate,
                            method,
                            reference,
                            notes
                        )
                    }
                }
            ) {
                Text("Save payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
