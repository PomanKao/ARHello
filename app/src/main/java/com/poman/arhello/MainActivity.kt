package com.poman.arhello

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException


class MainActivity : AppCompatActivity() {
    private var surfaceView: GLSurfaceView? = null
    private var session: Session? = null
    private var installRequested = false

    private var renderer: HelloArRenderer? = null  // 添加渲染器變數

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        surfaceView = findViewById(R.id.surfaceview)
        surfaceView?.setEGLContextClientVersion(2)
        surfaceView?.preserveEGLContextOnPause = true
    }

    private fun configureSession() {
        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.focusMode = Config.FocusMode.AUTO
        // 啟用平面檢測
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        session?.configure(config)
    }

    override fun onResume() {
        super.onResume()

        // 檢查相機權限
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        // 確保AR環境可用並創建Session
        try {
            if (session == null) {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // 成功，可以創建Session
                    }
                }

                // 創建AR Session
                session = Session(this)
                configureSession()

                // 創建渲染器並設置
                renderer = session?.let { HelloArRenderer(this, it) }
                surfaceView?.setRenderer(renderer)
            }
        } catch (e: Exception) {
            handleSessionException(e)
            return
        }

        try {
            session?.resume()
            surfaceView?.onResume()
        } catch (e: Exception) {
            handleSessionException(e)
            return
        }
    }

    override fun onPause() {
        super.onPause()
        session?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.close()
        session = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            ).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // 引導用戶前往設定頁面開啟權限
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    private fun handleSessionException(e: Exception) {
        val message = when (e) {
            is UnavailableArcoreNotInstalledException,
            is UnavailableUserDeclinedInstallationException -> "Please install ARCore"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            else -> "Failed to create AR session: $e"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}