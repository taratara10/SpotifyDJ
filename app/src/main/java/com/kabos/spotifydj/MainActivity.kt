package com.kabos.spotifydj

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kabos.spotifydj.databinding.ActivityMainBinding
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.repository.UserService
import com.kabos.spotifydj.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

    }

    override fun onSupportNavigateUp(): Boolean {
        //Fragmentのコールバックがあればそれを実行する
        if (onBackPressedDispatcher.hasEnabledCallbacks()) onBackPressedDispatcher.onBackPressed()
        return true
    }
}
