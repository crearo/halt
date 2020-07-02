package com.crearo.halt

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.crearo.halt.data.DndRepository
import com.crearo.halt.data.UnlockStatRepository
import com.crearo.halt.rx.DndStateBus
import com.crearo.halt.rx.DndStateEnum
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var unlockStatRepository: UnlockStatRepository
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var dndRepository: DndRepository

    @Inject
    lateinit var dndStateBus: DndStateBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppForegroundService.startService(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        findViewById<Button>(R.id.set_dnd).setOnClickListener {
            if (dndRepository.isDndEnabled() == DndStateEnum.ENABLED) {
                dndRepository.setNoDnd()
            } else {
                dndRepository.setDnd()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        }

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(unlockStatRepository.getUnlockStats()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                findViewById<TextView>(R.id.tv_debug).text = "total ${list.size}"
            })
        compositeDisposable.add(
            dndStateBus.getState().observeOn(AndroidSchedulers.mainThread()).subscribe {
                findViewById<TextView>(R.id.tv_debug).text = "total $it"
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
