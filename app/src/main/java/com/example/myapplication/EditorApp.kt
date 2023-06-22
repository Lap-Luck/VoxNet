package com.example.myapplication
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.EditorApp.Companion.cursor3d
import com.example.myapplication.renderlib.MeshInstance
import com.example.myapplication.renderlib.Material
import com.example.myapplication.renderlib.Mesh
import com.example.myapplication.renderlib.obj.example
import com.example.myapplication.renderlib.obj.parseOBJ
import de.bixilon.kotlinglm.mat3x3.Mat3
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import java.io.Closeable


class EditorApp: View.OnClickListener,View.OnTouchListener{
    companion object{
        var brush=MeshInstance().also {
            it.mesh=parseOBJ(example().cube)
            it.material= Material().also { it.color= Vec4(1.0,1.0,0.5,1.0) }
            it.name="voxel"
        }
        var cursor3d=MeshInstance().also {
            it.mesh=parseOBJ(example().obj)
            it.material= Material().also { it.color= Vec4(0.0,1.0,0.0,1.0) }
            it.name="cursor"
        }
        //mesh Instance that we add per every voxel
        var scene=arrayOf<MeshInstance>()

        var Camera_pos= Vec3(0f,0f,3f)
        var Camera_look_at=Vec3(0f,0f,0f)

        var Camera_start_pos= Vec3(0f,0f,3f)
        var Camera_rot_x=0.0f
        var Camera_rot_y=0.0f

        var net:NetworkFile?=null
        var account:Account?=null

    }
    lateinit var conte:MainActivity

    fun get_Camera_position():Vec3= Camera_pos
    fun get_Camera_look_at():Vec3= Camera_look_at

    var requestRender={->}


