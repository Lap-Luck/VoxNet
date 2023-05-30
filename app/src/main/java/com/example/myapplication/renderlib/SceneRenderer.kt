package com.example.myapplication.renderlib

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.example.myapplication.EditorApp
import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


fun toBuffer(d: Bitmap): ByteBuffer {
    var bb = ByteBuffer.allocateDirect(4*1024*1024)
    bb.order(ByteOrder.nativeOrder())
    bb.position(0)
    for (x in 0..1023){
        for (y in 0..1023) {
            var rgba:Int=d.getPixel(x,y)
            bb.putInt(rgba)
            //bb.putInt(200*256*256)
        }
    }
    bb.position(0)
    return bb
}

class SceneRenderer: GLSurfaceView.Renderer {
    companion object{
        fun GLProgramCompile(vertexShaderCode:String, fragmentShaderCode: String):Int{
            Log.d("SHADER", "hello")
            fun assert_ok(shader:Int){
                val compilationStatus = IntArray(1).also{
                    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, it, 0)
                }
                if ( compilationStatus[0] == GLES20.GL_FALSE ) {
                    Log.e("OpenGL", "shader failed to compile")
                }
                Log.i("SHADER", "hello")
            }
            val v_shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
            GLES20.glShaderSource(v_shader, vertexShaderCode)
            GLES20.glCompileShader(v_shader)
            assert_ok(v_shader)
            val f_shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
            GLES20.glShaderSource(f_shader, fragmentShaderCode)
            GLES20.glCompileShader(f_shader)
            assert_ok(f_shader)

            val Program = GLES20.glCreateProgram()
            GLES20.glAttachShader(Program, v_shader)
            GLES20.glAttachShader(Program, f_shader)
            GLES20.glLinkProgram(Program)
            return Program
        }
    }
    fun setup(glSurfaceView:GLSurfaceView){
        //Init data

        //glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(this)
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    lateinit var editor:EditorApp
    lateinit var bitmap:Bitmap
    var texture_id=-1
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        for (s in ShadersToComplie){
            var GLProgram= GLProgramCompile(s.vertexShaderCode, s.fragmentShaderCode)
            s.GL_PROGRAM= GLProgram
        }
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LESS)

        //new!!!
        fun genTex():Int{
            var texture_id=intArrayOf(-1)
            GLES20.glGenTextures(1,texture_id,0)
            return texture_id[0]
        }
        texture_id=genTex()
        if (texture_id==0){ assert(false)}
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture_id)

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_BO);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //val borderColor = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
        //GLES20.glTexParameterfv(GLES20.GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR,borderColor,0)
       // GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1024, 1024, 0, GLES20.GL_RGBA,  GLES20.GL_UNSIGNED_BYTE, bitmap)
        //GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
        bitmap.recycle()

    }
    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    override fun onDrawFrame(unused: GL10?) {
        full_draw()
    }
    fun redraw(){

    }
    fun full_draw(){
        //Log.d("Draw","frame")
        fun toBuffer(d:FloatArray): FloatBuffer {
            //complex function to change data from JVM-endian to GL-endian
            //sad
            var bb= ByteBuffer.allocateDirect(d.size * 4)
            bb.order(ByteOrder.nativeOrder())
            var res=bb.asFloatBuffer().apply {
                put(d)
                position(0)
            }
            return res
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        var editor_get_Camera_position=editor.get_Camera_position()
        var editor_get_Camera_look_at=editor.get_Camera_look_at()


        var to_draw=editor.n_render()
        for (e in to_draw) {
            var mat: Material =e.material
            var mesh=e.mesh


            //activeGeometry= Geometry(editor.render())

            GLES20.glUseProgram(mat.shader.GL_PROGRAM)

            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_id);
            assert(GLES20.glGetUniformLocation(mat.shader.GL_PROGRAM, "ourTexture")!=-1)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture_id)
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mat.shader.GL_PROGRAM, "ourTexture"), 0);


            var GlAttributeHandles=intArrayOf()
            for (u_name in mat.shader.GlAttributes){
                var DIM=3
                var SIZE=4
                val positionHandle = GLES20.glGetAttribLocation(mat.shader.GL_PROGRAM, u_name)
                if (positionHandle==-1){
                    Log.e("ATRIBUTE NO NEEDED",u_name)
                    continue
                }
                GlAttributeHandles+=intArrayOf(positionHandle)

                //vertex data

                var Attribute=floatArrayOf()
                if (u_name=="vPosition"){
                    Attribute=mesh.vertex_array
                }
                if (u_name=="vNormal"){
                    Attribute=mesh.normal_array
                }
                if (u_name=="vUV"){
                    Attribute=mesh.uv_array
                }

                GLES20.glEnableVertexAttribArray(positionHandle)
                GLES20.glVertexAttribPointer(
                    positionHandle,
                    DIM,
                    GLES20.GL_FLOAT,
                    false,
                    DIM*SIZE,
                    toBuffer(Attribute)
                )
            }

            fun camera(): Mat4 {
                var ratio=1.0f
                val projection = GLM.perspective(GLM.PIf * 0.25f, ratio, 0.1f, 100.0f)
                var view = GLM.lookAt(
                    editor_get_Camera_position,
                    editor_get_Camera_look_at,
                    Vec3(0f,1f,0f),
                )
                return projection * view
            }
            var vPMatrix2=camera()*Mat4.identity.translate(e.pos)



            var vPMatrixHandle = GLES20.glGetUniformLocation(mat.shader.GL_PROGRAM, "uMVPMatrix")
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix2.toFloatArray(), 0)


            for (u_name in mat.shader.GlUniforms){
                val UHandle = GLES20.glGetUniformLocation(mat.shader.GL_PROGRAM, u_name)
                var value=mat.GlUniforms.getOrDefault(u_name, Vec4())
                GLES20.glUniform4fv(UHandle, 1,value.toFloatArray() , 0)
            }

            var vertexCount=mesh.vertex_array.size/3
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
            for (Handle in GlAttributeHandles)
            {
                GLES20.glDisableVertexAttribArray(Handle)
            }
        }
    }
}