package com.barakahtravel.staff

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import retrofit2.HttpException

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
        tokenStore.save(res.token)
        session = res
        AuthResult.Success(res)
    } catch (e: HttpException) {
        val msg = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?: "HTTP ${e.code()}"
        AuthResult.Failure(msg)
    } catch (e: Exception) {
        AuthResult.Failure(e.message ?: "Network error")
    }
}
