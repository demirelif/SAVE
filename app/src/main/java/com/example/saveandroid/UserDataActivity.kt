package com.example.saveandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class UserDataActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)
        if (savedInstanceState == null) {
        supportFragmentManager
        .beginTransaction()
        .replace(R.id.userdata, SettingsFragment())
        .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_data_user, rootKey)
        }
        }
}
