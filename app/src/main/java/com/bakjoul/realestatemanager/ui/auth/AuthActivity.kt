package com.bakjoul.realestatemanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.databinding.ActivityAuthBinding
import com.bakjoul.realestatemanager.ui.main.MainActivity
import com.bakjoul.realestatemanager.ui.utils.viewBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "AuthActivity"
    }

    private val binding by viewBinding { ActivityAuthBinding.inflate(it) }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setGoogleSignInButton()
    }

    private fun setGoogleSignInButton() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { handleGoogleSignInResult(it) }
        binding.authGoogleSignInButton.setOnClickListener {
/*            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                Log.d(TAG, "setGoogleSignInButton: too fast")
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()*/
            googleSignIn()
        }
    }

    private fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        activityResultLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")

                val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
    }
}
