package com.barakahtravel.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    staffName: String,
    staffEmail: String = "staff@barakahtravel.com",
    darkMode: Boolean = false,
    notifications: Boolean = true,
    onDarkModeChange: (Boolean) -> Unit = {},
    onNotificationsChange: (Boolean) -> Unit = {},
    onUpdateName: (String) -> Unit = {},
    onChangePassword: (String) -> String? = { null },
    onLogout: () -> Unit = {}
) {
    var nameEdit by remember(staffName) { mutableStateOf(staffName) }
    var newPw by remember { mutableStateOf("") }
    var pwMsg by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Settings", fontSize = 27.sp, color = Forest, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Staff profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Email: $staffEmail")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nameEdit,
                    onValueChange = { nameEdit = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()

                )

                Button(
                    onClick = { onUpdateName(nameEdit.trim()) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Update display name")
                }

            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark mode", Modifier.weight(1f))
                    Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", Modifier.weight(1f))


                    Switch(
                        checked = notifications,
                        onCheckedChange = onNotificationsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )


                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(18.dp)) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Account security",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Your staff account uses secure Google Sign-In. " +
                                    "Your password is managed through your Google account.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
                Spacer(Modifier.height(20.dp))
                Text("Barakah Staff • v1.0", color = Muted, fontSize = 12.sp)
            }
        }
    }
}