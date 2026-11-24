package com.callshield

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var switchFilter: SwitchMaterial
    private lateinit var txtStatus: TextView
    private lateinit var txtBlocked: TextView
    private lateinit var txtSetup: TextView
    private lateinit var btnSetup: Button
    private lateinit var pauseButtons: View

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 30_000) // refresca solo cuando termina la pausa
        }
    }

    private val contactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }

    private val roleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchFilter = findViewById(R.id.switchFilter)
        txtStatus = findViewById(R.id.txtStatus)
        txtBlocked = findViewById(R.id.txtBlocked)
        txtSetup = findViewById(R.id.txtSetup)
        btnSetup = findViewById(R.id.btnSetup)
        pauseButtons = findViewById(R.id.pauseButtons)

        switchFilter.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                Prefs.setEnabled(this, checked)
                refresh()
            }
        }

        findViewById<Button>(R.id.btnPause2h).setOnClickListener { pause(2) }
        findViewById<Button>(R.id.btnPause6h).setOnClickListener { pause(6) }
        findViewById<Button>(R.id.btnPause24h).setOnClickListener { pause(24) }
        findViewById<Button>(R.id.btnResume).setOnClickListener {
            Prefs.clearPause(this)
            refresh()
        }

        btnSetup.setOnClickListener { requestMissingSetup() }
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun pause(hours: Int) {
        Prefs.pauseFor(this, hours * 60L * 60L * 1000L)
        refresh()
    }

    private fun hasContactsPermission() =
        checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun hasScreeningRole(): Boolean {
        val rm = getSystemService(RoleManager::class.java)
        return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestMissingSetup() {
        when {
            !hasContactsPermission() -> contactsPermission.launch(Manifest.permission.READ_CONTACTS)
            !hasScreeningRole() -> {
                val rm = getSystemService(RoleManager::class.java)
                roleRequest.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
            }
        }
    }

    private fun refresh() {
        val ready = hasContactsPermission() && hasScreeningRole()

        // Paso de configuración inicial
        txtSetup.visibility = if (ready) View.GONE else View.VISIBLE
        btnSetup.visibility = if (ready) View.GONE else View.VISIBLE
        txtSetup.text = when {
            !hasContactsPermission() -> "1/2 · Necesito acceso a tus contactos para saber a quién dejar pasar."
            else -> "2/2 · Elige CallShield como app de identificación de llamadas y spam."
        }

        // Interruptor principal
        switchFilter.isEnabled = ready
        val enabled = Prefs.isEnabled(this)
        if (switchFilter.isChecked != enabled) switchFilter.isChecked = enabled
        pauseButtons.visibility = if (ready && enabled) View.VISIBLE else View.GONE

        // Texto de estado
        txtStatus.text = when {
            !ready -> "Falta terminar la configuración."
            !enabled -> "Filtro apagado: todas las llamadas suenan."
            Prefs.isPaused(this) -> {
                val hora = DateFormat.getTimeFormat(this).format(Date(Prefs.pausedUntil(this)))
                "Pausado hasta las $hora. Después se reactiva solo."
            }
            else -> "Filtro activo: solo suenan tus contactos."
        }
        txtBlocked.text = "Llamadas bloqueadas: ${Prefs.blockedCount(this)}"
    }
}
