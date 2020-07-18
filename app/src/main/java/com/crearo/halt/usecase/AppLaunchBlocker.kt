package com.crearo.halt.usecase

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.crearo.halt.R
import com.crearo.halt.manager.FocusModeManager
import com.crearo.halt.pollers.Poller
import com.crearo.halt.rx.AppLaunchBus
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppLaunchBlocker @Inject constructor(@ApplicationContext context: Context) : Poller(context) {

    @Inject
    lateinit var appLaunchBus: AppLaunchBus

    @Inject
    lateinit var focusModeManager: FocusModeManager

    private val BLOCKED_APPS = listOf("com.instagram.android")
    private val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private val blockView = getBlockingView()

    override fun start() {
        compositeDisposable.add(appLaunchBus
            .getTopApp()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (focusModeManager.isFocusMode() && BLOCKED_APPS.contains(it)) {
                    goToHomeActivity()
                    addBlockViewToWindow()
                }
            })
    }

    private fun goToHomeActivity() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(homeIntent)
    }

    private fun addBlockViewToWindow() {
        Timber.d("Drawing blocking view")

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0

        windowManager.addView(blockView, params)
    }

    private fun removeBlockViewFromWindow() {
        windowManager.removeView(blockView)
    }

    private fun getBlockingView(): View {
        val blockView = inflater.inflate(R.layout.fullscreen_block_view, null)
        blockView.id = View.generateViewId()
        blockView.setOnClickListener { removeBlockViewFromWindow() }
        return blockView
    }

}