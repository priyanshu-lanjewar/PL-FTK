package com.example.pl_ftk

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import java.util.*
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
class HomeScreen : AppCompatActivity(), SensorEventListener {
    private lateinit var onButton : FloatingActionButton
    private lateinit var offButton : FloatingActionButton
    private lateinit var configure: Button
    private lateinit var status : TextView
    private lateinit var mode : Spinner
    private lateinit var sens : Slider
    private lateinit var hours : Slider
    private lateinit var mins : Slider
    private lateinit var secs : Slider
    private var hasCameraFlash = false
    private var sensorManager:SensorManager? =null
    private var sensor : Sensor? = null
    private var autoFlashMode = false
    private var isFlashOn = false
    private var blinkDelayTime : Double =0.5
    private var numberOfBlinks : Int =1
    private var sensi = 40
    private var hour = 0
    private var min = 0
    private var sec = 2
    private var time = 2
    private lateinit var handler : Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        initializeAll()



        mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                configure.isEnabled = position!=0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        onButton.setOnClickListener {

            runOnUiThread {
                onButton.visibility = View.GONE
                offButton.visibility = View.VISIBLE
                status.text = getString(R.string.status_on)
                status.setTextColor(Color.GREEN)
            }
            when(mode.selectedItemPosition){
                0-> flashLightOn()
                1-> {
                    val tr = Thread {
                        blinkLightMode()
                    }
                    tr.start()
                }
                2->timerFlashOn()
                3-> automaticFlashOn(true)
            }
        }

        offButton.setOnClickListener {
            onButton.visibility=View.VISIBLE
            offButton.visibility=View.GONE
            status.text = getString(R.string.status_off)
            status.setTextColor(Color.RED)
            when(mode.selectedItemPosition){
                0-> flashLightOff()
                2-> flashLightOff()
                3 -> automaticFlashOn(false)
                else -> {

                }
            }
        }

