package com.example.myapplication.renderlib
import de.bixilon.kotlinglm.vec4.Vec4

var defaultShader:Shader= Shader()

class Material {
    var shader:Shader=defaultShader
    var GlUniforms=mutableMapOf("vColor" to (Vec4(0.0,0.0,0.0,1.0)))
    //var GlAttributes=mutableMapOf("vPosition" to 0)
    //TODO this check:
    //constructor(){
    //    assert(GlAttributes.keys contentEquals shader.GlAttributes)
    //    assert(GlUniforms.keys contentEquals  shader.GlUniforms)
    //}
    var color: Vec4
        get(){
            var res=Vec4()
            GlUniforms["vColor"]?.let{res=it}
            return res
        }
        set(value:Vec4){
            GlUniforms.set("vColor",value)
        }
}