package com.example.myapplication.renderlib

import android.util.Log

var ShadersToComplie= arrayOf<Shader>()

class Shader {
    var GL_PROGRAM=-1//waiting form GLRenderer to start
    val vertexShaderCode =
        //"out vec4 vertexColor;"+
        """
                
                uniform mat4 uMVPMatrix;
                attribute vec4 vPosition;
                attribute vec4 vNormal;
                attribute vec4 vUV;
                varying vec4 myNormal;
                varying vec4 myUV;
                void main() {
                gl_Position = uMVPMatrix*vPosition;
                myNormal=vNormal;
                myUV=vUV;
                //myColor=vec4(0.0,1.0,0.0,1.0);
                }
            """

    val fragmentShaderCode =
        // "in vec4 vertexColor;"+
        """
                precision mediump float;
                varying vec4 myNormal;
                uniform vec4 vColor;
                varying vec4 myUV;
                uniform sampler2D ourTexture;
                void main() {
                    //gl_FragColor = myColor;
                    vec4 res=vColor;
                    vec2 uv=vec2(myUV.x,myUV.y);
                    vec4 light_dir=vec4(1.0,0.0,0.0,0.0);
                    float light=dot(light_dir,myNormal);
                    if (light<0.0){
                    res=0.1*res;
                    }
                    else{
                    res=(0.1+light)*res;
                    }
                    
                    //gl_FragColor = vec4(uv,0.0,0.0)
                    gl_FragColor =vColor*texture2D(ourTexture, uv)*(0.8+0.2*light);
                    //if (texture2D(ourTexture, uv).g!=0.0){
                    //gl_FragColor = vec4(1.0,0.0,0.0,0.0);
                    //}
                }
        """
    var GlAttributes= arrayOf<String>("vPosition","vNormal"
        ,"vUV"
    )
    var GlUniforms= arrayOf<String>("vColor")//only vec4 uniforms not uMVPMatrix
    constructor(){
        Log.e("SHADER","NEW")
        ShadersToComplie+=arrayOf(this)
    }
}