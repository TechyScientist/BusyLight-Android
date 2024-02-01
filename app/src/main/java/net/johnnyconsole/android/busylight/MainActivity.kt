package net.johnnyconsole.android.busylight

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.material.slider.Slider
import net.johnnyconsole.android.busylight.databinding.ActivityMainBinding
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var color: Int = 0
    private var tint: Int = 0
    private var connected: Boolean = false

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var btDevice: BluetoothDevice
    private lateinit var btConnection: ConnectThread
    private var btSocket: BluetoothSocket? = null
    private var btOutput: OutputStream? = null

    private val BT_SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    private val BT_REQUEST_CONNECT = 100
    private val BT_REQUEST_ENABLE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        color = getColor(R.color.grey)
        tint = getColor(R.color.black)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
        supportActionBar!!.title = "BusyLight: Off"
        binding.toolbar.setTitleTextColor(tint)
    }

    fun onButtonClick(view: View) {
        if(!connected) {
            AlertDialog.Builder(this).setTitle(R.string.connectionError)
                .setMessage(R.string.connectionErrorMessage)
                .setNeutralButton(R.string.dismiss, null)
                .create().show()
            return
        }
        if (view.id == R.id.btCustom) {
            val builder = AlertDialog.Builder(this)
            val dialog = builder.setView(R.layout.dialog_custom_colour)
                .setTitle("Select Custom Color")
                .setPositiveButton("Confirm") { d, _ ->
                    val dialog = d as AlertDialog
                    val red = dialog.findViewById<Slider>(R.id.red)!!.value.toInt()
                    val green = dialog.findViewById<Slider>(R.id.green)!!.value.toInt()
                    val blue = dialog.findViewById<Slider>(R.id.blue)!!.value.toInt()
                    color = Color.rgb(red, green, blue)
                    tint =
                        getColor(if (dialog.findViewById<RadioButton>(R.id.rbBlack)!!.isChecked) R.color.black else R.color.white)
                    supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
                    binding.toolbar.setTitleTextColor(tint)
                    supportActionBar!!.title = "BusyLight: Custom"
                    val btRequest = "R${color.red} G${color.green} B${color.blue} X\r\n"
                    Log.d("BusyLight", btRequest)
                    btConnection.dispatch(btRequest)
                    val button = view as Button
                    button.setBackgroundColor(color)
                    button.setTextColor(tint)
                }
                .setNegativeButton("Cancel", null).create()
            dialog.show()

            dialog.findViewById<Slider>(R.id.red)!!
                .addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {

                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val color = dialog.findViewById<TextView>(R.id.colour)!!
                        val red = slider.value.toInt()
                        val green = dialog.findViewById<Slider>(R.id.green)!!.value.toInt()
                        val blue = dialog.findViewById<Slider>(R.id.blue)!!.value.toInt()
                        color.background = ColorDrawable(Color.rgb(red, green, blue))
                    }
                })

            dialog.findViewById<Slider>(R.id.green)!!
                .addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {

                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val color = dialog.findViewById<TextView>(R.id.colour)!!
                        val red = dialog.findViewById<Slider>(R.id.red)!!.value.toInt()
                        val green = slider.value.toInt()
                        val blue = dialog.findViewById<Slider>(R.id.blue)!!.value.toInt()
                        color.background = ColorDrawable(Color.rgb(red, green, blue))
                    }
                })

            dialog.findViewById<Slider>(R.id.blue)!!
                .addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {

                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val color = dialog.findViewById<TextView>(R.id.colour)!!
                        val red = dialog.findViewById<Slider>(R.id.red)!!.value.toInt()
                        val green = dialog.findViewById<Slider>(R.id.green)!!.value.toInt()
                        val blue = slider.value.toInt()
                        color.background = ColorDrawable(Color.rgb(red, green, blue))
                    }
                })

            dialog.findViewById<RadioButton>(R.id.rbBlack)!!
                .setOnCheckedChangeListener { _, isChecked ->
                    val color = dialog.findViewById<TextView>(R.id.colour)!!
                    if (isChecked) color.setTextColor(getColor(R.color.black))
                }

            dialog.findViewById<RadioButton>(R.id.rbWhite)!!
                .setOnCheckedChangeListener { _, isChecked ->
                    val color = dialog.findViewById<TextView>(R.id.colour)!!
                    if (isChecked) color.setTextColor(getColor(R.color.white))
                }

        } else {
            color = getColor(
                when (view.id) {
                    R.id.btOff -> R.color.black
                    R.id.btRed -> R.color.red
                    R.id.btGreen -> R.color.green
                    R.id.btBlue -> R.color.blue
                    R.id.btYellow -> R.color.yellow
                    R.id.btPink -> R.color.pink
                    else -> R.color.white
                }
            )
            tint =
                getColor(if (view.id == R.id.btYellow || view.id == R.id.btWhite) R.color.black else R.color.white)
            val text = (view as Button).text
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
            binding.toolbar.setTitleTextColor(tint)
            supportActionBar!!.title = "BusyLight: $text"
            val btRequest = "R${color.red} G${color.green} B${color.blue} X\r\n"
            Log.d("BusyLight", btRequest)
            btConnection.dispatch(btRequest)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            btConnection.cancel()
            btSocket = null
            btOutput = null
            connected = false
        } catch(e: Exception) {
            connected = false
        }
    }

    override fun onResume() {
        super.onResume()
        btManager = getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter

        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), BT_REQUEST_CONNECT)
        }

        while(!btAdapter.isEnabled) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_ENABLE)
        }

        val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            if(device.name == "BusyLight") btDevice = device
        }

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_SERIAL_UUID))
            btOutput = btSocket?.outputStream
            btConnection = ConnectThread()
            btConnection.start()
            connected = true
        } catch (e: Exception) {
            connected = false
            AlertDialog.Builder(this).setTitle(R.string.connectionError)
                .setMessage(R.string.connectionErrorMessage)
                .setNeutralButton(R.string.dismiss, null)
                .create().show()

        }
    }

    private inner class ConnectThread() : Thread() {
        override fun run() {
            btSocket?.let { socket ->
                while(ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), BT_REQUEST_CONNECT)
                }
                socket.connect()
            }
        }

        fun cancel() {
            try {
                btSocket?.close()
            } catch (e: IOException) {
                Log.e("BusyLight:ConnectThread", "Could not close the client socket", e)
            }
        }

        fun dispatch(message: String) {
            btOutput!!.write(message.toByteArray())
        }
    }

}
