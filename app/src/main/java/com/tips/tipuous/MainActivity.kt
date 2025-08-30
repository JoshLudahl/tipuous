package com.tips.tipuous

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.tips.tipuous.navigation.AppNavigation
import com.tips.tipuous.ui.theme.ThemeManager
import com.tips.tipuous.ui.theme.TipuousTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var aut: Task<AppUpdateInfo>
    private val updateType = AppUpdateType.FLEXIBLE
    private lateinit var themeManager: ThemeManager

    private val listener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.i("MainActivity", "Update has been downloaded.")
                Toast.makeText(
                    this,
                    "Update Completed. Restarting application.",
                    Toast.LENGTH_SHORT,
                ).show()
                lifecycleScope.launch {
                    appUpdateManager.completeUpdate()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        aut = appUpdateManager.appUpdateInfo
        checkIsUpdateAvailable()

        themeManager = ThemeManager.getInstance(this)
        enableEdgeToEdge()
        setContent {

            TipuousTheme(
                darkTheme = themeManager.isDarkTheme(),
                dynamicColor = themeManager.dynamicColor.collectAsState().value,
            ) { // Replace with your actual theme
                Surface(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                ) {
                    AppNavigation() // Changed to use AppNavigation
                }
            }
        }
    }

    private fun checkIsUpdateAvailable() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult(),
            ) { result ->
                if (result.resultCode != RESULT_OK) {
                    Log.i("MainActivity", "The Update has failed.")
                }
            }

        aut.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                Log.i("MainActivity", "Update is available.")

                appUpdateManager.registerListener(listener)
                Log.i("MainActivity", "Starting Update.")
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(updateType).build(),
                )
            } else {
                Log.i("MainActivity", "No Update Available.")
            }
        }
    }
}
