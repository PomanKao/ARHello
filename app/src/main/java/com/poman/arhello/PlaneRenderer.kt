package com.poman.arhello

import android.opengl.GLES20
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class PlaneRenderer {
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var planeProgram = 0
    private var planeXZPositionAlpha = 0

    companion object {
        private const val VERTICES_PER_FOOT = 2
        private const val BYTES_PER_FLOAT = 4
        private const val BYTES_PER_SHORT = 2
        private const val COORDS_PER_VERTEX = 3

        private const val VERTEX_SHADER = """
            uniform mat4 u_ModelViewProjection;
            uniform vec4 u_Color;
            attribute vec4 a_Position;
            varying vec4 v_Color;
            
            void main() {
                v_Color = u_Color;
                gl_Position = u_ModelViewProjection * vec4(a_Position.xyz, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec4 v_Color;
            
            void main() {
                gl_FragColor = v_Color;
            }
        """
    }

    fun createOnGlThread() {
        planeProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        planeXZPositionAlpha = GLES20.glGetAttribLocation(planeProgram, "a_Position")

        // 初始化緩衝區
        vertexBuffer = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * 4 * VERTICES_PER_FOOT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        indexBuffer = ByteBuffer.allocateDirect(BYTES_PER_SHORT * 6)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
    }

    fun draw(plane: Plane, cameraPose: Pose, projectionMatrix: FloatArray) {
        val planeMatrix = FloatArray(16)
        plane.centerPose.toMatrix(planeMatrix, 0)

        // 更新頂點數據
        updatePlaneVertices(plane)

        // 使用著色器程序
        GLES20.glUseProgram(planeProgram)

        // 設置頂點數據
        GLES20.glVertexAttribPointer(
            planeXZPositionAlpha, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, vertexBuffer)

        // 繪製平面
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexBuffer!!.capacity(),
            GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }

    private fun updatePlaneVertices(plane: Plane) {
        // 獲取平面邊界點並更新頂點緩衝區
        val polygon = plane.polygon ?: return
        vertexBuffer?.clear()

        // 遍歷平面的頂點
        for (i in 0 until polygon.size) {
            val point = polygon[i]
            vertexBuffer?.put(point[0])
            vertexBuffer?.put(point[1])
            vertexBuffer?.put(point[2])
        }
        vertexBuffer?.position(0)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }
}