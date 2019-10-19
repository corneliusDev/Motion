package com.justin.motion.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.net.Uri
import android.media.AudioManager
import android.media.MediaPlayer


class SensorBackgroundService : Service(), SensorEventListener {

    /**
     * again we need the sensor manager and sensor reference
     */
    private var mSensorManager: SensorManager? = null

    /**
     * an optional flag for logging
     */
    private var mLogging = true

    private var isTriggered = false

    private var isRegistered = false

    private var time_tile_trigger = 3000L

    private var CHANNEL_ID = "alarm_sensor"

    private var notificationId = 1001

    /**
     * treshold values
     */
    private var mThresholdX: Float = 0.toFloat()
    private var mThresholdY: Float = 0.toFloat()
    private var mThresholdZ: Float = 0.toFloat()

    private var deltaX: Float = 0.toFloat()
    private var deltaY: Float = 0.toFloat()
    private var deltaZ: Float = 0.toFloat()

    private var mGravity: FloatArray? = null
    private var mMediaPlayer:MediaPlayer? = null


    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.stop()
        mSensorManager!!.unregisterListener(this)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // get sensor manager on starting the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // have a default sensor configured
        var sensorType = Sensor.TYPE_ACCELEROMETER

        val args = intent.extras

        if (args != null) {

            // set sensortype from bundle
            if (args.containsKey(KEY_SENSOR_TYPE))
                sensorType = args.getInt(KEY_SENSOR_TYPE)

            // optional logging
            mLogging = args.getBoolean(KEY_LOGGING)

            mThresholdX =
                if (args.containsKey(KEY_THRESHOLD_X_VALUE)) args.getFloat(KEY_THRESHOLD_X_VALUE) else java.lang.Float.MIN_VALUE
            mThresholdY =
                if (args.containsKey(KEY_THRESHOLD_Y_VALUE)) args.getFloat(KEY_THRESHOLD_Y_VALUE) else java.lang.Float.MAX_VALUE
            mThresholdZ =
                if (args.containsKey(KEY_THRESHOLD_Z_VALUE)) args.getFloat(KEY_THRESHOLD_Z_VALUE) else java.lang.Float.MAX_VALUE

        }



        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // ignore this since not linked to an activity
        return null
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {

        mGravity = event.values.clone()
        // Shake detection
        val x = mGravity!![0]
        val y = mGravity!![1]
        val z = mGravity!![2]

        deltaX = x - previousValueX
        deltaY = y - previousValueY
        deltaZ = z - previousValueZ

//


        if (deltaX > acelDelta || deltaY > acelDelta || deltaZ > acelDelta && !isTriggered) {
            isTriggered = true
            pendingAlarm(this, this.applicationContext)

        }

        previousValueX = x
        previousValueY = y
        previousValueZ = z

    }


    fun pendingAlarm(sensor: SensorEventListener, context: Context) {
        // launch a new coroutine and keep a reference to its Job. This makes sure the phone is moving for more that three seconds before triggering alarm.
        GlobalScope.launch {
            delay(time_tile_trigger)
            if ((deltaX > acelDelta || deltaY > acelDelta || deltaZ > acelDelta) && !isRegistered) {
                mSensorManager!!.unregisterListener(sensor)
                isRegistered = true
                setRingtone(context)

            }else{
                isTriggered = false
            }
        }

    }

    private fun setRingtone(context: Context){
        try {
            val alert = Uri.parse("android.resource://" + context.getPackageName() + "/raw/default_alarm")
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(this, alert)
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
            if (mMediaPlayer != null) mMediaPlayer!!.run {
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                prepare()
                start()

            }
            }
        } catch (e: Exception) {
        }

    }






    companion object {

        /**
         * a tag for logging
         */
        private val TAG = SensorBackgroundService::class.java.simpleName

        /**
         * also keep track of the previous value
         */
        private var previousValueX: Float = 0.toFloat()
        private var previousValueY: Float = 0.toFloat()
        private var previousValueZ: Float = 0.toFloat()

        private val acelDelta = 0.3

        val KEY_SENSOR_TYPE = "sensor_type"

        val KEY_THRESHOLD_X_VALUE = "threshold_x_value"

        val KEY_THRESHOLD_Y_VALUE = "threshold_y_value"

        val KEY_THRESHOLD_Z_VALUE = "threshold_z_value"

        val KEY_LOGGING = "logging"
    }




}
