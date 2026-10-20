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

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
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
                Spacer(Modifier.height(10.dp))
                Button(onClick = { onUpdateName(nameEdit.trim()) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Update display name")
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp)) {
                Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark mode", Modifier.weight(1f))
                    Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", Modifier.weight(1f))
                    Switch(checked = notifications, onCheckedChange = onNotificationsChange)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp)) {
                Text("Change password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPw,
                    onValueChange = { newPw = it },
                    label = { Text("New password") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (pwMsg.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(pwMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    val err = onChangePassword(newPw)
                    pwMsg = err ?: "Password updated"
                    if (err == null) newPw = ""
                }, modifier = Modifier.fillMaxWidth()) { Text("Update password") }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        Spacer(Modifier.height(20.dp))
        Text("Barakah Staff • v1.0", color = Muted, fontSize = 12.sp)
    }
}