    lateinit var scaleGestureDetector:ScaleGestureDetector
    var scaleFactor = 1f
    fun begin(){



        scaleGestureDetector= ScaleGestureDetector(
            conte,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // not working on emulator
                    Log.d("SCALE",scaleFactor.toString())
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

                    return super.onScale(detector)
                }
            }
        )
        //net.clear()
        //net.push("Ala ma kota")
        scene+=arrayOf<MeshInstance>(cursor3d)
    }
    fun get_account():Account?{
        return account
    }

    fun n_render():Array<MeshInstance>{
        return scene
    }
    fun onNeededRender(){
        requestRender()
    }
    fun onButtomPressed(){
        camera_update()
        onNeededRender()
    }
    fun camera_update(){
        Camera_pos= Mat3.identity.scale(scaleFactor.toFloat())*Mat3.identity.rotateY(Camera_rot_y)*
                Mat3.identity.rotateX(Camera_rot_x)*Camera_start_pos
        Camera_look_at=scene[0].pos
    }

    fun on_file_selected(f:FileInfo){
        Log.e("FILE OPEN",f.name)
        net=NetworkFile({event -> EditVox(event)},f,account!!)
        conte.runOnUiThread{
            conte.findViewById<View>(R.id.fileList).visibility=View.GONE
            conte.findViewById<TextView>(R.id.server_info).text=f.name
        }

    }



    fun update_file_list(){
        Thread{
            var files=query_files(account!!)
            conte.runOnUiThread({

                var fl=conte.findViewById<LinearLayout>(R.id.file_list)
                if (fl!=null){
                    for( id in 1..fl.childCount-1){
                        fl.getChildAt(id).visibility=View.GONE
                    }

                    for (f in files){
                        var new_button=Button(conte)
                        new_button.text=f.name
                        new_button.setOnClickListener({on_file_selected((f))})

                        fl.addView(new_button)
                    }
                }

            })

        }.start()

    }

    fun on_add_file(public:Boolean=false){
        Log.e("FILE add","TRY")
        var name=conte.findViewById<EditText>(R.id.file_name).text.toString()
        var f=add_file(name,account!!)//TODO DO NOT CRUSH
        if(public){
            make_public(f,account!!)
        }
        update_file_list()
    }

    fun on_login(){
        Log.e("LOGIN","on login")
        var et_login=conte.findViewById<EditText>(R.id.editText_login)
        var et_pass=conte.findViewById<EditText>(R.id.editText_pass)

        var login=et_login.text.toString()
        var pass=et_pass.text.toString()
        Log.e("LOGIN", login+"->"+pass)
        var a=Account(login,pass,false) //register

        conte.runOnUiThread{
        if (a.login==""){
            val toast = Toast.makeText(conte,"Login error", Toast.LENGTH_LONG)
            toast.show()
            conte.findViewById<EditText>(R.id.editText_login).text.clear()
            conte.findViewById<EditText>(R.id.editText_pass).text.clear()
        }
        else
        {
            val toast = Toast.makeText(conte,"You have successfully logged in", Toast.LENGTH_LONG)
            toast.show()
            conte.findViewById<EditText>(R.id.editText_login).text.clear()
            conte.findViewById<EditText>(R.id.editText_pass).text.clear()
            account=a
            var view=conte.findViewById<View>(R.id.loginscreen)
            conte.runOnUiThread({view.visibility=View.GONE})
            Log.e("LOGIN", a.login)
        }}

    }

    fun on_register(){
        var et_login=conte.findViewById<EditText>(R.id.reg_loggin)
        var et_pass=conte.findViewById<EditText>(R.id.reg_pass)

        var login=et_login.text.toString()
        var pass=et_pass.text.toString()

        var a=Account(login,pass,true)
        conte.runOnUiThread{
        if (a.login==""){
            val toast = Toast.makeText(conte,"Account already exists", Toast.LENGTH_LONG)
            toast.show()
            conte.findViewById<EditText>(R.id.reg_loggin).text.clear()
            conte.findViewById<EditText>(R.id.reg_pass).text.clear()
        }
        else
        {
            val toast = Toast.makeText(conte,"Successful registration", Toast.LENGTH_LONG)
            toast.show()
            conte.findViewById<EditText>(R.id.reg_loggin).text.clear()
            conte.findViewById<EditText>(R.id.reg_pass).text.clear()
        }
      
        account=a
        var view=conte.findViewById<View>(R.id.loginscreen)
        conte.runOnUiThread({view.visibility=View.GONE})
        Log.e("LOGIN", a.login)
        }
    }

    fun EditVox(info:String){
        var data=info.split("|")
        if (data[0]=="ADD"){
            Log.e("NEW Vexel add",cursor3d.pos.toString())
            scene+=arrayOf<MeshInstance>(brush.copy().also {
                it.pos= Vec3(data[1].toFloat(),data[2].toFloat(),data[3].toFloat())
                it.material=Material().also { it.color= Vec4(data[4].toFloat(),data[5].toFloat(),data[6].toFloat(),1.0) }
                it.name= brush.name//WHY
            })
        }
        if (data[0]=="DEL"){
            var del_pos=Vec3(data[1].toFloat(),data[2].toFloat(),data[3].toFloat())
            Log.e("NEW Vexel delete",cursor3d.pos.toString())
            var n_scene=arrayOf<MeshInstance>()
            for (ob in scene){
                var removed=false
                if (ob!= cursor3d){
                    if (ob.pos== del_pos)
                    {
                        removed=true
                    }
                }
                if (!removed){
                    n_scene+=arrayOf<MeshInstance>(ob)
                }
            }
            scene=n_scene
        }
        //assert(info.size==7)//format 0<- 'A' or 'D' 1..3<-x,y,z    4..6<-rgb
        onButtomPressed()
    }



    override fun onClick(button: View?) {
        when(button?.id){
            R.id.server_connect->{
                scene=arrayOf<MeshInstance>(cursor3d)
                //net=Network { event -> EditVox(event) }
            }
            R.id.server_disconnect->{
                net=null
                conte.findViewById<TextView>(R.id.server_info).text="local"
            }
            R.id.add_file->{
                Thread{on_add_file()}.start()
            }

            R.id.make_public->{
                Thread{on_add_file()}.start()
            }
            R.id.ADD_elem->{
                var event= arrayOf("ADD",
                    cursor3d.pos.x.toString(),
                    cursor3d.pos.y.toString(),
                    cursor3d.pos.z.toString(),
                    cursor3d.material.color.r.toString(),
                    cursor3d.material.color.g.toString(),
                    cursor3d.material.color.b.toString()
                    ).reduce{a,b->"$a|$b"}
                if (net!=null){
                    net!!.push(event)
                }
                else{
                    EditVox(event)
                }
            }
            R.id.button_login->{
                Thread{on_login()}.start()

            }
            R.id.register->{
                Thread{on_register()}.start()
            }
            R.id.guest->{
                conte.findViewById<ImageButton>(R.id.server).visibility = View.GONE
                val toast = Toast.makeText(conte,"Logged in as a guest", Toast.LENGTH_LONG)
                toast.show()
                var view=conte.findViewById<View>(R.id.loginscreen)
                view.visibility=View.GONE
            }
            R.id.DEL_elem->{
                var event= arrayOf("DEL",
                    cursor3d.pos.x.toString(),
                    cursor3d.pos.y.toString(),
                    cursor3d.pos.z.toString(),
                ).reduce{a,b->"$a|$b"}
                if (net!=null){
                    net!!.push(event)
                }
                else{
                    EditVox(event)
                }

            }
            R.id.X_Minus->{cursor3d.pos=cursor3d.pos+Vec3(-2f,0f,0f)}
            R.id.X_Add->{cursor3d.pos=cursor3d.pos+Vec3(2f,0f,0f)}
            R.id.Y_Minus->{cursor3d.pos=cursor3d.pos+Vec3(0f,-2f,0f)}
            R.id.Y_Add->{cursor3d.pos=cursor3d.pos+Vec3(0f,2f,0f)}
            R.id.Z_Minus->{cursor3d.pos=cursor3d.pos+Vec3(0f,0f,-2f)}
            R.id.Z_Add->{cursor3d.pos=cursor3d.pos+Vec3(0f,0f,2f)}

            R.id.Red_color->{cursor3d.material= Material().also { it.color= Vec4(1.0,0.0,0.0,1.0) }}
            R.id.Green_color->{cursor3d.material= Material().also { it.color= Vec4(0.0,1.0,0.0,1.0) }}
            R.id.Blue_color->{cursor3d.material= Material().also { it.color= Vec4(0.0,0.0,1.0,1.0) }}

            R.id.Zoom_Minus->{
                Camera_start_pos=Camera_start_pos*(1.0f/0.9f)
            }
            R.id.Zoom_Add->{
                Camera_start_pos=Camera_start_pos*0.9f
                onButtomPressed()
            }

        }
        onButtomPressed()
    }
    var start_mouse:Vec2=Vec2(0.0f)

    override fun onTouch(unused: View?, event: MotionEvent?): Boolean {
        event?.let{e->
            val x: Float = e.x
            val y: Float = e.y
            scaleGestureDetector.onTouchEvent(event)
            when (e.action) {
                MotionEvent.ACTION_DOWN ->{
                    start_mouse=Vec2(x,y)
                }
                MotionEvent.ACTION_MOVE -> {
                    var move=Vec2(x,y)-start_mouse
                    Camera_rot_x+=-0.001f*move.y
                    Camera_rot_y+=-0.001f*move.x
                    camera_update()
                    onNeededRender()
                    start_mouse=Vec2(x,y)
                }
                MotionEvent.ACTION_UP -> {
                    var move=Vec2(x,y)-start_mouse
                    Camera_rot_x+=0.001f*move.y
                    Camera_rot_y+=0.001f*move.x
                    camera_update()
                    onNeededRender()
                }
            }
        }
        return true
    }

    fun save_to_string():String{
        var res=""
        Log.w("START",">>>>>>>>>>>>>>>")
        Log.w("FULL SCENE SIZE", scene.size.toString())
        for(m in scene){
            Log.w("LOOP", m.name)
            if(m.name=="voxel"){
                Log.e("MESH","VOX")
                res+=m.pos.x.toString()+","+m.pos.y.toString()+","+m.pos.z.toString()+","+(
                        m.material.color.r.toString()+","+m.material.color.g.toString()+","+m.material.color.b.toString()
                        )+"\n"

            }
            else{
                Log.e("MESH","??")
            }
        }
        Log.w("SCENE",res)
        Log.e("SCENE_SIZE",res.length.toString())
        Log.w("END",">>>>>>>>>>>>>>>")
        return res
    }
    fun load_from_string(file:String){
        var new_scene=arrayOf<MeshInstance>()
        for(m in scene){
            if(m.name!="voxel"){
                new_scene+=arrayOf<MeshInstance>(m)
            }
        }
        Log.e("File",file)
        for (line in file.split("\n")){
            Log.e("LINe",line)
            if (line.length>5){
                val mesh_data=line.split(",").map { it.toFloat() }
                new_scene+=arrayOf<MeshInstance>(brush.copy().also {
                    it.pos= Vec3(mesh_data[0],mesh_data[1],mesh_data[2])
                    it.material=Material().also { it.color= Vec4(mesh_data[3],mesh_data[4],mesh_data[5],1.0) }
                })
            }
        }
        scene=new_scene
    }


}

