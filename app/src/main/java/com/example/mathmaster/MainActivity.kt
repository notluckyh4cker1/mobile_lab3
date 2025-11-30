package com.example.mathmaster

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mathmaster.databinding.ActivityMainBinding
import android.view.View
import androidx.navigation.Navigation
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadLanguageSettings()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtonListeners()
        setupNavigation()
        setupNavControllerListener()
    }

    private fun loadLanguageSettings() {
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("language", "system") ?: "system"

        if (language != "system") {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val resources = resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavControllerListener() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.historyFragment -> {
                    binding.topButtonsLayout.visibility = View.GONE
                }
                R.id.graphsFragment -> {
                    binding.topButtonsLayout.visibility = View.VISIBLE
                    binding.buttonHistory.visibility = View.VISIBLE
                    binding.buttonEngineering.visibility = View.GONE
                    binding.buttonEngineering.text = getString(R.string.engineering_button)
                }
                R.id.calculatorFragment, R.id.programmerFragment -> {
                    binding.topButtonsLayout.visibility = View.VISIBLE
                    binding.buttonHistory.visibility = View.VISIBLE
                    binding.buttonEngineering.visibility = View.VISIBLE

                    val isProgrammerMode = navController.currentDestination?.id == R.id.programmerFragment
                    binding.buttonEngineering.text = if (isProgrammerMode) {
                        getString(R.string.programmer_button)
                    } else {
                        getString(R.string.engineering_button)
                    }
                }
                else -> {
                    binding.topButtonsLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupButtonListeners() {
        binding.buttonHistory.setOnClickListener {
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            navController.navigate(R.id.historyFragment)
        }

        binding.buttonEngineering.setOnClickListener {
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            val isProgrammerMode = navController.currentDestination?.id == R.id.programmerFragment

            if (isProgrammerMode) {
                binding.buttonEngineering.text = getString(R.string.engineering_button)
                navController.navigate(R.id.calculatorFragment)
            } else {
                binding.buttonEngineering.text = getString(R.string.programmer_button)
                navController.navigate(R.id.programmerFragment)
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }
}