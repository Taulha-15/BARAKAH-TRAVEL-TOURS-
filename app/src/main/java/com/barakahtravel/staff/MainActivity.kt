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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

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

        setContent {
            BarakahApp()
        }
    }
}

suspend fun generateStaffRegistrationCode(customerId: Int): String =
    withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Please sign in again.")
        val token = user.getIdToken(false).await().token
            ?: throw Exception("Could not obtain Firebase token.")

        val url = URL("${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffCustomers/$customerId/generate-code")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            val response = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            if (responseCode != 200) {
                val message = runCatching {
                    org.json.JSONObject(response).optString("message", "HTTP $responseCode")
                }.getOrDefault("HTTP $responseCode")
                throw Exception(message)
            }
            org.json.JSONObject(response).getString("registrationCode")
        } finally {
            connection.disconnect()
        }
    }

private val BarakahLightColors = lightColorScheme(
    primary = Color(0xFF18794E),
    background = Color(0xFFF6F8F6),
    surface = Color.White,
    onBackground = Color(0xFF173B2C),
    onSurface = Color(0xFF173B2C)
)

private val BarakahDarkColors = darkColorScheme(
    onPrimary = Color(0xFF10251A),
    primary = Color(0xFF81C9A1),
    background = Color(0xFF121C17),
    surface = Color(0xFF1D2B23),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BarakahApp() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Saved staff settings
    val preferences = remember {
        context.getSharedPreferences(
            "barakah_staff_settings",
            android.content.Context.MODE_PRIVATE
        )
    }

    var darkMode by remember {
        mutableStateOf(
            preferences.getBoolean("dark_mode", false)
        )
    }

    var notificationsEnabled by remember {
        mutableStateOf(
            preferences.getBoolean("notifications_enabled", true)
        )
    }

    var signedIn by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf("Dashboard") }
    var staffName by remember {
        mutableStateOf(
            preferences.getString("staff_display_name", "Staff Member")
                ?: "Staff Member"
        )
    }
    var search by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var notice by remember { mutableStateOf("") }

    val customers = remember {
        mutableStateListOf<Customer>()
    }

    var customerLoadError by remember {
        mutableStateOf("")
    }

    val applications = remember {
        mutableStateListOf<TravelApplication>()
    }

    val documents = remember {
        mutableStateListOf<StaffDocument>()
    }
    LaunchedEffect(page, signedIn) {
        if (signedIn) {

            // Load customers on Dashboard or Customers page.
            if (page == "Dashboard" || page == "Customers") {
                customerLoadError = ""

                try {
                    val realCustomers = fetchStaffCustomers()
                    customers.clear()
                    customers.addAll(realCustomers)
                } catch (e: Exception) {
                    customerLoadError =
                        e.message ?: "Unable to load customers."
                }
            }

            // Load applications on Dashboard or Applications page.
            if (page == "Dashboard" || page == "Applications") {
                try {
                    val realApplications = fetchStaffApplications()
                    applications.clear()
                    applications.addAll(realApplications)
                } catch (e: Exception) {
                    notice =
                        e.message ?: "Unable to load applications."
                }
            }

            // Load documents on Dashboard or Documents page.
            if (page == "Dashboard" || page == "Documents") {
                try {
                    val realDocuments = fetchStaffDocuments()
                    documents.clear()
                    documents.addAll(realDocuments)
                } catch (e: Exception) {
                    notice =
                        e.message ?: "Unable to load documents."
                }
            }
        }
    }



    MaterialTheme(
        colorScheme = if (darkMode) {
            BarakahDarkColors
        } else {
            BarakahLightColors
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

        if (!signedIn) {
                LoginScreen(
                    onLogin = { authorizedStaffName ->
                        staffName = preferences.getString(
                            "staff_display_name",
                            null
                        ) ?: authorizedStaffName

                        signedIn = true
                        page = "Dashboard"
                    }
                )
            } else  {
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
                    BackHandler(enabled = page != "Dashboard") {
                        page = when (page) {
                            "Edit Customer" -> "Customers"
                            else -> "Dashboard"
                        }
                    }
                    if (page != "Dashboard") {
                        TextButton(
                            onClick = {
                                page = when (page) {
                                    "Edit Customer" -> "Customers"
                                    else -> "Dashboard"
                                }
                            },
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 8.dp
                            )
                        ) {
                            Text(
                                "← Back",
                                color = Forest,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    when (page) {
                        "Dashboard" -> DashboardScreen(
                            customers.size,
                            applications.size,
                            documents.size,
                            onNavigate = { page = it }
                        )

                        "Customers" -> CustomersScreen(
                            customers = customers,
                            search = search,
                            onSearch = { search = it },
                            onEdit = { selectedCustomer = it; page = "Edit Customer" },
                            onGenerate = { c ->
                                scope.launch {
                                    try {
                                        val code = generateStaffRegistrationCode(c.id)

                                        val i = customers.indexOfFirst { it.id == c.id }
                                        if (i >= 0) {
                                            customers[i] = customers[i].copy(code = code)
                                        }

                                        notice = "Registration code ready: $code"
                                    } catch (e: Exception) {
                                        notice = e.message ?: "Unable to generate registration code."
                                    }
                                }
                            }

                        )
                        "Edit Customer" -> {
                            val current = selectedCustomer
                            if (current == null) {
                                EmptyState("No customer selected", "Return to Customers and choose a record.") {
                                    page = "Customers"
                                }
                            } else {
                                EditCustomerScreen(
                                    current,
                                    onCancel = { page = "Customers" },
                                    onSave = { updated ->
                                        scope.launch {
                                            try {
                                                updateStaffCustomer(updated)

                                                val i = customers.indexOfFirst { it.id == updated.id }
                                                if (i >= 0) customers[i] = updated

                                                selectedCustomer = updated
                                                notice = "Customer updated successfully in Azure SQL."
                                                page = "Customers"
                                            } catch (e: Exception) {
                                                notice = e.message ?: "Unable to save customer."
                                            }

                                                }
                                    }
                                )
                            }
                        }

                        "Applications" -> ApplicationsScreen(applications) { id, status ->
                            scope.launch {
                                try {
                                    updateStaffApplicationStatus(id, status)

                                    val i = applications.indexOfFirst { it.id == id }
                                    if (i >= 0) {
                                        applications[i] = applications[i].copy(status = status)
                                    }

                                    notice = "Application #$id updated to $status in Azure SQL."
                                } catch (e: Exception) {
                                    notice = e.message ?: "Unable to update application status."
                                }
                            }
                        }


                        "Documents" -> StaffDocumentsScreen(
                            documents = documents,
                            onStatusChange = { id, status ->
                                scope.launch {
                                    try {
                                        updateStaffDocumentStatus(id, status)

                                        val i = documents.indexOfFirst { it.id == id }

                                        if (i >= 0) {
                                            documents[i] = documents[i].copy(status = status)
                                        }

                                        notice = "Document #$id updated to $status in Azure SQL."

                                    } catch (e: Exception) {
                                        notice = e.message
                                            ?: "Unable to update document status."
                                    }
                                }
                            },


                            onViewFile = { id ->
                                scope.launch {
                                    try {
                                        val file = downloadStaffDocument(context, id)

                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )

                                        val mimeType = when (file.extension.lowercase()) {
                                            "pdf" -> "application/pdf"
                                            "jpg", "jpeg" -> "image/jpeg"
                                            "png" -> "image/png"
                                            else -> throw Exception("Unsupported file format.")
                                        }

                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }

                                        context.startActivity(intent)

                                    } catch (e: Exception) {
                                        notice = e.message ?: "Unable to open document."
                                    }
                                }
                            }


                        )



                        "Packages" -> EmptyState(
                            "Umrah Packages",
                            "Package management is not available in the staff app yet."
                        ) {
                            page = "Dashboard"
                        }

                        "Bookings" -> EmptyState(
                            "Bookings",
                            "Booking management is not connected to the staff API yet."
                        ) {
                            page = "Dashboard"
                        }

                        "Payments" -> EmptyState(
                            "Payments",
                            "Payment management is not connected to a payment provider."
                        ) {
                            page = "Dashboard"
                        }

                        "Travel Schedule" -> EmptyState(
                            "Travel Schedule",
                            "Travel schedule management is not available yet."
                        ) {
                            page = "Dashboard"
                        }



                        "Settings" -> SettingsScreen(
                            staffName = staffName,

                            onUpdateName = { newName ->
                                if (newName.isBlank()) {
                                    notice = "Display name cannot be empty."
                                } else {
                                    staffName = newName
                                    preferences.edit()
                                        .putString("staff_display_name", newName)
                                        .apply()
                                    notice = "Display name updated successfully."
                                }
                            },

                            staffEmail = FirebaseAuth.getInstance().currentUser?.email
                                ?: "No email available",

                            darkMode = darkMode,
                            notifications = notificationsEnabled,

                            onDarkModeChange = { enabled ->
                                darkMode = enabled
                                preferences.edit()
                                    .putBoolean("dark_mode", enabled)
                                    .apply()
                            },

                            onNotificationsChange = { enabled ->
                                notificationsEnabled = enabled
                                preferences.edit()
                                    .putBoolean("notifications_enabled", enabled)
                                    .apply()
                            },

                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                signedIn = false
                                page = "Dashboard"
                                notice = ""
                            }
                        )


                        else -> DashboardScreen(
                            customerCount = customers.size,
                            applicationCount = applications.size,
                            documentCount = documents.size,
                            onNavigate = { page = it }
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    BottomNavigation(page = page, onNavigate = { page = it })
                }
            }
        }
    }
}


