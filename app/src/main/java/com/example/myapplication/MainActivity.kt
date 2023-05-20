package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.renderlib.SceneRenderer
import com.example.myapplication.renderlib.obj.example
import java.lang.Math.cos
import java.lang.Math.sin

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import java.io.*


class MainActivity : AppCompatActivity() {
    lateinit var editor:EditorApp
    lateinit var binding: ActivityMainBinding
    private lateinit var glSurfaceView: GLSurfaceView
    @RequiresApi(Build.VERSION_CODES.R)// TODO delet after android 10 and below iplementation
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.Wybierz.setOnClickListener{
            if(checkPermission()){
                val intent = Intent()
                    .setType("text/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 333)
            }else{
                requestPermision()
            }
        }
        binding.Zapisz.setOnClickListener{
            if(checkPermission()){
                saveFile()
            }else{
                requestPermision()
            }
        }

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
    //Read file
    private fun readFile(path: String)
    {
        val split: List<String> = path.split(":")
        val myExternalFile = File(Environment.getExternalStorageDirectory().toString()+"/"+split[1])
        try{
            var fileInputStream = FileInputStream(myExternalFile)

            var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }
            fileInputStream.close()
            binding.Wynik.text = stringBuilder
        } catch (e: IOException) {
            e.printStackTrace()
            binding.Wynik.text = "Nie dziala"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == RESULT_OK) {
            val selectedFile = data?.data
            readFile(selectedFile?.path.toString())
        }}

    //Save file
    private fun saveFile()
    {
        //Creating app folder if it doesnt exist
        val myExternalDirectory = File(Environment.getExternalStorageDirectory().toString()+"/VoxNet")
        if(!myExternalDirectory.exists())
        {
            myExternalDirectory.mkdir()
        }

        //If name is not null
        if(binding.nazwa.text.toString().trim() != null)
        {
            val myExternalFile = File(myExternalDirectory.path, binding.nazwa.text.toString().trim())
            try {
                val fileOutPutStream = FileOutputStream(myExternalFile)
                fileOutPutStream.write(binding.zawartosc.text.toString().toByteArray())
                fileOutPutStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //Ask for permition
    private fun requestPermision(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) //TODO add for version 10 and below
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package",this.packageName,null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        //11 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if(Environment.isExternalStorageManager())
            {
            }
        }
    }

    //Checking app permision to write and read files
    @RequiresApi(Build.VERSION_CODES.R) //TODO add permisions for android 10 and below
    private fun checkPermission(): Boolean{
        return Environment.isExternalStorageManager()
    }
}