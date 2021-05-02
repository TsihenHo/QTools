package me.tsihen.qtools.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.tsihen.qtools.R
import me.tsihen.util.log

abstract class AbsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_QTools)
        super.onCreate(savedInstanceState)
    }
}