suspend fun fetchStaffCustomers(): List<Customer> = withContext(Dispatchers.IO) {
    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("Please sign in again.")

    val token = user.getIdToken(false).await().token
        ?: throw Exception("Could not obtain Firebase token.")

    val url = URL("${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffCustomers")
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        if (connection.responseCode != 200) {
            throw Exception("Customer API returned HTTP ${connection.responseCode}")
        }

        val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })

        (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)

            Customer(
                id = item.getInt("customerId"),
                name = "${item.optString("firstName")} ${item.optString("surname")}".trim(),
                email = item.optString("email"),
                phone = item.optString("phone"),
                status = item.optString("status", "Pending")
            )
        }
    } finally {
        connection.disconnect()
    }
}


suspend fun fetchStaffApplications(): List<TravelApplication> =
    withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Please sign in again.")

        val token = user.getIdToken(false).await().token
            ?: throw Exception("Could not obtain Firebase token.")

        val url = URL(
            "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffApplications"
        )
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            if (connection.responseCode != 200) {
                throw Exception(
                    "Applications API returned HTTP ${connection.responseCode}"
                )
            }

            val json = JSONArray(
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }
            )

            (0 until json.length()).map { index ->
                val item = json.getJSONObject(index)

                TravelApplication(
                    item.getInt("applicationId"),
                    item.optString("customerName"),
                    item.optString("packageName"),
                    item.optString("status")
                )
            }
        } finally {
            connection.disconnect()
        }
    }


