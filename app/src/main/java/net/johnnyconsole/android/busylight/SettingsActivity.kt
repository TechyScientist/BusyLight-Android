package net.johnnyconsole.android.busylight

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.johnnyconsole.android.busylight.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(intent.getIntExtra("color", 0)))
        supportActionBar!!.title = "BusyLight: Settings"
        binding.toolbar.setTitleTextColor(intent.getIntExtra("tint", 0))
    }
}