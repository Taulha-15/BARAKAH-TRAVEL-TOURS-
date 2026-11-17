// google-signin.js – "Continue with Google" on the customer login page (single sign-on).
//
// Flow: Google popup (Firebase Authentication) -> Firebase ID token -> POST to /Login?handler=Google
//       -> the website asks the API to verify the token -> customer cookie session is created.
// Reference: Firebase Authentication documentation – "Authenticate Using Google with JavaScript".

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
    getAuth,
    GoogleAuthProvider,
    signInWithPopup,
    signOut
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { firebaseConfig } from "./firebase-config.js";

const button = document.getElementById("googleSignIn");
const errorBox = document.getElementById("googleError");

function showError(message) {
    errorBox.textContent = message;
    console.error("[BarakahWeb] " + message);
}

if (button) {
    if (firebaseConfig.apiKey.startsWith("PASTE_")) {
        // Helpful message while the Firebase web config has not been filled in yet
        button.addEventListener("click", () =>
            showError("Google sign-in is not configured yet (see wwwroot/js/firebase-config.js)."));
    } else {
        const auth = getAuth(initializeApp(firebaseConfig));

        button.addEventListener("click", async () => {
            errorBox.textContent = "";
            button.disabled = true;

            try {
                // 1. Ask Google who the user is (popup window)
                const result = await signInWithPopup(auth, new GoogleAuthProvider());
                const idToken = await result.user.getIdToken();
                console.log("[BarakahWeb] Google sign-in succeeded, sending token to the server");

                // The browser's Firebase session is not needed after this; the website uses its own cookie.
                await signOut(auth);

                // 2. Send the token to the Razor Pages handler (antiforgery token from the login form)
                const antiForgery = document.querySelector('input[name="__RequestVerificationToken"]').value;
                const response = await fetch("/Login?handler=Google", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "RequestVerificationToken": antiForgery
                    },
                    body: JSON.stringify({ idToken })
                });

                const data = await response.json().catch(() => ({}));

                // 3. Success: go to the dashboard. Failure: show the server's message.
                if (response.ok && data.success) {
                    window.location.href = data.redirectUrl || "/CustomerDashboard";
                } else {
                    showError(data.message || "Google sign-in failed. Please try again.");
                }
            } catch (err) {
                if (err.code === "auth/popup-closed-by-user" || err.code === "auth/cancelled-popup-request") {
                    showError("Google sign-in was cancelled.");
                } else if (err.code === "auth/popup-blocked") {
                    showError("Your browser blocked the Google pop-up. Allow pop-ups for this site and try again.");
                } else {
                    showError("Google sign-in failed: " + (err.message || err));
                }
            } finally {
                button.disabled = false;
            }
        });
    }
}