suspend fun fetchStaffDocuments(): List<StaffDocument> =
    withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Please sign in again.")

        val token = user.getIdToken(false).await().token
            ?: throw Exception("Could not obtain Firebase token.")

        val url = URL(
            "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffDocuments"
        )

        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            if (connection.responseCode != 200) {
                throw Exception(
                    "Documents API returned HTTP ${connection.responseCode}"
                )
            }

            val json = JSONArray(
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }
            )

            (0 until json.length()).map { index ->
                val item = json.getJSONObject(index)

                StaffDocument(
                    id = item.getInt("documentId"),
                    applicationId = item.getInt("applicationId"),
                    customerName = item.optString("customerName"),
                    documentType = item.optString("documentType"),
                    fileName = item.optString("fileName"),
                    fileUrl = item.optString("fileUrl"),
                    status = item.optString("verificationStatus")
                )
            }
        } finally {
            connection.disconnect()
        }
    }


suspend fun updateStaffDocumentStatus(
    documentId: Int,
    status: String
) = withContext(Dispatchers.IO) {
    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("Please sign in again.")

    val token = user.getIdToken(false).await().token
        ?: throw Exception("Could not obtain Firebase token.")

    val url = URL(
        "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffDocuments/$documentId/status"
    )

    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true

        val body = JSONObject().apply {
            put("status", status)
        }

        connection.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        if (connection.responseCode != 200) {
            val error = connection.errorStream
                ?.bufferedReader()
                ?.use { it.readText() }

            throw Exception(
                "Document update failed (HTTP ${connection.responseCode}): ${error ?: "Unknown error"}"
            )
        }
    } finally {
        connection.disconnect()
    }
}


