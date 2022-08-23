package io.ostendorf.heartratetoweb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.RequestQueue
import com.android.volley.request.StringRequest
import com.android.volley.toolbox.Volley
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class HeartRateService : Service(), SensorEventListener {

    private lateinit var mHeartRateSensor: Sensor
    private lateinit var mSensorManager: SensorManager
    private lateinit var httpQueue: RequestQueue
    private lateinit var preferences: SharedPreferences

    private val CHANNEL_ID = "HeartRateService"

        companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, HeartRateService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, HeartRateService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        doSomething()

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(R.string.notification_title.toString())
            .setContentText(R.string.notification_text.toString())
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            R.string.notification_channel_title.toString(),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager!!.createNotificationChannel(serviceChannel)
    }

    private fun doSomething() {

        preferences = this.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        httpQueue = Volley.newRequestQueue(this)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        startMeasure()
    }

    private fun startMeasure() {
        val sensorRegistered: Boolean = mSensorManager.registerListener(
            this,
            mHeartRateSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        Log.d("Sensor Status:", " Sensor registered: " + (if (sensorRegistered) "yes" else "no"))
        sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_STARTING)
    }

    private fun stopMeasure() {
        mSensorManager.unregisterListener(this)
        sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_NOT_RUNNING)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val mHeartRateFloat: Float = event!!.values[0]

        val mHeartRate: Int = mHeartRateFloat.roundToInt()
        Log.d("HR: ", mHeartRate.toString())

        sendHeartRate(mHeartRate)
        sendHeartRateToActivity(mHeartRate)

//        for (i in 1..75) {
//            val mDemoHeartRate: Int = (40..150).random()
//            Log.d("My demo heart rate is: ", mDemoHeartRate.toString())
//
//            sendHeartRate(mDemoHeartRate)
//            sendHeartRateToActivity(mDemoHeartRate)
//            TimeUnit.SECONDS.sleep(1)
//        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // ignored
    }

    private fun sendHeartRate(heartrate: Int) {

        val patient_id = "100"
        val httpUrl = "http://" +
                preferences.getString(
                    MainActivity.Config.CONF_HTTP_HOSTNAME,
                    MainActivity.Config.CONF_HTTP_HOSTNAME_DEFAULT
                ) +
                ":" + preferences.getInt(
            MainActivity.Config.CONF_HTTP_PORT,
            MainActivity.Config.CONF_HTTP_PORT_DEFAULT
        ).toString()

        val httpRequest = object : StringRequest(
            Method.POST,
            httpUrl,
            { response ->
                Log.d("HTTP Reponse: ", response)
                sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_OK)
            },
            {
                Log.e("HTTP Error", it.message.toString())
                sendStatusToActivity(MainActivity.Config.CONF_SENDING_STATUS_ERROR)
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getBody(): ByteArray {
                return ("rate=$heartrate"+"&patient=$patient_id").toByteArray(Charsets.UTF_8)
            }
        }

        httpQueue.add(httpRequest)
    }

    override fun onDestroy() {
        stopMeasure()
        super.onDestroy()
    }

    private fun sendHeartRateToActivity(heartrate: Int) {
        val intent = Intent(MainActivity.Config.CONF_BROADCAST_HEARTRATE_UPDATE)
        intent.putExtra("heartrate", heartrate)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendStatusToActivity(status: String) {
        val intent = Intent(MainActivity.Config.CONF_BROADCAST_STATUS)
        intent.putExtra("status", status)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}