package com.example.myapplication

class helper {
    //for now unused function that prase android color from int to rgba
    fun get_color(color:Int):IntArray= intArrayOf((color shr 24) and 0xff,(color shr 16) and 0xff,(color shr 8) and 0xff,(color shr 0) and 0xff)

}
