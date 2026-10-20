package com.barakahtravel.staff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

val Forest = Color(0xFF174C3C)
val Green = Color(0xFF267A58)
val Mint = Color(0xFFEAF4EE)
val Sand = Color(0xFFF8F7F2)
val Ink = Color(0xFF1F2B25)
val Muted = Color(0xFF6D7972)
val Orange = Color(0xFFB66B20)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BarakahApp() }
    }
}

@Composable
fun BarakahApp() {
    var signedIn by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf("Dashboard") }
    var staffName by remember { mutableStateOf("Staff Member") }
    var search by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var notice by remember { mutableStateOf("") }

    val customers = remember {
        mutableStateListOf(
            Customer(1001, "Amina Jacobs", "amina@example.com", "082 555 0101", "Verified"),
            Customer(1002, "Yusuf Daniels", "yusuf@example.com", "082 555 0102", "Pending"),
            Customer(1003, "Fatima Khan", "fatima@example.com", "082 555 0103", "Pending"),
            Customer(1004, "Ibrahim Williams", "ibrahim@example.com", "082 555 0104", "Verified"),
            Customer(1005, "Maryam Petersen", "maryam@example.com", "082 555 0105", "Review")
        )
    }
    val applications = remember {
        mutableStateListOf(
            TravelApplication(501, "Amina Jacobs", "Essential Umrah", "Approved"),
            TravelApplication(502, "Yusuf Daniels", "Premium Umrah", "Under Review"),
            TravelApplication(503, "Fatima Khan", "Family Umrah", "Pending")
        )
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            secondary = Green,
            background = Sand,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = Ink
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Sand) {
            if (!signedIn) {
                LoginScreen(
                    onLogin = { username ->
                        staffName = username.trim().ifBlank { "Staff Member" }
                        signedIn = true
                    }
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    TopBar(staffName = staffName, onLogout = {
                        signedIn = false
                        page = "Dashboard"
                        notice = ""
                    })
                    if (notice.isNotBlank()) {
                        Row(
                            Modifier.fillMaxWidth().background(Mint).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notice, Modifier.weight(1f), color = Forest, fontSize = 13.sp)
                            Text("×", Modifier.clickable { notice = "" }, color = Forest, fontSize = 20.sp)
                        }
                    }
                    when (page) {
                        "Dashboard" -> DashboardScreen(
                            customers.size, applications.size,
                            onNavigate = { page = it }
                        )
                        "Customers" -> CustomersScreen(
                            customers = customers,
                            search = search,
                            onSearch = { search = it },
                            onEdit = { selectedCustomer = it; page = "Edit Customer" },
                            onGenerate = { c ->
                                val code = "BRK-" + UUID.randomUUID().toString()
                                    .replace("-", "").take(8).uppercase()
                                val i = customers.indexOfFirst { it.id == c.id }
                                if (i >= 0) customers[i] = customers[i].copy(code = code)
                                notice = "Demo registration code created: $code (not saved to a server)."
                            }
                        )
                        "Edit Customer" -> {
                            val current = selectedCustomer
                            if (current == null) {
                                EmptyState("No customer selected", "Return to Customers and choose a record.") {
                                    page = "Customers"
                                }
                            } else {
                                EditCustomerScreen(current, onCancel = { page = "Customers" }, onSave = { updated ->
                                    val i = customers.indexOfFirst { it.id == updated.id }
                                    if (i >= 0) customers[i] = updated
                                    selectedCustomer = updated
                                    notice = "Changes saved locally for this demo only."
                                    page = "Customers"
                                })
                            }
                        }
                        "Applications" -> ApplicationsScreen(applications) { id, status ->
                            val i = applications.indexOfFirst { it.id == id }
                            if (i >= 0) applications[i] = applications[i].copy(status = status)
                            notice = "Application status changed locally. It is not saved to the database."
                        }
                        "Documents" -> GenericListScreen(
                            "Document Management",
                            "Review uploaded documents and their verification status.",
                            listOf("Passport copy • Amina Jacobs • Verified",
                                "Identity document • Yusuf Daniels • Pending",
                                "Passport copy • Fatima Khan • Pending"),
                            "Document review is a demo. Secure file access will be connected to the API later."
                        )
                        "Packages" -> GenericListScreen(
                            "Umrah Packages",
                            "Package catalogue overview",
                            listOf("Essential Umrah • 10 days • R24,999",
                                "Premium Umrah • 14 days • R34,999",
                                "Family Umrah • 12 days • R29,999"),
                            "Package records are sample data and cannot change your website's database."
                        )
                        "Bookings" -> GenericListScreen(
                            "Bookings",
                            "Track customer bookings",
                            listOf("BK-2026-001 • Amina Jacobs • Confirmed",
                                "BK-2026-002 • Yusuf Daniels • Awaiting payment",
                                "BK-2026-003 • Fatima Khan • Pending"),
                            "Booking status shown here is sample data."
                        )
                        "Payments" -> GenericListScreen(
                            "Payments",
                            "Payment tracking overview",
                            listOf("Amina Jacobs • R24,999 • Paid",
                                "Yusuf Daniels • R34,999 • Deposit received",
                                "Fatima Khan • R29,999 • Awaiting payment"),
                            "No payment provider is connected. Do not record real payments here."
                        )
                        "Travel Schedule" -> GenericListScreen(
                            "Travel Schedule",
                            "Upcoming travel and departure overview",
                            listOf("12 Nov 2026 • Essential Umrah • Johannesburg",
                                "21 Nov 2026 • Premium Umrah • Cape Town",
                                "03 Dec 2026 • Family Umrah • Johannesburg"),
                            "Travel dates are illustrative sample data."
                        )
                        "Settings" -> SettingsScreen(staffName)
                        else -> DashboardScreen(customers.size, applications.size) { page = it }
                    }
                    Spacer(Modifier.weight(1f))
                    BottomNavigation(page = page, onNavigate = { page = it })
                }
            }
        }
    }
}