        configure.setOnClickListener {
            when(mode.selectedItemPosition){
                1 -> configureBlinkMode()
                2 -> configureTimerMode()
                3 -> configureAutomaticMode()
            }
        }

    }

    private fun configureTimerMode() {
        val inflate = layoutInflater
        val inflater = inflate.inflate(R.layout.timer_dialog,null)
        hours = inflater.findViewById(R.id.hour)
        mins = inflater.findViewById(R.id.min)
        secs = inflater.findViewById(R.id.sec)
        hours.value = hour.toFloat()
        mins.value = min.toFloat()
        secs.value = sec.toFloat()
        val  alert = AlertDialog.Builder(this)
        alert.setTitle("Timer Mode Configuration")
        alert.setView(inflater)
        alert.setCancelable(false)

        alert.setNegativeButton("set defaults"){dialog,_ ->
            hours.value = 0F
            mins.value = 0F
            secs.value = 2F
            hour = 0
            min = 0
            sec = 2
            time = hour*3600 + min*60 + sec
            dialog.dismiss()
        }
        alert.setPositiveButton("Update"){_,_ ->
            hour = hours.value.toInt()
            min = mins.value.toInt()
            sec = secs.value.toInt()
            time = hour*3600 + min*60 + sec
        }

        val  alertDialog = alert.create()
        alertDialog.show()
    }

    private fun configureAutomaticMode() {
        val inflate = layoutInflater
        val inflater = inflate.inflate(R.layout.automatic_dialog,null)
        sens = inflater.findViewById(R.id.sens)
        sens.value = sensi.toFloat()
        val  alert = AlertDialog.Builder(this)
        alert.setTitle("Automatic Mode Configuration")
        alert.setView(inflater)
        alert.setCancelable(false)

        alert.setNegativeButton("set defaults"){dialog,_ ->
            sensi=40
            sens.value=sensi.toFloat()
            dialog.dismiss()
        }
        alert.setPositiveButton("Update"){_,_ ->
           sensi = sens.value.toInt()
        }

        val  alertDialog = alert.create()
        alertDialog.show()
    }

    private fun configureBlinkMode() {
        val inflate = layoutInflater
        val inflater = inflate.inflate(R.layout.blink_dialog,null)
        val numberOfBlinksWidget = inflater.findViewById(R.id.blinks) as NumberPicker
        val delaySlider = inflater.findViewById(R.id.delay) as Slider
        numberOfBlinksWidget.maxValue=25
        numberOfBlinksWidget.minValue=1
        numberOfBlinksWidget.value = numberOfBlinks
        delaySlider.value = blinkDelayTime.toFloat()
        val  alert = AlertDialog.Builder(this)
        alert.setTitle("Blink Mode Configuration")
        alert.setView(inflater)
        alert.setCancelable(false)

        alert.setNegativeButton("set defaults"){dialog,_ ->
            blinkDelayTime =0.5
            numberOfBlinks  =1
            delaySlider.value = blinkDelayTime.toFloat()
            numberOfBlinksWidget.value = numberOfBlinks
            dialog.dismiss()
        }
        alert.setPositiveButton("Update"){_,_ ->
            numberOfBlinks = numberOfBlinksWidget.value
            blinkDelayTime = delaySlider.value.toDouble()
        }

        val  alertDialog = alert.create()
        alertDialog.show()
    }

    private fun timerFlashOn() {
        flashLightOn()
        Handler().postDelayed({
            flashLightOff()
            offButton.performClick()
        },(1000*time).toLong())
    }

    private fun blinkLightMode() {
        blink()
        runOnUiThread {
            offButton.performClick()
        }
        Thread.interrupted()


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun blink() {
        var str = "01"
        while ((str.length)/2!=numberOfBlinks)
            str+="01"

        for (c in str){
            if (c == '0'){
                flashLightOn()
            }
            else{
                flashLightOff()
            }
            try {
                Thread.sleep((1000*blinkDelayTime).toLong())
            }catch (x : Exception){
                x.printStackTrace()
            }
        }

    }

    // Initializing All the variable --------------------------------------------------------------------------
    private fun initializeAll() {
        offButton = findViewById(R.id.switchOff)
        onButton = findViewById(R.id.switchOn)
        status = findViewById(R.id.status)
        mode = findViewById(R.id.mode)
        configure = findViewById(R.id.configure)
        handler = Handler(Looper.getMainLooper())
        configure.isEnabled=false
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        hasCameraFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        configModeSpinner()
    }
// Initializing All the variable Completed------------------------------------------------------------------

    enum class MODE{
        DEFAULT,
        BLINK,
        TIMER,
        AUTOMATIC
    }
    private fun configModeSpinner() {
        val modes = mutableListOf(MODE.DEFAULT,MODE.BLINK,MODE.TIMER,MODE.AUTOMATIC)
        val adapter = ArrayAdapter(this@HomeScreen,android.R.layout.simple_spinner_dropdown_item,modes)
        mode.adapter=adapter
    }





    private fun flashLightOn() {
        if (isFlashOn) return
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as  CameraManager
        val cid = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cid,true)
        isFlashOn = true
    }

    private fun flashLightOff() {
        if(!isFlashOn) return
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as  CameraManager
        val cid = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cid,false)
        isFlashOn = false

    }


    override fun onBackPressed() {
        val dial = AlertDialog.Builder(this)
        dial.setCancelable(true)
        dial.setMessage("Are You Really want to Exit ?")
        dial.setTitle("PL - FTK")
        dial.setPositiveButton("YES") { _, _ -> exitProcess(0) }
        dial.setNegativeButton("NO"){dialogue , _ ->dialogue.cancel()}
        dial.show()
    }
// automatic Mode Start-----------------------------------------------------------------------------------

    private fun automaticFlashOn(b:Boolean) {
        autoFlashMode=b
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.values[0]>sensi){
            if (autoFlashMode)
            flashLightOff()
        }
        else{
            if (autoFlashMode)
            flashLightOn()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onResume() {
                sensorManager?.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)

        super.onResume()
    }

    override fun onPause() {
                sensorManager?.unregisterListener(this)
        super.onPause()
    }
}

// automatic Mode End------------------------------------------------------------------------------------
