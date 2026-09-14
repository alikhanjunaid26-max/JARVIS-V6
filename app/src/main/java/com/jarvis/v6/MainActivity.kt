package com.jarvis.v6

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var output: TextView
    private lateinit var status: TextView
    private val voiceRequest = 1001
    private val audioRequest = 1002
    private var torchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                audioRequest
            )
        }

        buildInterface()
    }

    private fun buildInterface() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 35, 30, 30)
        root.setBackgroundColor(0xFF05070A.toInt())

        val title = TextView(this)
        title.text = "J A R V I S   V6"
        title.textSize = 28f
        title.gravity = Gravity.CENTER
        title.setTextColor(0xFF00E5FF.toInt())

        status = TextView(this)
        status.text = "SYSTEM ONLINE"
        status.textSize = 14f
        status.gravity = Gravity.CENTER
        status.setTextColor(0xFF80FFFF.toInt())

        output = TextView(this)
        output.text = "Hello. I am JARVIS.\n\nTap MIC and speak a command."
        output.textSize = 17f
        output.setTextColor(0xFFE8FFFF.toInt())
        output.setPadding(20, 30, 20, 30)

        val scroll = ScrollView(this)
        scroll.addView(output)

        val mic = Button(this)
        mic.text = "MIC - SPEAK"
        mic.textSize = 18f
        mic.setOnClickListener {
            startVoice()
        }

        root.addView(title)
        root.addView(status)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        root.addView(
            mic,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        addButton(root, "FLASHLIGHT") {
            toggleFlashlight()
        }

        addButton(root, "VOLUME +") {
            changeVolume(true)
        }

        addButton(root, "VOLUME -") {
            changeVolume(false)
        }

        addButton(root, "BRIGHTNESS") {
            openBrightnessSettings()
        }

        addButton(root, "WIFI SETTINGS") {
            openSettings(Settings.ACTION_WIFI_SETTINGS)
        }

        addButton(root, "BLUETOOTH SETTINGS") {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
        }

        addButton(root, "BATTERY INFO") {
            batteryInfo()
        }

        addButton(root, "PHONE SETTINGS") {
            openSettings(Settings.ACTION_SETTINGS)
        }

        setContentView(root)
    }

    private fun addButton(
        root: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = text
        button.setOnClickListener { action() }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun startVoice() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak to JARVIS"
        )

        try {
            status.text = "LISTENING..."
            startActivityForResult(intent, voiceRequest)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Voice recognition is not available.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest &&
            resultCode == Activity.RESULT_OK
        ) {

            val results =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val command =
                results?.firstOrNull()?.lowercase(Locale.getDefault())
                    ?: return

            status.text = "COMMAND RECEIVED"

            output.text =
                "YOU:\n$command\n\nJARVIS:\n${processCommand(command)}"
        }
    }

    private fun processCommand(command: String): String {

        return when {

            command.contains("flashlight") ||
            command.contains("torch") -> {
                toggleFlashlight()
                if (torchOn) {
                    "Flashlight activated."
                } else {
                    "Flashlight deactivated."
                }
            }

            command.contains("volume up") ||
            command.contains("increase volume") ||
            command.contains("louder") -> {
                changeVolume(true)
                "Volume increased."
            }

            command.contains("volume down") ||
            command.contains("decrease volume") ||
            command.contains("quieter") -> {
                changeVolume(false)
                "Volume decreased."
            }

            command.contains("wifi") -> {
                openSettings(Settings.ACTION_WIFI_SETTINGS)
                "Opening WiFi settings."
            }

            command.contains("bluetooth") -> {
                openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
                "Opening Bluetooth settings."
            }

            command.contains("brightness") -> {
                openBrightnessSettings()
                "Opening brightness controls."
            }

            command.contains("battery") -> {
                batteryInfo()
                "Reading battery information."
            }

            command.contains("settings") -> {
                openSettings(Settings.ACTION_SETTINGS)
                "Opening phone settings."
            }

            command.contains("youtube") -> {
                openApp("com.google.android.youtube")
                "Opening YouTube."
            }

            command.contains("chrome") -> {
                openApp("com.android.chrome")
                "Opening Chrome."
            }

            command.contains("whatsapp") -> {
                openApp("com.whatsapp")
                "Opening WhatsApp."
            }

            else -> {
                "I understood your command as:\n\"$command\"\n\nI do not have an action for that command yet."
            }
        }
    }

    private fun toggleFlashlight() {

        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {

            val cameraId = manager.cameraIdList.firstOrNull { id ->
                val characteristics =
                    manager.getCameraCharacteristics(id)

                characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            }

            if (cameraId != null) {

                torchOn = !torchOn

                manager.setTorchMode(
                    cameraId,
                    torchOn
                )

                status.text =
                    if (torchOn) "FLASHLIGHT ON"
                    else "FLASHLIGHT OFF"
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Cannot control flashlight.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun changeVolume(increase: Boolean) {

        val audio =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val direction =
            if (increase)
                AudioManager.ADJUST_RAISE
            else
                AudioManager.ADJUST_LOWER

        audio.adjustVolume(
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun batteryInfo() {

        val battery =
            getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val level =
            battery.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        output.text =
            "JARVIS:\n\nBattery level: $level%"
    }

    private fun openBrightnessSettings() {
        openSettings(Settings.ACTION_DISPLAY_SETTINGS)
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }
    }

    private fun openApp(packageName: String) {

        val intent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "App not installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
