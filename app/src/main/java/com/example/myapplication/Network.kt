package com.example.myapplication

import android.util.Log
import org.json.JSONArray
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

val server_url=




fun interface NetworkListener {
    fun on_event(event:String)
}

class Network(on_event_:NetworkListener){
    var event_id=0
    var events=arrayOf<String>()
    var on_event=on_event_
    var run:Unit=NetworkLoop()
    private fun NetworkLoop(){
        Thread{
            Log.e("WEB START", "6something")
            while (true){
                Log.e("WEB LOOP", "6something")
                val url = URL(server_url+"/get_event/"+event_id.toString()+"/")
                val connection = url.openConnection() as HttpsURLConnection
                val inputSR = InputStreamReader(connection.inputStream, "UTF-8")
                var t=inputSR.readText()

                Log.e("WEB RECIVED", "something")
                Log.e("WEB RECIVED", t)
                var a= JSONArray(t)
                if (a.length()>0){
                    for (i:Int in 0..a.length()-1){
                        var event=a.getString(i)
                        events+=arrayOf(event)
                        on_event.on_event(event)
                    }
                    event_id+=a.length()
                }
                Thread.sleep(10_000)
            }
        }.start()
    }
    fun push(event:String){
        Thread{
            val url = URL(server_url+"/event/"+event+"/")
            val connection = url.openConnection() as HttpsURLConnection
            val inputSR = InputStreamReader(connection.inputStream, "UTF-8")
            var t=inputSR.readText()
            Log.e("WEB SEND", event)
        }.start()
    }
    fun clear(){
        Thread{
            val url = URL(server_url+"/clear/")
            val connection = url.openConnection() as HttpsURLConnection
            val inputSR = InputStreamReader(connection.inputStream, "UTF-8")
            var t=inputSR.readText()
            Log.e("WEB CLEAR", "CLEAR")
        }.start()
    }

}