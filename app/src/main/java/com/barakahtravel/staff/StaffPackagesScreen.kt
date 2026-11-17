
package com.barakahtravel.staff

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun StaffPackagesScreen(
    packages: List<TravelPackage>,
    onAdd: (TravelPackage) -> Unit,
    onUpdate: (TravelPackage) -> Unit,
    onToggleActive: (TravelPackage) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<TravelPackage?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Umrah Packages",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Text(
            "Manage the travel packages offered by Barakah Travel.",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                selectedPackage = null
                showForm = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("＋ Add New Package")
        }

        Spacer(Modifier.height(14.dp))

        if (packages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Mint
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "No packages found",
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add an Umrah package to get started.",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(packages, key = { it.id }) { travelPackage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                travelPackage.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                travelPackage.description ?: "No description provided.",
                                fontSize = 13.sp,
                                color = Muted
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "Price: R${"%.2f".format(travelPackage.price)}",
                                fontWeight = FontWeight.Bold,
                                color = Green
                            )

                            Text(
                                "Duration: ${travelPackage.durationDays} days",
                                color = Muted,
                                fontSize = 13.sp
                            )

                            Text(
                                "Departure: ${travelPackage.departureDate ?: "Not set"}",
                                color = Muted,
                                fontSize = 12.sp
                            )

                            Text(
                                "Return: ${travelPackage.returnDate ?: "Not set"}",
                                color = Muted,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            Surface(
                                color = if (travelPackage.isActive)
                                    Color(0xFFDFF2E5)
                                else
                                    Color(0xFFFFEFD8),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    if (travelPackage.isActive) "Active" else "Inactive",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    color = if (travelPackage.isActive)
                                        Color(0xFF226641)
                                    else
                                        Color(0xFF8A5715),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedPackage = travelPackage
                                        showForm = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit")
                                }

                                Button(
                                    onClick = {
                                        onToggleActive(travelPackage)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (travelPackage.isActive)
                                            "Deactivate"
                                        else
                                            "Activate"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        PackageFormDialog(
            existingPackage = selectedPackage,
            onDismiss = { showForm = false },
            onSave = { updatedPackage ->
                if (selectedPackage == null) {
                    onAdd(updatedPackage)
                } else {
                    onUpdate(updatedPackage)
                }
                showForm = false
            }
        )
    }
}

@Composable
private fun PackageFormDialog(
    existingPackage: TravelPackage?,
    onDismiss: () -> Unit,
    onSave: (TravelPackage) -> Unit
) {
    var name by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.name ?: "")
    }
    var description by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.description ?: "")
    }
    var price by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.price?.toString() ?: "")
    }
    var duration by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.durationDays?.toString() ?: "")
    }
    var departureDate by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.departureDate ?: "")
    }
    var returnDate by remember(existingPackage?.id) {
        mutableStateOf(existingPackage?.returnDate ?: "")
    }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingPackage == null) "Add Umrah Package"
                else "Edit Umrah Package",
                color = Forest,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                androidx.compose.foundation.rememberScrollState().let { scrollState ->
                    Column(
                        Modifier.verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Package name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price (R)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration in days") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = departureDate,
                            onValueChange = { departureDate = it },
                            label = { Text("Departure date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = returnDate,
                            onValueChange = { returnDate = it },
                            label = { Text("Return date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (error.isNotBlank()) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull()
                    val parsedDuration = duration.toIntOrNull()

                    when {
                        name.isBlank() -> error = "Enter a package name."
                        parsedPrice == null || parsedPrice <= 0 ->
                            error = "Enter a valid package price."
                        parsedDuration == null || parsedDuration <= 0 ->
                            error = "Enter a valid duration."
                        else -> {
                            onSave(
                                TravelPackage(
                                    id = existingPackage?.id ?: 0,
                                    name = name.trim(),
                                    description = description.trim().ifBlank { null },
                                    price = parsedPrice,
                                    durationDays = parsedDuration,
                                    departureDate = departureDate.trim().ifBlank { null },
                                    returnDate = returnDate.trim().ifBlank { null },
                                    isActive = existingPackage?.isActive ?: true
                                )
                            )
                        }
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
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
