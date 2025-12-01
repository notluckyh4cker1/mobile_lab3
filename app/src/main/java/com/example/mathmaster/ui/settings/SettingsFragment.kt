package com.example.mathmaster.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.mathmaster.MainActivity
import com.example.mathmaster.R
import com.example.mathmaster.databinding.FragmentSettingsBinding
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentSettings()
        setupThemeSelection()
        setupLanguageSelection()
        setupSaveButton()
        setupBackButton()
    }

    private fun loadCurrentSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val currentTheme = sharedPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioDark.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }

        val currentLanguage = sharedPrefs.getString("language", "system") ?: "system"
        when (currentLanguage) {
            "ru" -> binding.radioRussian.isChecked = true
            "en" -> binding.radioEnglish.isChecked = true
            else -> binding.radioSystemLang.isChecked = true
        }
    }

    private fun setupThemeSelection() {
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // processing upon saving
        }
    }

    private fun setupLanguageSelection() {
        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // processing upon saving
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun saveSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val themeMode = when (binding.themeRadioGroup.checkedRadioButtonId) {
            binding.radioLight.id -> AppCompatDelegate.MODE_NIGHT_NO
            binding.radioDark.id -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        editor.putInt("theme_mode", themeMode)

        val oldLanguage = sharedPrefs.getString("language", "system") ?: "system"
        val newLanguage = when (binding.languageRadioGroup.checkedRadioButtonId) {
            binding.radioRussian.id -> "ru"
            binding.radioEnglish.id -> "en"
            else -> "system"
        }
        editor.putString("language", newLanguage)

        editor.apply()

        applyTheme(themeMode)

        if (oldLanguage != newLanguage) {
            applyLanguage(newLanguage)
            restartApp()
        } else {
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.settings_saved),
                android.widget.Toast.LENGTH_SHORT
            ).show()

            requireActivity().onBackPressed()
        }
    }

    private fun applyTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun applyLanguage(languageCode: String) {
        if (languageCode != "system") {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = requireContext().resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    private fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}