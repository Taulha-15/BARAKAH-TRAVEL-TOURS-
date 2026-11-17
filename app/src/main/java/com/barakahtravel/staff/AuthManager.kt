package com.barakahtravel.staff
/*
 * AuthManager.kt – email/password session handling from the first prototype's REST login.
 * Note: the current screens sign staff in with Google and Firebase inside MainActivity.kt,
 * so this class is not used by the running UI. It is kept as a documented example of
 * handling API success, HTTP errors and network errors with a sealed result type.
 */

import android.util.Log
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import retrofit2.HttpException

/** Outcome of a sign-in attempt: Success carries the session, Failure carries a message. */
sealed class AuthResult {
    data class Success(val session: AuthResponse) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

class AuthManager(
    private val context: Context,
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    var session: AuthResponse? = null
        private set

    val isSignedIn: Boolean get() = session != null && tokenStore.token != null

    fun googleClient(): GoogleSignInClient {
        val opts = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, opts)
    }

    suspend fun signIn(email: String, password: String): AuthResult =
        call { api.login(LoginRequest(email, password)) }

    suspend fun register(email: String, password: String, displayName: String): AuthResult =
        call { api.register(RegisterRequest(email, password, displayName)) }

    suspend fun signInWithGoogle(idToken: String): AuthResult =
        call { api.googleLogin(GoogleLoginRequest(idToken)) }

    suspend fun changePassword(current: String, new: String): Result<Unit> = runCatching {
        api.changePassword(UpdatePasswordRequest(current, new))
    }

    suspend fun updateProfile(displayName: String): AuthResult =
        call { api.updateProfile(UpdateProfileRequest(displayName)) }

    suspend fun signOut() {
        tokenStore.clear()
        session = null
    }

    private suspend fun call(block: suspend () -> AuthResponse): AuthResult = try {
        val res = block()
        Log.d("BarakahStaff", "AuthManager: sign-in call succeeded, saving token")
        tokenStore.save(res.token)
        session = res
        AuthResult.Success(res)
    } catch (e: HttpException) {
        val msg = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?: "HTTP ${e.code()}"
        Log.w("BarakahStaff", "AuthManager: HTTP error ${e.code()}")
        AuthResult.Failure(msg)
    } catch (e: Exception) {
        Log.e("BarakahStaff", "AuthManager: network error ${e.message}", e)
        AuthResult.Failure(e.message ?: "Network error")
    }
}
