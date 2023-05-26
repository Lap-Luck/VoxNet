package com.example.myapplication
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
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
        scene+=arrayOf<MeshInstance>(cursor3d)
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

    override fun onClick(button: View?) {
        when(button?.id){
            R.id.button_add->{
                Log.d("Vexel add",cursor3d.pos.toString())
                scene+=arrayOf<MeshInstance>(brush.copy().also {
                    it.pos= cursor3d.pos
                    it.material=cursor3d.material
                    it.name= brush.name//WHY
                })
            }
            R.id.button_del->{
                Log.d("Vexel delete",cursor3d.pos.toString())
                var n_scene=arrayOf<MeshInstance>()
                for (ob in scene){
                    var removed=false
                    if (ob!= cursor3d){
                        if (ob.pos== cursor3d.pos)
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
            R.id.button_move_x_m->{cursor3d.pos=cursor3d.pos+Vec3(-2f,0f,0f)}
            R.id.button_move_x_p->{cursor3d.pos=cursor3d.pos+Vec3(2f,0f,0f)}
            R.id.button_move_y_m->{cursor3d.pos=cursor3d.pos+Vec3(0f,-2f,0f)}
            R.id.button_move_y_p->{cursor3d.pos=cursor3d.pos+Vec3(0f,2f,0f)}
            R.id.button_move_z_m->{cursor3d.pos=cursor3d.pos+Vec3(0f,0f,-2f)}
            R.id.button_move_z_p->{cursor3d.pos=cursor3d.pos+Vec3(0f,0f,2f)}

            R.id.button_color_r->{cursor3d.material= Material().also { it.color= Vec4(1.0,0.0,0.0,1.0) }}
            R.id.button_color_g->{cursor3d.material= Material().also { it.color= Vec4(0.0,1.0,0.0,1.0) }}
            R.id.button_color_b->{cursor3d.material= Material().also { it.color= Vec4(0.0,0.0,1.0,1.0) }}

            R.id.button_zoom_minus->{
                Camera_start_pos=Camera_start_pos*(1.0f/0.9f)
            }
            R.id.button_zoom_plus->{
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

