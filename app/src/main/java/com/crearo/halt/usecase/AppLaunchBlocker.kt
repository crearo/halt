package com.crearo.halt.usecase

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
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
    private val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private val blockView = getBlockingView()

    override fun start() {
        compositeDisposable.add(appLaunchBus
            .getTopApp()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (focusModeManager.isFocusMode() && BLOCKED_APPS.contains(it)) {
                    drawBlockViewOnWindow()
                } else if (blockView.isShown) {
                    windowManager.removeView(blockView)
                }
            })
    }

    private fun drawBlockViewOnWindow() {
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

    private fun getBlockingView(): View {
        val blockView = ImageView(context)
        blockView.id = View.generateViewId()
        blockView.setBackgroundColor(Color.WHITE)
        return blockView
    }

}