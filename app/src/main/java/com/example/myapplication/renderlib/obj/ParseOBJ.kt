package com.example.myapplication.renderlib.obj
import android.util.Log
import com.example.myapplication.renderlib.Mesh
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i

var print_debug:Boolean=false

fun parseOBJ(obj_model:String):Mesh {
    var res:Mesh=Mesh()
    var vetices=arrayOf<Vec3>()
    var normal=arrayOf<Vec3>()
    var uv=arrayOf<Vec3>()//TODO change to vec2
    var faces=arrayOf<Vec3i>()
    var faces_n=arrayOf<Vec3i>()
    var faces_uv=arrayOf<Vec3i>()
    if (true){//step 1 read indices and coordinates
        if (print_debug){Log.e("OBJ",obj_model.slice(0..20))}
        fun v_parse(t:String):Vec3{
            var nums_text=t.split(" ")
            var res:FloatArray=floatArrayOf()
            for (n in nums_text){
                if (n.length>0){
                    res+=floatArrayOf(n.toFloat())
                }
            }
            //Log.d("V",res.contentToString())
            //print(res.contentToString())
            return Vec3(res[0],res[1],res[2])
        }
        fun uv_parse(t:String):Vec3{
            var nums_text=t.split(" ")
            var res:FloatArray=floatArrayOf()
            for (n in nums_text){
                if (n.length>0){
                    res+=floatArrayOf(n.toFloat())
                }
            }
            //Log.d("V",res.contentToString())
            //print(res.contentToString())
            return Vec3(res[0],res[1],0f)
        }
        fun f_parse(t:String,id:Int=0):Vec3i{
            var nums_text=t.split(" ")
            var res=intArrayOf()
            for (n in nums_text){
                if (n.length>0){
                    var vex_id:String=n.split("/")[id]
                    res+=intArrayOf(vex_id.toInt()-1)
                }
            }
            //print(res.contentToString())
            return Vec3i(res[0],res[1],res[2])
        }
        var v=arrayOf<Vec3>()
        var vn=arrayOf<Vec3>()
        var vuv=arrayOf<Vec3>()
        var f=arrayOf<Vec3i>()
        var fn=arrayOf<Vec3i>()
        var fuv=arrayOf<Vec3i>()
        for (line in obj_model.split('\n')){
            if (print_debug){Log.e("Line",line)}
            var sline=line.split(" ",limit=2)
            if (sline.size==2){
                var prefix=sline[0]
                var sufix=sline[1]
                when(prefix){
                    "v"->v+=v_parse(sufix.toString())
                    "vn"->vn+=v_parse(sufix.toString())
                    "vt"->vuv+=uv_parse(sufix.toString())
                    "f"->{f+=f_parse(sufix.toString())
                        fn+=f_parse(sufix.toString(),2)
                        fuv+=f_parse(sufix.toString(),1)
                    }

                }
            }
        }
        Log.d("PARSE",v.contentToString())
        vetices=v
        normal=vn
        uv=vuv
        faces=f
        faces_n=fn
        faces_uv=fuv

    }
    for (face in faces){
        for (v_id in face.toIntArray()){
            res.vertex_array+=vetices[v_id].toFloatArray()
        }
    }
    for (face_n in faces_n){
        for (v_id in face_n.toIntArray()){
            res.normal_array+=normal[v_id].toFloatArray()
        }
    }
    for (face_uv in faces_uv){
        for (v_id in face_uv.toIntArray()){
            res.uv_array+=uv[v_id].toFloatArray()
        }
    }

    return res
}
