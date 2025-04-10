package molina.esmeralda.practica10_245201

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import molina.esmeralda.practica10_245201.R

class Bienvenida : AppCompatActivity() {

    private val PREFS_NAME = "LoginPrefs"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        auth = FirebaseAuth.getInstance()

        val correo = intent.getStringExtra("correo")
        val proveedor = intent.getStringExtra("proveedor")

        // Mostrar información en la interfaz
        val evCorreo = findViewById<TextView>(R.id.evCorreo)
        val evProveedor = findViewById<TextView>(R.id.evProveedor)

        evCorreo.text = "Correo electrónico: $correo"
        evProveedor.text = "Proveedor: $proveedor"

        // Configurar botón de salir
        val btnSalir = findViewById<Button>(R.id.btnSalir)
        btnSalir.setOnClickListener {
            // Cerrar sesión en Firebase
            auth.signOut()

            // Borrar datos
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()

            // Regresar a la pantalla de inicio
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}