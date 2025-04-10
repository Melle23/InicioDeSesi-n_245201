package molina.esmeralda.practica10_245201

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "LoginPrefs"
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        verificarSesion()

        oneTapClient = Identity.getSignInClient(this)

        val webClientId = "862859373666-unhqda42he9g4brl6d5fuuteo38tkpn4.apps.googleusercontent.com"

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        findViewById<Button>(R.id.btnLoginGoogle).setOnClickListener {
            signInWithGoogle()
        }

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            Toast.makeText(this, "Función de login estándar no implementada", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_crearcuenta).setOnClickListener {
            Toast.makeText(this, "Función de crear cuenta no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        Log.d("GoogleSignIn", "Iniciando Google Sign-In")
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQUEST_CODE_GOOGLE_SIGN_IN,
                        null, 0, 0, 0, null
                    )
                    Log.d("GoogleSignIn", "Intent iniciado correctamente")
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Error al iniciar intent: ${e.message}", e)
                    Toast.makeText(this, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                if (e is ApiException) {
                    Log.e("GoogleSignIn", "ApiException: ${e.statusCode} - ${e.message}", e)
                    if (e.statusCode == 16) {
                        Toast.makeText(this, "No hay cuentas válidas disponibles. Agrega una cuenta Google al dispositivo.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error de Sign-In: código ${e.statusCode}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("GoogleSignIn", "Error inicio sesión: ${e.message}", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Log.d("GoogleSignIn", "onActivityResult: resultCode=$resultCode")
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                val email = credential.id

                if (idToken != null) {
                    Log.d("GoogleSignIn", "Autenticando con Firebase. Token obtenido.")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("GoogleSignIn", "Autenticación Firebase exitosa")
                                guardarDatosSesion(email, "Google")
                                val intent = Intent(this, Bienvenida::class.java)
                                intent.putExtra("correo", email)
                                intent.putExtra("proveedor", "Google")
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("GoogleSignIn", "Error Firebase: ${task.exception?.message}", task.exception)
                                Toast.makeText(this, "Error autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("GoogleSignIn", "No se obtuvo ID token")
                    Toast.makeText(this, "No se obtuvo ID token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "ApiException: ${e.statusCode} - ${e.message}", e)
                Toast.makeText(this, "Error de autenticación: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Excepción general: ${e.message}", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarSesion() {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val correo = prefs.getString("correo", null)
        val proveedor = prefs.getString("proveedor", null)

        if (correo != null && proveedor != null) {
            val intent = Intent(this, Bienvenida::class.java)
            intent.putExtra("correo", correo)
            intent.putExtra("proveedor", proveedor)
            startActivity(intent)
            finish()
        }
    }

    private fun guardarDatosSesion(correo: String, proveedor: String) {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("correo", correo)
        editor.putString("proveedor", proveedor)
        editor.apply()
    }

    companion object {
        private const val REQUEST_CODE_GOOGLE_SIGN_IN = 100
    }
}

