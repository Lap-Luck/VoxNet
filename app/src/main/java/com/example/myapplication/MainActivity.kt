package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.renderlib.SceneRenderer
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketListener
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection


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


       // if(findViewById<View>(R.id.navigation).visibility == View.VISIBLE)
          //  outState.putBoolean("Navigation",true)
       // if(findViewById<View>(R.id.zoom).visibility == View.VISIBLE)
         //   outState.putBoolean("Zoom",true)
       // if(findViewById<View>(R.id.add_del).visibility == View.VISIBLE)
         //   outState.putBoolean("Add",true)
       // if(findViewById<View>(R.id.color_pallet).visibility == View.VISIBLE)
           // outState.putBoolean("Color",true)
       // if(findViewById<View>(R.id.saving).visibility == View.VISIBLE)
            //outState.putBoolean("Save",true)

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean("Navigation"))
                findViewById<View>(R.id.navigation).visibility = View.VISIBLE
            if(savedInstanceState.getBoolean("Zoom"))
                findViewById<View>(R.id.zoom).visibility = View.VISIBLE
            if(savedInstanceState.getBoolean("Add"))
                findViewById<View>(R.id.add_del).visibility = View.VISIBLE
            if(savedInstanceState.getBoolean("Color"))
                findViewById<View>(R.id.color_pallet).visibility = View.VISIBLE
            if(savedInstanceState.getBoolean("Save"))
                findViewById<View>(R.id.saving).visibility = View.VISIBLE
            if(savedInstanceState.getString("File_name") != null)
                binding.saving.FileName.setText(savedInstanceState.getString("File_name"))
            if(savedInstanceState.getBoolean("Login"))
                findViewById<View>(R.id.loginscreen).visibility = View.VISIBLE
            else
                findViewById<View>(R.id.loginscreen).visibility = View.GONE
            if(savedInstanceState.getBoolean("Register"))
                findViewById<View>(R.id.registerscreen).visibility = View.VISIBLE
            else
                findViewById<View>(R.id.registerscreen).visibility = View.GONE
            if(savedInstanceState.getBoolean("Server"))
                findViewById<View>(R.id.server).visibility = View.VISIBLE
            else
                findViewById<View>(R.id.server).visibility = View.GONE
            if(savedInstanceState.getBoolean("List"))
                findViewById<View>(R.id.fileList).visibility = View.VISIBLE
            else
                findViewById<View>(R.id.fileList).visibility = View.GONE
        }


        //var ws: WebSocket = WebSocketFactory().createSocket("ws://192.168.0.220:8001/endpoint")
        var ws: WebSocket = WebSocketFactory().createSocket("ws://127.0.0.1:8001/endpoint")
        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(websocket:WebSocket, message:String) {
                Log.e("WEB RECIVED", "something")
                Log.e("WEB RECIVED", message)
            }
        })




        binding.saving.ChooseFile.setOnClickListener{
            if(checkPermission()){
                val intent = Intent()
                    .setType("text/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 333)
            }else{
                requestPermision()
            }
        }
        binding.saving.SaveFile.setOnClickListener{
            if(checkPermission()){
                saveFile("res.txt",editor.save_to_string())
            }else{
                requestPermision()
            }
        }

        glSurfaceView = findViewById<GLSurfaceView>(R.id.glSurfaceView)
        editor=EditorApp()
        editor.conte=this

        for (id in intArrayOf(
            R.id.Zoom_Add,
            R.id.Zoom_Minus,
            R.id.X_Minus,R.id.Y_Minus,R.id.Z_Minus,
            R.id.X_Add,R.id.Y_Add,R.id.Z_Add,
            R.id.Red_color,R.id.Green_color,R.id.Blue_color,
            R.id.DEL_elem,
            R.id.ADD_elem,
            //R.id.server_connect,
            R.id.server_disconnect,
            R.id.button_login,
            R.id.add_file,
            R.id.make_public,
            R.id.register,
            R.id.guest,
            R.id.register_account,
            R.id.exit_list,
        )){
            findViewById<Button>(id).setOnClickListener(editor)
        }
        //findViewById<AppCompatImageButton>(R.id.server).setOnClickListener(editor)//WHY TODO
        fun clear_ui(){
            for (id in intArrayOf(
                R.id.navigation,
                R.id.saving,
                R.id.zoom,
                R.id.add_del,
                R.id.color_pallet,
                R.id.server_connect_to
            )){
                findViewById<View>(id).visibility=View.GONE
            }
        }

        var shortcut2view= mapOf(
            R.id.options to R.id.tableLayout,
            R.id.show_nav to R.id.navigation,
            R.id.show_save to R.id.saving,
            R.id.show_add to R.id.add_del,
            R.id.show_color to R.id.color_pallet,
            R.id.show_zoom to R.id.zoom,
            R.id.server to R.id.server_connect_to,
        )
        for ((shortcut,view) in shortcut2view){
            val change=mapOf(
                View.VISIBLE to View.GONE,
                View.INVISIBLE to View.VISIBLE,//to be complete
                View.GONE to View.VISIBLE,
            )
            findViewById<ImageButton>(shortcut).setOnClickListener({

                var window:View=findViewById<View>(view)
                window.visibility=change[window.visibility]!!
            })
        }

        var shortcut2viewB= mapOf(
            R.id.show_files to R.id.fileList,
            R.id.show_login to R.id.loginscreen,
            R.id.show_register to R.id.registerscreen
        )
        for ((shortcut,view) in shortcut2viewB){
            val change=mapOf(
                View.VISIBLE to View.GONE,
                View.INVISIBLE to View.VISIBLE,//to be complete
                View.GONE to View.VISIBLE,
            )
            findViewById<Button>(shortcut).setOnClickListener({
                var window:View=findViewById<View>(view)
                if (it.id==R.id.show_files){

                    if (editor.get_account()!=null) {
                        window.visibility=change[window.visibility]!!
                        findViewById<View>(R.id.server_connect_to).visibility = View.GONE//TODO repair
                        Log.e("NEED", "FILES")
                        editor.update_file_list()
                        clear_ui()
                        findViewById<View>(R.id.tableLayout).visibility=View.GONE
                    }
                }
                else{
                    window.visibility=change[window.visibility]!!
                    findViewById<View>(R.id.server_connect_to).visibility=View.GONE//TODO repair
                }
            })
        }




        findViewById<ImageButton>(R.id.clear_filters).setOnClickListener({
            clear_ui()
        })
        /*


        findViewById<ImageButton>(R.id.options).setOnClickListener({
            Log.e("MENU","OPEN MENU")
            if(findViewById<View>(R.id.tableLayout).visibility == View.VISIBLE){
                findViewById<View>(R.id.tableLayout).visibility=View.GONE
            }else{
                Log.e("MENU","show MENU")
                findViewById<View>(R.id.tableLayout).visibility=View.VISIBLE
            }
        })

        findViewById<ImageButton>(R.id.show_nav).setOnClickListener({
            if(findViewById<View>(R.id.navigation).visibility == View.VISIBLE){
                findViewById<View>(R.id.navigation).visibility=View.GONE
            }else{
                findViewById<View>(R.id.navigation).visibility=View.VISIBLE
            }
        })
        findViewById<ImageButton>(R.id.show_save).setOnClickListener({
            if(findViewById<View>(R.id.saving).visibility == View.VISIBLE){
                findViewById<View>(R.id.saving).visibility=View.GONE
            }else{
                findViewById<View>(R.id.saving).visibility=View.VISIBLE
            }
        })
        findViewById<ImageButton>(R.id.show_zoom).setOnClickListener({
            if(findViewById<View>(R.id.zoom).visibility == View.VISIBLE){
                findViewById<View>(R.id.zoom).visibility=View.GONE
            }else{
                findViewById<View>(R.id.zoom).visibility=View.VISIBLE
            }
        })
        findViewById<ImageButton>(R.id.show_add).setOnClickListener({
            if(findViewById<View>(R.id.add_del).visibility == View.VISIBLE){
                findViewById<View>(R.id.add_del).visibility=View.GONE
            }else{
                findViewById<View>(R.id.add_del).visibility=View.VISIBLE
            }
        })
        findViewById<ImageButton>(R.id.show_color).setOnClickListener({
            if(findViewById<View>(R.id.color_pallet).visibility == View.VISIBLE){
                findViewById<View>(R.id.color_pallet).visibility=View.GONE
            }else{
                findViewById<View>(R.id.color_pallet).visibility=View.VISIBLE
            }
        })

         */

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
    private fun readFile(path: String):String
    {
        Log.e("READ CONTENT","try")

        if(true){
            try{
                var myFile = File(path)
                if(!path.contains(Environment.getExternalStorageDirectory().toString()))
                    myFile=File(Environment.getExternalStorageDirectory().toString()+'/'+path.split(':')[1])

                Log.e("READ CONTENT","OK")
                var fileInputStream = FileInputStream(myFile)
                var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder: StringBuilder = StringBuilder()
                var text: String? = null
                while ({ text = bufferedReader.readLine(); text }() != null) {
                    stringBuilder.append(text)
                    stringBuilder.append("\n")
                }
                fileInputStream.close()
                Log.e("CONTENT",stringBuilder.toString())
                return stringBuilder.toString()
            } catch (e: IOException) {
                binding.tools.Wynik.text = "Blad"
                e.printStackTrace()
                assert(false)
                return ""
            }
        }

        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == RESULT_OK) {
            val selectedFile = data?.data
            Log.e("READING",selectedFile?.path.toString())
            editor.load_from_string(readFile(selectedFile?.path.toString()))
        }}

    //Save file
    private fun saveFile(path:String,content:String)
    {
        if(binding.saving.FileName.text.toString().endsWith(".txt"))
        {
            Log.e("SAVE","XX")
            //Creating app folder if it doesnt exist
            val myExternalDirectory = File(Environment.getExternalStorageDirectory().toString()+"/VoxNet")
            if(!myExternalDirectory.exists())
            {
                myExternalDirectory.mkdir()
            }

            //If name is not null
            if(path != null)
            {
                val myExternalFile = File(myExternalDirectory.path, binding.saving.FileName.text.toString().trim())
                try {
                    val fileOutPutStream = FileOutputStream(myExternalFile)
                    fileOutPutStream.write(content.toByteArray())
                    fileOutPutStream.close()
                    val toast = Toast.makeText(this, "File saved as "+binding.saving.FileName.text, Toast.LENGTH_LONG)
                    toast.show()
                    binding.saving.FileName.text.clear()
                } catch (e: IOException) {
                    e.printStackTrace()
                    val toast = Toast.makeText(this, "File "+binding.saving.FileName.text+": opening failure", Toast.LENGTH_LONG)
                    toast.show()
                    binding.saving.FileName.text.clear()
                }
            }
        }else
        {
            val toast = Toast.makeText(this, "File name NEED to end with .txt", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    //Ask for permition
    private fun requestPermision(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)//TODO add for version 10 and below
        {
            Log.e("FILE_PER","XX")
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
        else{
            Log.e("FILE_PER","YY")
            assert(false)
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        //11 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if(Environment.isExternalStorageManager())
            {
                saveFile("res.txt",editor.save_to_string())
            }
        }
    }

    //Checking app permision to write and read files
    @RequiresApi(Build.VERSION_CODES.R) //TODO add permisions for android 10 and below
    private fun checkPermission(): Boolean{
        return Environment.isExternalStorageManager()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("File_name",binding.saving.FileName.text.toString())

        if(findViewById<View>(R.id.navigation).visibility == View.VISIBLE)
            outState.putBoolean("Navigation",true)
        else
            outState.putBoolean("Navigation",false)
        if(findViewById<View>(R.id.zoom).visibility == View.VISIBLE)
            outState.putBoolean("Zoom",true)
        else
            outState.putBoolean("Zoom",false)
        if(findViewById<View>(R.id.add_del).visibility == View.VISIBLE)
            outState.putBoolean("Add",true)
        else
            outState.putBoolean("Add",false)
        if(findViewById<View>(R.id.color_pallet).visibility == View.VISIBLE)
            outState.putBoolean("Color",true)
        else
            outState.putBoolean("Color",false)
        if(findViewById<View>(R.id.saving).visibility == View.VISIBLE)
            outState.putBoolean("Save",true)
        else
            outState.putBoolean("Save",false)
        if(findViewById<View>(R.id.loginscreen).visibility == View.VISIBLE)
            outState.putBoolean("Login",true)
        else
            outState.putBoolean("Login",false)
        if(findViewById<View>(R.id.registerscreen).visibility == View.VISIBLE)
            outState.putBoolean("Register",true)
        else
            outState.putBoolean("Register",false)
        if(findViewById<View>(R.id.server).visibility == View.VISIBLE)
            outState.putBoolean("Server",true)
        else
            outState.putBoolean("Server",false)
        if(findViewById<View>(R.id.fileList).visibility == View.VISIBLE)
            outState.putBoolean("List",true)
        else
            outState.putBoolean("List",false)
    }
}