suspend fun downloadStaffDocument(
    context: android.content.Context,
    documentId: Int
): File = withContext(Dispatchers.IO) {

    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("Please sign in again.")

    val token = user.getIdToken(false).await().token
        ?: throw Exception("Could not obtain Firebase token.")

    val url = URL(
        "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffDocuments/$documentId/file"
    )

    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.setRequestProperty(
            "Authorization",
            "Bearer $token"
        )
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        if (connection.responseCode != 200) {
            val error = connection.errorStream
                ?.bufferedReader()
                ?.use { it.readText() }

            throw Exception(
                "Unable to open document (HTTP ${connection.responseCode}): " +
                        (error ?: "File unavailable")
            )
        }

        val contentType = connection.contentType
            ?.substringBefore(";")
            ?.trim()
            ?.lowercase()

        val extension = when (contentType) {
            "application/pdf" -> ".pdf"
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            else -> throw Exception("Unsupported document format.")
        }

        val file = File.createTempFile(
            "staff_document_${documentId}_",
            extension,
            context.cacheDir
        )

        try {
            connection.inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }

        file
    } finally {
        connection.disconnect()
    }
}

suspend fun updateStaffCustomer(customer: Customer): Customer =
    withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Please sign in again.")

        val token = user.getIdToken(false).await().token
            ?: throw Exception("Could not obtain Firebase token.")

        val nameParts = customer.name.trim().split(Regex("\\s+"), limit = 2)

        if (nameParts.size < 2) {
            throw Exception("Enter both a first name and surname.")
        }

        val body = org.json.JSONObject().apply {
            put("firstName", nameParts[0])
            put("surname", nameParts[1])
            put("email", customer.email.trim())
            put("phone", customer.phone.trim())
            put("status", customer.status)
        }

        val url = URL(
            "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffCustomers/${customer.id}"
        )

        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode != 200) {
                throw Exception(
                    "Unable to save customer. HTTP ${connection.responseCode}"
                )
            }

            customer
        } finally {
            connection.disconnect()
        }
    }

@Composable
fun TopBar(staffName: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Forest)
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.barakah_logo),
            contentDescription = "Barakah Travel and Tours logo",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.width(11.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "BARAKAH",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                "STAFF PORTAL",
                color = Color(0xFFC8DED1),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
        }

        Text(
            "Hi, $staffName",
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "Exit",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .clickable { onLogout() }
                .padding(4.dp)
        )
    }
}

