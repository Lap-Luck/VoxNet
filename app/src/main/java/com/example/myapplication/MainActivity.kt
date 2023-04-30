package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.opengl.GLES20
import android.util.Log
import android.view.*
import android.widget.Button
import com.example.myapplication.renderlib.SceneRenderer
import com.example.myapplication.renderlib.obj.example
import java.lang.Math.cos
import java.lang.Math.sin

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3


class MainActivity : AppCompatActivity() {
    lateinit var editor:EditorApp
    private lateinit var glSurfaceView: GLSurfaceView
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.glSurfaceView)
        editor=EditorApp()
        editor.conte=this

        for (id in intArrayOf(
            R.id.button_zoom_plus,
            R.id.button_zoom_minus,
            R.id.button_move_x_m,R.id.button_move_y_m,R.id.button_move_z_m,
            R.id.button_move_x_p,R.id.button_move_y_p,R.id.button_move_z_p,
            R.id.button_color_r,R.id.button_color_g,R.id.button_color_b,
            R.id.button_del,
            R.id.button_add,
        )){
            findViewById<Button>(id).setOnClickListener(editor)
        }
        glSurfaceView.setOnTouchListener(editor)

        //Loading texture
        var andr_img:Bitmap=BitmapFactory.decodeResource(this.resources,R.drawable.white_rhombus_pattern);
        //assert(andr_img.getWidth()==1024)//TODO turn off autoscaling
                                        // TODO just because it is stupid to upscale texture from 512 to 1024
                                        // TODO due to screen scaling
        //assert(andr_img.getHeight()==1024)
        //assert(get_color(andr_img.getPixel(0,0))[2]>100)
        var renderer= SceneRenderer().also { it.editor=editor;it.bitmap=andr_img }
        renderer.setup(glSurfaceView)
        editor.begin()
        editor.requestRender={->
            Log.d("Render","request")
            glSurfaceView.requestRender()
        }

    }
}