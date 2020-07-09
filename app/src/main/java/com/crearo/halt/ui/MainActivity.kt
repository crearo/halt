package com.crearo.halt.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.AppForegroundService
import com.crearo.halt.data.UnlockStatRepository
import com.crearo.halt.databinding.ActivityMainBinding
import com.crearo.halt.manager.AppTasksManager
import com.crearo.halt.manager.FocusModeManager
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var notificationManager: NotificationManager
    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository

    @Inject
    lateinit var appTasksManager: AppTasksManager

    @Inject
    lateinit var focusModeManager: FocusModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppForegroundService.startService(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        binding.btnDnd.setOnClickListener {
            focusModeManager.setFocusMode(!focusModeManager.isFocusMode())
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(
                intent,
                ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
            )
        }

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }

        if (!appTasksManager.hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(unlockStatRepository.getUnlockStats()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                binding.tvDebug.text = "total ${list.size}"
            })
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            Timber.d("Can draw on screen? ${Settings.canDrawOverlays(this)}")
        }
    }

}