private suspend fun verifyStaffWithApi(idToken: String): String =
    withContext(Dispatchers.IO) {
        val url = URL(BuildConfig.API_BASE_URL + "api/StaffAuth/login")
        val connection = (url.openConnection() as HttpURLConnection)

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            val requestBody = JSONObject()
                .put("idToken", idToken)
                .toString()

            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode

            val responseText = (
                    if (status in 200..299) connection.inputStream
                    else connection.errorStream
                    )?.bufferedReader()?.use { it.readText() }.orEmpty()

            val response = runCatching {
                JSONObject(responseText)
            }.getOrNull()

            when (status) {
                200 -> {
                    val firstName = response?.optString("firstName").orEmpty()
                    val surname = response?.optString("surname").orEmpty()
                    "$firstName $surname".trim().ifBlank { "Staff Member" }
                }

                403 -> throw Exception(
                    "This Google account is not authorized for staff access."
                )

                401 -> throw Exception(
                    "Your Firebase login was rejected. Please sign in again."
                )

                else -> throw Exception(
                    "Staff verification failed (HTTP $status). " +
                            (response?.optString("message").orEmpty())
                )
            }
        } finally {
            connection.disconnect()
        }
    }
@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    val context = LocalContext.current
    val googleClientId = context.getString(R.string.default_web_client_id)
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var googleError by remember { mutableStateOf("") }
    var signingIn by remember { mutableStateOf(false) }

    val googleSignInOptions = remember(googleClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember(context, googleSignInOptions) {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)

                val credential = GoogleAuthProvider.getCredential(
                    account.idToken,
                    null
                )

                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        authResult.user?.getIdToken(false)
                            ?.addOnSuccessListener { tokenResult ->
                                val idToken = tokenResult.token

                                if (idToken.isNullOrBlank()) {
                                    signingIn = false
                                    googleError = "Firebase did not provide an ID token."
                                } else {
                                    scope.launch {
                                        try {
                                            googleError = "Verifying staff account..."
                                            val authorizedStaffName = verifyStaffWithApi(idToken)

                                            signingIn = false
                                            googleError = ""
                                            onLogin(authorizedStaffName)
                                        } catch (exception: Exception) {
                                            signingIn = false
                                            googleError = exception.localizedMessage
                                                ?: "Could not verify staff account."
                                        }
                                    }
                                }
                            }
                            ?.addOnFailureListener { exception ->
                                signingIn = false
                                googleError = exception.localizedMessage
                                    ?: "Could not retrieve Firebase token."
                            }
                            ?: run {
                                signingIn = false
                                googleError = "Firebase user is unavailable."
                            }
                    }
                    .addOnFailureListener { exception ->
                        signingIn = false
                        googleError = exception.localizedMessage
                            ?: "Firebase sign-in failed."
                    }

            } catch (exception: Exception) {
                signingIn = false
                googleError = exception.localizedMessage
                    ?: "Google sign-in failed."
            }
        } else {
            signingIn = false
            googleError = "Google sign-in cancelled."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.barakah_logo),
            contentDescription = "Barakah Travel and Tours logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "BARAKAH TRAVEL",
            fontSize = 13.sp,
            color = Green,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Staff Admin Portal",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Manage customer journeys with care.",
            color = Muted
        )

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Staff sign in",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Sign in with your authorised Google account.",
                    color = Muted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        googleError = ""
                        signingIn = true
                        googleSignInLauncher.launch(
                            googleSignInClient.signInIntent
                        )
                    },
                    enabled = !signingIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (signingIn) {
                            "Signing in..."
                        } else {
                            "Sign in with Google"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (googleError.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = googleError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Staff access is granted only after verification by Barakah Travel.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Secure access for authorised Barakah Travel staff",
            color = Muted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DashboardScreen(
    customerCount: Int,
    applicationCount: Int,
    documentCount: Int,
    onNavigate: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text(
            "Assalamu Alaikum",
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Text(
            "Staff dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Text("Your operations at a glance.", color = Muted)

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                "Customers",
                customerCount.toString(),
                "Registered records",
                Modifier.weight(1f)
            )

            StatCard(
                "Applications",
                applicationCount.toString(),
                "Total applications",
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                "Documents",
                documentCount.toString(),
                "Total documents",
                Modifier.weight(1f)
            )

            StatCard(
                "Bookings",
                "—",
                "Not connected yet",
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "QUICK ACCESS",
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(10.dp))

        val items = listOf(
            Triple("Customers", "View, edit and generate registration codes", "♙"),
            Triple("Applications", "Review application statuses", "▤"),
            Triple("Documents", "Check document verification", "▣"),
            Triple("Packages", "View Umrah package catalogue", "✧"),
            Triple("Bookings", "Track booking status", "▦"),
            Triple("Payments", "Review payment overview", "R"),
            Triple("Travel Schedule", "See upcoming departures", "◷"),
            Triple("Settings", "App information and settings", "⚙")
        )

        items.forEach { (title, subtitle, symbol) ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onNavigate(title) },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .background(
                                Mint,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            symbol,
                            color = Forest,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )

                        Text(
                            subtitle,
                            fontSize = 12.sp,
                            color = Muted
                        )
                    }

                    Text(
                        "›",
                        color = Green,
                        fontSize = 26.sp
                    )
                }
            }
        }
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
        Text("Search and manage customer records stored in Azure SQL.", color = Muted, fontSize = 13.sp)
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
                            Text("Registration code: ${customer.code}", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(customer) }, modifier = Modifier.weight(1f)) {
                                Text("Edit details", fontSize = 12.sp)
                            }
                            Button(onClick = { onGenerate(customer) }, modifier = Modifier.weight(1f)) {
                                Text(if (customer.code.isBlank()) "Get code" else "Get code again", fontSize = 12.sp)
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
        Text("Customer #${customer.id}", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone number") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text(
            "Customer details and status are saved securely to Azure SQL.",
            color = Muted,
            fontSize = 12.sp
        )
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
        }, modifier = Modifier.fillMaxWidth()) { Text("Save changes") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        Spacer(Modifier.height(10.dp))
        Text(
            "Name, email and phone changes are saved to Azure SQL.",
            color = Muted,
            fontSize = 12.sp
        )
    }
}


