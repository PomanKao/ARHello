package com.poman.arhello

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import javax.microedition.khronos.opengles.GL10

class HelloArRenderer(val activity: MainActivity, val session: Session) : GLSurfaceView.Renderer {
    private val backgroundRenderer = BackgroundRenderer()

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        // 設置清除色為黑色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        backgroundRenderer.createOnGlThread()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 設置視口尺寸
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 清除背景
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // 更新 session 並獲取當前幀
        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId())
            val frame = session.update()
            backgroundRenderer.draw(frame)

            // 獲取相機位姿和投影矩陣
            val camera = frame.camera
            val projectionMatrix = FloatArray(16)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}