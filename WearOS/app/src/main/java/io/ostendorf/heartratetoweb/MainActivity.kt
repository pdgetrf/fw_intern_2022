package io.ostendorf.heartratetoweb

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.ostendorf.heartratetoweb.databinding.ActivityMainBinding

class MainActivity : Activity() {

    object Config {
        const val CONF_HTTP_HOSTNAME = "HTTP_HOSTNAME"
        const val CONF_HTTP_PORT = "HTTP_PORT"

        const val CONF_HTTP_HOSTNAME_DEFAULT = ""
        const val CONF_HTTP_PORT_DEFAULT = 6547

        const val CONF_BROADCAST_HEARTRATE_UPDATE = "heartratetoweb.updateHeartRate"
        const val CONF_BROADCAST_STATUS = "heartratetoweb.updateStatus"

        const val CONF_SENDING_STATUS_OK = "ok"
        const val CONF_SENDING_STATUS_ERROR = "error"
        const val CONF_SENDING_STATUS_NOT_RUNNING = "not_running"
        const val CONF_SENDING_STATUS_STARTING = "starting"
    }

    private lateinit var textStatus: TextView
    private lateinit var mStatusUpdateReceiver: BroadcastReceiver
    private lateinit var textCurrentHr: TextView
    private lateinit var binding: ActivityMainBinding
    private lateinit var startButton: Button

    private lateinit var preferences: SharedPreferences

    private var runningState: RunningState = RunningState.STOPPED

    private lateinit var mHeartRateUpdateReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initConfig()
        bindConfigToInputs()

        textCurrentHr = findViewById(R.id.textViewHeartRate)
        updateHeartRate(0)

        textStatus = findViewById(R.id.textViewStatus)
        updateStatus(Config.CONF_SENDING_STATUS_NOT_RUNNING)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 100)
        }

        startButton = findViewById(R.id.buttonStartStop)

        wireButton()

        mHeartRateUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.hasExtra("heartrate") == true) {
                    updateHeartRate(intent.getIntExtra("heartrate", 0))
                }
            }
        }

        mStatusUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.hasExtra("status") == true) {
                    updateStatus(intent.getStringExtra("status"))
                }
            }
        }

        registerBroadcastReceivers()

    }


    private fun wireButton() {

        startButton.setOnClickListener {
            if (runningState == RunningState.STOPPED) {
                runningState = RunningState.RUNNING
                startMeasure()
            } else if (runningState == RunningState.RUNNING) {
                runningState = RunningState.STOPPED
                stopMeasure()
            }

            updateButtonText()
        }
    }

    private fun updateButtonText() {

        if (runningState == RunningState.STOPPED) {
            startButton.setText(R.string.button_start)
        } else if (runningState == RunningState.RUNNING) {
            startButton.setText(R.string.button_stop)
        }

        startButton.invalidate()
    }

    private fun bindConfigToInputs() {
        val hostnameInput = findViewById<EditText>(R.id.editTextHostname)
        val portInput = findViewById<EditText>(R.id.editTextPort)

        hostnameInput.setText(
            preferences.getString(
                Config.CONF_HTTP_HOSTNAME,
                Config.CONF_HTTP_HOSTNAME_DEFAULT
            )
        )
        portInput.setText(
            preferences.getInt(Config.CONF_HTTP_PORT, Config.CONF_HTTP_PORT_DEFAULT).toString()
        )

        hostnameInput.doAfterTextChanged {
            with(preferences.edit()) {
                putString(Config.CONF_HTTP_HOSTNAME, it.toString())
                apply()
                Log.d("config", "Saved new Hostname: " + it.toString())
            }
        }

        portInput.doAfterTextChanged {
            with(preferences.edit()) {
                putInt(Config.CONF_HTTP_PORT, it.toString().toInt())
                apply()
                Log.d("config", "Saved new Port: " + it.toString())
            }
        }
    }

    private fun initConfig() {
        preferences = this.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)

        with(preferences.edit()) {
            if (!preferences.contains(Config.CONF_HTTP_HOSTNAME)) {
                putString(Config.CONF_HTTP_HOSTNAME, Config.CONF_HTTP_HOSTNAME_DEFAULT)
            }

            if (!preferences.contains(Config.CONF_HTTP_PORT)) {
                putInt(Config.CONF_HTTP_PORT, Config.CONF_HTTP_PORT_DEFAULT)
            }
            apply()
        }
    }

    fun updateHeartRate(heartrate: Int) {


        textCurrentHr.text = resources.getString(R.string.text_current_hr, heartrate)
        textCurrentHr.invalidate()
    }

    private fun updateStatus(status: String?) {

        val stringResource = when (status) {
            Config.CONF_SENDING_STATUS_OK -> R.string.text_status_ok
            Config.CONF_SENDING_STATUS_ERROR -> R.string.text_status_error
            Config.CONF_SENDING_STATUS_NOT_RUNNING -> R.string.text_status_not_running
            Config.CONF_SENDING_STATUS_STARTING -> R.string.text_status_starting
            else -> R.string.text_status_unknown
        }

        textStatus.text =
            resources.getString(R.string.text_status, resources.getString(stringResource))
        textStatus.invalidate()
    }

    private fun startMeasure() {
        Log.d("service", "Starting foreground heart rate service ...")
        HeartRateService.startService(this)
    }

    private fun stopMeasure() {
        Log.d("service", "Stopping foreground heart rate service ...")
        HeartRateService.stopService(this)
    }

    private fun registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mHeartRateUpdateReceiver,
            IntentFilter(Config.CONF_BROADCAST_HEARTRATE_UPDATE)
        )
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mStatusUpdateReceiver, IntentFilter(Config.CONF_BROADCAST_STATUS))
    }

    private fun unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHeartRateUpdateReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusUpdateReceiver)
    }

    override fun onPause() {
        unregisterBroadcastReceivers()
        super.onPause()
    }

    override fun onResume() {
        registerBroadcastReceivers()
        updateButtonText()
        super.onResume()
    }
}