suspend fun updateStaffApplicationStatus(
    applicationId: Int,
    status: String
) = withContext(Dispatchers.IO) {
    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("Please sign in again.")

    val token = user.getIdToken(false).await().token
        ?: throw Exception("Could not obtain Firebase token.")

    val url = URL(
        "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/StaffApplications/$applicationId/status"
    )

    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true

        val body = JSONObject().apply {
            put("status", status)
        }

        connection.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        if (connection.responseCode != 200) {
            val error = connection.errorStream
                ?.bufferedReader()
                ?.use { it.readText() }

            throw Exception(
                "Application update failed (HTTP ${connection.responseCode}): ${error ?: "Unknown error"}"
            )
        }
    } finally {
        connection.disconnect()
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
                        Text("Update application status", fontSize = 12.sp, color = Muted)
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

fun StaffDocumentsScreen(
    documents: List<StaffDocument>,
    onStatusChange: (Int, String) -> Unit,
    onViewFile: (Int) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "Document Management",
            fontSize = 27.sp,
            color = Forest,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Review uploaded documents and their verification status.",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(documents, key = { it.id }) { doc ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Text(
                            "Document #${doc.id} • Application #${doc.applicationId}",
                            color = Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            doc.customerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "${doc.documentType} • ${doc.fileName}",
                            color = Muted,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(8.dp))
                        StatusPill(doc.status)
                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Update verification status",
                            fontSize = 12.sp,
                            color = Muted
                        )

                        TextButton(
                            onClick = {
                                onViewFile(doc.id)
                            }
                        ) {
                            Text("View File", fontSize = 13.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Pending", "Accepted", "Rejected")
                                .forEach { status ->
                                    TextButton(
                                        onClick = {
                                            onStatusChange(doc.id, status)
                                        }
                                    ) {
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