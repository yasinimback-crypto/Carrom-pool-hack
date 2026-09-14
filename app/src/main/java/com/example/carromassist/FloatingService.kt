package com.example.carromassist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var menuView: View? = null
    private var isMenuOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val iconParams = WindowManager.LayoutParams(
            150, 150,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.master_logo)
        }
        floatingView = logoImage

        logoImage.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = iconParams.x
                        initialY = iconParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        iconParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        iconParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, iconParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = Math.abs(event.rawX - initialTouchX)
                        val diffY = Math.abs(event.rawY - initialTouchY)
                        if (diffX < 10 && diffY < 10) {
                            toggleMenu(layoutType)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, iconParams)
    }

    private fun toggleMenu(layoutType: Int) {
        if (isMenuOpen) {
            menuView?.let { windowManager.removeView(it) }
            isMenuOpen = false
        } else {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            menuView = inflater.inflate(R.layout.floating_menu, null)

            val menuParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            menuView?.findViewById<View>(R.id.btnClose)?.setOnClickListener {
                toggleMenu(layoutType)
            }

            windowManager.addView(menuView, menuParams)
            isMenuOpen = true
        }
    }

    private fun startForegroundService() {
        val channelId = "floating_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Floating Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Carrom Assist Running")
            .setContentText("Floating Engine Active")
            .setSmallIcon(R.drawable.master_logo)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
        menuView?.let { windowManager.removeView(it) }
    }
}