@Composable
fun TopBar(staffName: String, onLogout: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Forest).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).background(Color.White.copy(alpha = .14f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) { Text("B", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("BARAKAH", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("STAFF PORTAL", color = Color(0xFFC8DED1), fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
        Text("Hi, $staffName", color = Color.White, fontSize = 12.sp, maxLines = 1)
        Spacer(Modifier.width(8.dp))
        Text("Exit", color = Color.White, fontSize = 12.sp,
            modifier = Modifier.clickable { onLogout() }.padding(4.dp))
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(84.dp).background(Forest, RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
            Text("B", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        Text("BARAKAH TRAVEL", fontSize = 13.sp, color = Green, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(Modifier.height(8.dp))
        Text("Staff Admin Portal", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Forest)
        Spacer(Modifier.height(8.dp))
        Text("Manage customer journeys with care.", color = Muted)
        Spacer(Modifier.height(28.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text("Staff sign in", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Demo mode — authentication is not connected yet.", color = Orange, fontSize = 12.sp)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Staff username") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            error = "Enter a username and password to preview the app."
                        } else {
                            error = ""
                            onLogin(username)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Sign in to demo", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(12.dp))
                Text("Use any non-empty username and password for this local preview.",
                    color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Secure access for authorised Barakah Travel staff", color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun DashboardScreen(customerCount: Int, applicationCount: Int, onNavigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Assalamu Alaikum", color = Green, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text("Staff dashboard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Forest)
        Text("Your operations at a glance.", color = Muted)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Customers", customerCount.toString(), "Registered records", Modifier.weight(1f))
            StatCard("Applications", applicationCount.toString(), "Need tracking", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Documents", "3", "Sample records", Modifier.weight(1f))
            StatCard("Bookings", "3", "Sample records", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Text("QUICK ACCESS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        val items = listOf(
            Triple("Customers", "View, edit and generate demo registration codes", "♙"),
            Triple("Applications", "Review application statuses", "▤"),
            Triple("Documents", "Check document verification", "▣"),
            Triple("Packages", "View Umrah package catalogue", "✧"),
            Triple("Bookings", "Track booking status", "▦"),
            Triple("Payments", "Review payment overview", "R"),
            Triple("Travel Schedule", "See upcoming departures", "◷"),
            Triple("Settings", "App information and demo notes", "⚙")
        )
        items.forEach { (title, subtitle, symbol) ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNavigate(title) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(Mint, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text(symbol, color = Forest, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
                        Text(subtitle, fontSize = 12.sp, color = Muted)
                    }
                    Text("›", color = Green, fontSize = 26.sp)
                }
            }
        }
        DemoBanner()
    }
}

@Composable
fun StatCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, color = Forest, fontWeight = FontWeight.Bold, fontSize = 27.sp)
            Text(detail, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    search: String,
    onSearch: (String) -> Unit,
    onEdit: (Customer) -> Unit,
    onGenerate: (Customer) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Customer management", fontSize = 25.sp, color = Forest, fontWeight = FontWeight.Bold)
        Text("Search customer records and manage demo registration codes.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = search, onValueChange = onSearch, label = { Text("Search name, email or ID") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text("${customers.count { it.name.contains(search, true) || it.email.contains(search, true) || it.id.toString().contains(search) }} customer records",
            color = Muted, fontSize = 12.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(customers.filter {
                it.name.contains(search, true) || it.email.contains(search, true) || it.id.toString().contains(search)
            }, key = { it.id }) { customer ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(Mint, CircleShape), contentAlignment = Alignment.Center) {
                                Text(customer.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                                    color = Forest, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(customer.name, fontWeight = FontWeight.Bold)
                                Text("Customer #${customer.id}", fontSize = 12.sp, color = Muted)
                            }
                            StatusPill(customer.status)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(customer.email, fontSize = 13.sp)
                        Text(customer.phone, fontSize = 13.sp, color = Muted)
                        if (customer.code.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Demo code: ${customer.code}", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(customer) }, modifier = Modifier.weight(1f)) {
                                Text("Edit details", fontSize = 12.sp)
                            }
                            Button(onClick = { onGenerate(customer) }, modifier = Modifier.weight(1f)) {
                                Text(if (customer.code.isBlank()) "Generate code" else "Regenerate", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditCustomerScreen(customer: Customer, onCancel: () -> Unit, onSave: (Customer) -> Unit) {
    var name by remember(customer.id) { mutableStateOf(customer.name) }
    var email by remember(customer.id) { mutableStateOf(customer.email) }
    var phone by remember(customer.id) { mutableStateOf(customer.phone) }
    var status by remember(customer.id) { mutableStateOf(customer.status) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Edit customer", fontSize = 27.sp, color = Forest, fontWeight = FontWeight.Bold)
        Text("Customer #${customer.id} • Demo changes only", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone number") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text("Customer status", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Pending", "Review", "Verified").forEach {
                FilterChip(selected = status == it, onClick = { status = it }, label = { Text(it) })
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            if (name.isNotBlank() && email.isNotBlank()) onSave(customer.copy(
                name = name.trim(), email = email.trim(), phone = phone.trim(), status = status
            ))
        }, modifier = Modifier.fillMaxWidth()) { Text("Save demo changes") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        Spacer(Modifier.height(10.dp))
        Text("Saving here changes only temporary app memory. Database saving will be added in the integration phase.",
            color = Orange, fontSize = 12.sp)
    }
}

@Composable
fun ApplicationsScreen(applications: List<TravelApplication>, onStatusChange: (Int, String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Applications", fontSize = 27.sp, color = Forest, fontWeight = FontWeight.Bold)
        Text("Track and review customer Umrah applications.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(applications, key = { it.id }) { app ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Text("Application #${app.id}", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(app.customer, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(app.packageName, color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        StatusPill(app.status)
                        Spacer(Modifier.height(10.dp))
                        Text("Update demo status", fontSize = 12.sp, color = Muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Pending", "Under Review", "Approved").forEach { status ->
                                TextButton(onClick = { onStatusChange(app.id, status) }) {
                                    Text(status, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenericListScreen(title: String, subtitle: String, records: List<String>, note: String) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text(title, fontSize = 27.sp, color = Forest, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(records) { record ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(Green, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(record, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Mint), shape = RoundedCornerShape(14.dp)) {
            Text(note, Modifier.padding(14.dp), color = Forest, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val bg = when (status.lowercase()) {
        "verified", "approved", "confirmed", "paid" -> Color(0xFFDFF2E5)
        "pending", "awaiting payment" -> Color(0xFFFFEFD8)
        else -> Color(0xFFE8EBE9)
    }
    val fg = when (status.lowercase()) {
        "verified", "approved", "confirmed", "paid" -> Color(0xFF226641)
        "pending", "awaiting payment" -> Color(0xFF8A5715)
        else -> Muted
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(status, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DemoBanner() {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D9)), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.Top) {
            Text("!", color = Orange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.width(9.dp))
            Column {
                Text("DEMO MODE", color = Orange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Sample data only. Changes are not saved to the Barakah database.", color = Color(0xFF75552B), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, fontSize = 22.sp, color = Forest, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = Muted)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onBack) { Text("Go back") }
    }
}

@Composable
fun BottomNavigation(page: String, onNavigate: (String) -> Unit) {
    val navItems = listOf("Dashboard", "Customers", "Applications", "Settings")
    NavigationBar(containerColor = Color.White) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = page == item || (item == "Customers" && page == "Edit Customer"),
                onClick = { onNavigate(item) },
                icon = {
                    Text(when (item) {
                        "Dashboard" -> "⌂"
                        "Customers" -> "♙"
                        "Applications" -> "▤"
                        else -> "⚙"
                    }, fontSize = 19.sp)
                },
                label = { Text(item, fontSize = 10.sp) }
            )
        }
    }
}