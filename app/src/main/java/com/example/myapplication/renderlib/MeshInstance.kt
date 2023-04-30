package com.example.myapplication.renderlib
import com.example.myapplication.renderlib.Material
import de.bixilon.kotlinglm.vec3.Vec3


class MeshInstance{
    var mesh:Mesh=Mesh()
    var material: Material =Material()
    var pos:Vec3= Vec3(0.0)
    fun copy():MeshInstance{
        var res=MeshInstance()
        res.mesh=mesh
        res.material=material
        res.pos=pos
        return res
    }
}