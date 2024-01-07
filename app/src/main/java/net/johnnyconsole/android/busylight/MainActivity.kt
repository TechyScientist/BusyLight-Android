package net.johnnyconsole.android.busylight

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.material.slider.Slider
import net.johnnyconsole.android.busylight.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: MenuItem
    private var color: Int = 0
    private var tint: Int = 0

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

        if(view.id == R.id.btCustom) {
            val builder = AlertDialog.Builder(this)
            val dialog = builder.setView(R.layout.dialog_custom_colour)
                .setTitle("Select Custom Color")
                .setPositiveButton("Confirm") { d, _ ->
                    val dialog = d as AlertDialog
                    val red = dialog.findViewById<Slider>(R.id.red)!!.value.toInt()
                    val green = dialog.findViewById<Slider>(R.id.green)!!.value.toInt()
                    val blue = dialog.findViewById<Slider>(R.id.blue)!!.value.toInt()
                    color = Color.rgb(red, green, blue)
                    tint = getColor(if(dialog.findViewById<RadioButton>(R.id.rbBlack)!!.isChecked) R.color.black else R.color.white)
                    supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
                    binding.toolbar.setTitleTextColor(tint)
                    settings.icon!!.setTint(tint)
                    supportActionBar!!.title = "BusyLight: Custom"
                    val button = view as Button
                    button.setBackgroundColor(color)
                    button.setTextColor(tint)
                }
                .setNegativeButton("Cancel", null).create()
            dialog.show()

            dialog.findViewById<Slider>(R.id.red)!!.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

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

            dialog.findViewById<Slider>(R.id.green)!!.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

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

            dialog.findViewById<Slider>(R.id.blue)!!.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

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

            dialog.findViewById<RadioButton>(R.id.rbBlack)!!.setOnCheckedChangeListener { _, isChecked ->
                val color = dialog.findViewById<TextView>(R.id.colour)!!
                if(isChecked) color.setTextColor(getColor(R.color.black))
            }

            dialog.findViewById<RadioButton>(R.id.rbWhite)!!.setOnCheckedChangeListener { _, isChecked ->
                val color = dialog.findViewById<TextView>(R.id.colour)!!
                if(isChecked) color.setTextColor(getColor(R.color.white))
            }

        }
        else {
            color = getColor(
                when (view.id) {
                    R.id.btOff -> R.color.grey
                    R.id.btRed -> R.color.red
                    R.id.btGreen -> R.color.green
                    R.id.btBlue -> R.color.blue
                    R.id.btYellow -> R.color.yellow
                    else -> R.color.pink
                }
            )
            tint =
                getColor(if (view.id == R.id.btYellow || view.id == R.id.btOff) R.color.black else R.color.white)
            val text = (view as Button).text
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
            binding.toolbar.setTitleTextColor(tint)
            settings.icon!!.setTint(tint)
            supportActionBar!!.title = "BusyLight: $text"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        settings = menu!![0]
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("color", color)
        intent.putExtra("tint", tint)
        startActivity(intent)
        return true
    }
}