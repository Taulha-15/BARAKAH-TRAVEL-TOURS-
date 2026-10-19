package com.barakahtravel.staff

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class Entry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String = "",
    val status: String = "Active",
    val detail: String = ""
)

private val sections = listOf("Customers", "Applications", "Documents", "Packages", "Bookings", "Payments", "Travel Schedule")
private val statuses = listOf("Active", "Pending", "Approved", "Rejected", "Under Review", "Paid", "Unpaid", "Confirmed", "Cancelled", "Completed")

class StaffAdmin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BarakahApp(this) } }
    }
}

@Composable
private fun BarakahApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("barakah_local_demo", Context.MODE_PRIVATE) }
    var signedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf("Dashboard") }
    var query by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Entry?>(null) }
    var creating by remember { mutableStateOf(false) }
    var formTitle by remember { mutableStateOf("") }
    var formSubtitle by remember { mutableStateOf("") }
    var formStatus by remember { mutableStateOf("Pending") }
    var formDetail by remember { mutableStateOf("") }
    var settingsName by remember { mutableStateOf(prefs.getString("staff_name", "Staff Member") ?: "Staff Member") }
    var settingsBranch by remember { mutableStateOf(prefs.getString("branch", "Main Office") ?: "Main Office") }

    fun defaults(): MutableMap<String, MutableList<Entry>> = mutableMapOf(
        "Customers" to mutableListOf(
            Entry(title="Amina Jacobs", subtitle="amina@example.com · 071 234 5678", status="Active", detail="Passport: Demo P1234567\nNationality: South African\nAddress: Cape Town"),
            Entry(title="Yusuf Khan", subtitle="yusuf@example.com · 082 555 0192", status="Active", detail="Passport: Demo P7654321\nNationality: South African\nAddress: Johannesburg"),
            Entry(title="Fatima Daniels", subtitle="fatima@example.com · 079 123 8844", status="Pending", detail="Passport: Not provided\nNationality: South African\nAddress: Durban")
        ),
        "Applications" to mutableListOf(
            Entry(title="APP-1001", subtitle="Amina Jacobs · Umrah Package", status="Under Review", detail="Submitted: Demo record\nStaff notes:"),
            Entry(title="APP-1002", subtitle="Yusuf Khan · Hajj Package", status="Pending", detail="Submitted: Demo record\nStaff notes:")
        ),
        "Documents" to mutableListOf(
            Entry(title="Passport copy — Amina Jacobs", subtitle="APP-1001", status="Pending", detail="Document type: Passport copy\nReview notes:"),
            Entry(title="Application form — Yusuf Khan", subtitle="APP-1002", status="Approved", detail="Document type: Application form\nReview notes:")
        ),
        "Packages" to mutableListOf(
            Entry(title="Umrah Essential", subtitle="10 days · From R 24,999", status="Active", detail="Destination: Makkah and Madinah\nDuration: 10 days\nPrice: R 24999"),
            Entry(title="Hajj Group Package", subtitle="18 days · From R 89,999", status="Active", detail="Destination: Makkah and Madinah\nDuration: 18 days\nPrice: R 89999")
        ),
        "Bookings" to mutableListOf(
            Entry(title="BK-2001", subtitle="Amina Jacobs · Umrah Essential", status="Confirmed", detail="Travel date: To be confirmed\nBooking notes:"),
            Entry(title="BK-2002", subtitle="Yusuf Khan · Hajj Group Package", status="Pending", detail="Travel date: To be confirmed\nBooking notes:")
        ),
        "Payments" to mutableListOf(
            Entry(title="PAY-3001", subtitle="BK-2001 · R 12,500", status="Paid", detail="Amount: R 12500\nMethod: Demo\nReference: DEMO-3001"),
            Entry(title="PAY-3002", subtitle="BK-2002 · R 20,000", status="Unpaid", detail="Amount: R 20000\nMethod: Not recorded\nReference:")
        ),
        "Travel Schedule" to mutableListOf(
            Entry(title="Umrah Group Departure", subtitle="Date to be confirmed · Makkah/Madinah", status="Pending", detail="Meeting point: Main Office\nCoordinator: Staff Member"),
            Entry(title="Hajj Group Departure", subtitle="Date to be confirmed · Makkah/Madinah", status="Pending", detail="Meeting point: Main Office\nCoordinator: Staff Member")
        )
    )
    fun loadData(): MutableMap<String, MutableList<Entry>> {
        val raw = prefs.getString("records", null) ?: return defaults()
        return try {
            val root = JSONObject(raw)
            sections.associateWith { section ->
                val arr = root.optJSONArray(section) ?: JSONArray()
                MutableList(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    Entry(o.optString("id", UUID.randomUUID().toString()), o.optString("title"), o.optString("subtitle"), o.optString("status", "Pending"), o.optString("detail"))
                }
            }.toMutableMap()
        } catch (_: Exception) { defaults() }
    }
    var records by remember { mutableStateOf(loadData()) }
    fun save() {
        val root = JSONObject()
        records.forEach { (section, entries) ->
            val arr = JSONArray()
            entries.forEach { e ->
                arr.put(JSONObject().put("id", e.id).put("title", e.title).put("subtitle", e.subtitle)
                    .put("status", e.status).put("detail", e.detail))
            }
            root.put(section, arr)
        }
        prefs.edit().putString("records", root.toString()).apply()
    }
    fun openNew(section: String) {
        selected = null; creating = true
        formTitle = ""; formSubtitle = ""; formStatus = if (section == "Customers") "Active" else "Pending"; formDetail = ""
        screen = "Edit:$section"
    }
    fun openEdit(section: String, entry: Entry) {
        selected = entry; creating = false
        formTitle = entry.title; formSubtitle = entry.subtitle; formStatus = entry.status; formDetail = entry.detail
        screen = "Edit:$section"
    }
    fun persistEntry(section: String) {
        if (formTitle.isBlank()) { notice = "A name/title is required."; return }
        val updated = Entry(selected?.id ?: UUID.randomUUID().toString(), formTitle.trim(), formSubtitle.trim(), formStatus, formDetail.trim())
        val list = records[section] ?: mutableListOf()
        records = records.toMutableMap().also { map ->
            map[section] = list.toMutableList().apply {
                val index = indexOfFirst { it.id == updated.id }
                if (index >= 0) set(index, updated) else add(0, updated)
            }
        }
        save(); notice = "$section record saved locally."; screen = section; selected = null; creating = false
    }

    if (!signedIn) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Barakah Travel & Tours", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Staff administration prototype", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(username, { username = it }, label = { Text("Staff username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    signedIn = true; notice = "Demo mode: this login is not authenticated by a server."
                } else notice = "Enter a username and password to continue."
            }, modifier = Modifier.fillMaxWidth()) { Text("Sign in to demo") }
            Spacer(Modifier.height(12.dp))
            Text("Local prototype only. Use sample information, not real customer data.", style = MaterialTheme.typography.bodySmall)
            if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBarCompat(
                title = if (screen.startsWith("Edit:")) "Edit ${screen.removePrefix("Edit:")}" else screen,
                onHome = { screen = "Dashboard"; query = "" },
                onLogout = { signedIn = false; password = ""; screen = "Dashboard" }
            )
        },
        bottomBar = {
            NavigationBar {
                listOf("Dashboard", "Customers", "Applications", "More").forEach { item ->
                    NavigationBarItem(
                        selected = (screen == item || (item == "More" && screen in listOf("Documents","Packages","Bookings","Payments","Travel Schedule","Reports","Settings"))),
                        onClick = { screen = item; query = "" },
                        icon = { Text(when(item) { "Dashboard" -> "⌂"; "Customers" -> "♙"; "Applications" -> "▤"; else -> "•••" }) },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (notice.isNotBlank()) {
                Text(notice, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 6.dp))
            }
            when {
                screen == "Dashboard" -> {
                    Text("Welcome, ${settingsName.ifBlank { "Staff Member" }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Local demo workspace · ${settingsBranch}")
                    Spacer(Modifier.height(12.dp))
                    val totalCustomers = records["Customers"]?.size ?: 0
                    val pendingApps = records["Applications"]?.count { it.status in listOf("Pending","Under Review") } ?: 0
                    val pendingDocs = records["Documents"]?.count { it.status == "Pending" || it.status == "Under Review" } ?: 0
                    val unpaid = records["Payments"]?.count { it.status == "Unpaid" || it.status == "Pending" } ?: 0
                    listOf("Customers" to totalCustomers, "Applications needing review" to pendingApps, "Documents awaiting review" to pendingDocs, "Unpaid/pending payments" to unpaid).forEach { (label, count) ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, Modifier.weight(1f)); Text("$count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Management areas", style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        items(sections) { section ->
                            ListItem(headlineContent = { Text(section) }, supportingContent = { Text("${records[section]?.size ?: 0} local records") },
                                modifier = Modifier.clickable { screen = section })
                        }
                        item { ListItem(headlineContent = { Text("Reports") }, modifier = Modifier.clickable { screen = "Reports" }) }
                        item { ListItem(headlineContent = { Text("Settings") }, modifier = Modifier.clickable { screen = "Settings" }) }
                    }
                }
                screen == "More" -> {
                    Text("More tools", style = MaterialTheme.typography.titleLarge)
                    listOf("Documents", "Packages", "Bookings", "Payments", "Travel Schedule", "Reports", "Settings").forEach { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { screen = item }) {
                            ListItem(headlineContent = { Text(item) }, supportingContent = { Text("Open $item management") })
                        }
                    }
                }
                screen == "Reports" -> {
                    Text("Operational summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    sections.forEach { section ->
                        val list = records[section] ?: emptyList()
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(section, fontWeight = FontWeight.SemiBold)
                                Text("Total: ${list.size} · Active/approved/paid: ${list.count { it.status in listOf("Active","Approved","Paid","Confirmed","Completed") }}")
                                Text("Pending/review/unpaid: ${list.count { it.status in listOf("Pending","Under Review","Unpaid") }}")
                            }
                        }
                    }
                    Text("Reports are calculated from this device's demo records, not the website database.", style = MaterialTheme.typography.bodySmall)
                }
                screen == "Settings" -> {
                    Text("Staff settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(settingsName, { settingsName = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(settingsBranch, { settingsBranch = it }, label = { Text("Branch / office") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { prefs.edit().putString("staff_name", settingsName).putString("branch", settingsBranch).apply(); notice = "Settings saved on this device." }) { Text("Save settings") }
                    OutlinedButton(onClick = { records = defaults(); save(); notice = "Sample records restored. Your saved staff settings were kept." }) { Text("Restore sample records") }
                    Text("The app currently uses local device storage. API/database integration and secure staff authentication are intentionally deferred.", style = MaterialTheme.typography.bodySmall)
                }
                screen.startsWith("Edit:") -> {
                    val section = screen.removePrefix("Edit:")
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(if (creating) "Create $section record" else "Update $section record", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(formTitle, { formTitle = it }, label = { Text(if (section == "Customers") "Customer name *" else "Name / title *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(formSubtitle, { formSubtitle = it }, label = { Text("Contact / summary") }, modifier = Modifier.fillMaxWidth())
                        Text("Status", modifier = Modifier.padding(top = 8.dp))
                        var statusMenu by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { statusMenu = true }) { Text(formStatus) }
                            DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                                statuses.forEach { status ->
                                    DropdownMenuItem(text = { Text(status) }, onClick = { formStatus = status; statusMenu = false })
                                }
                            }
                        }
                        OutlinedTextField(formDetail, { formDetail = it }, label = { Text("Details / notes") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), minLines = 4)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { persistEntry(section) }) { Text("Save record") }
                            OutlinedButton(onClick = { screen = section; selected = null }) { Text("Cancel") }
                        }
                    }
                }
                screen in sections -> {
                    val list = records[screen] ?: emptyList()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(screen, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Button(onClick = { openNew(screen) }) { Text("Add +") }
                    }
                    OutlinedTextField(query, { query = it }, label = { Text("Search $screen") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    if (screen == "Customers") {
                        Text("Customer registration codes can be generated for demonstration only.", style = MaterialTheme.typography.bodySmall)
                    }
                    LazyColumn {
                        items(list.filter { "${it.title} ${it.subtitle} ${it.status} ${it.detail}".contains(query, true) }, key = { it.id }) { entry ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    if (entry.subtitle.isNotBlank()) Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium)
                                    Text("Status: ${entry.status}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    if (entry.detail.isNotBlank()) Text(entry.detail, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { openEdit(screen, entry) }) { Text("View / edit") }
                                        if (screen == "Customers") TextButton(onClick = {
                                            val code = "BAR-" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase(Locale.ROOT)
                                            notice = "Demo registration code for ${entry.title}: $code (local only; not valid on the website)."
                                        }) { Text("Generate code") }
                                        TextButton(onClick = {
                                            records = records.toMutableMap().also { map -> map[screen] = (map[screen] ?: mutableListOf()).filterNot { it.id == entry.id }.toMutableList() }
                                            save(); notice = "Record deleted locally."
                                        }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Text("Choose a section from Dashboard or More.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarCompat(title: String, onHome: () -> Unit, onLogout: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { TextButton(onClick = onHome) { Text("Home") } },
        actions = { TextButton(onClick = onLogout) { Text("Log out") } }
    )
}
