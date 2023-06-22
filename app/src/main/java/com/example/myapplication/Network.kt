package com.example.myapplication

import android.net.wifi.hotspot2.pps.Credential
import android.util.Log
import org.json.JSONArray
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

val server_url="https://michapp.pythonanywhere.com"

fun requestGet(page:String):String {
    val url = URL(page)
    val connection = url.openConnection() as HttpsURLConnection
    val inputSR = InputStreamReader(connection.inputStream, "UTF-8")
    var t = inputSR.readText()
    Log.e("GET",page+"->"+t)

    return t
}


fun interface NetworkListener {
    fun on_event(event:String)
}


class Account {
    var login=""
    var password=""
    constructor(login:String,password:String,new:Boolean=false){
        if(new){
            var ok=requestGet(server_url+"/register/"+login+"/"+password)
            if (ok=="OK")
            {
                this.login=login
                this.password=password
            }
            else{
                Log.e("LOGIN","LOGIN ERROR")
            }

        }
        else{
            var ok=requestGet(server_url+"/login/"+login+"/"+password)
            if (ok=="OK")
            {
                this.login=login
                this.password=password
            }
            else{
                Log.e("LOGIN","LOGIN ERROR")
                assert(ok=="OK")
            }
        }
    }
}

class FileInfo(var id:Int,var name:String)

fun add_file(name:String,user:Account):FileInfo{
    var t=requestGet(server_url+"/add_file/"+name+"/"+user.login+"/"+user.password)
    return FileInfo(t.toInt(),name)
}
fun make_public(file:FileInfo,user: Account){
    var ok=requestGet(server_url+"/add_file/"+file.id.toString()+"/"+user.login+"/"+user.password)
    assert(ok=="OK")
}

fun query_files(user:Account):Array<FileInfo>{
    var res=arrayOf<FileInfo>()
    var t=requestGet(server_url+"/query_files/"+user.login+"/"+user.password)
    var json=JSONArray(t)
    for (id in 0..json.length()-1){
        var obj=json.getJSONObject(id)

        res+=arrayOf<FileInfo>(FileInfo(
            id=obj.getInt("id"),
            name=obj.getString("name"),
        ))
    }
    return res
}


class NetworkFile(on_event_:NetworkListener,file:FileInfo,user:Account){
    var event_id=0
    var events=arrayOf<String>()
    var on_event=on_event_
    var file=file
    var user=user
    var CredentialsString=file.id.toString()+"/"+user.login+"/"+user.password
    var run:Unit=NetworkLoop()
    private fun NetworkLoop(){
        Thread{
            Log.e("WEB START", "6something")
            while (true){
                Log.e("WEB LOOP", "6something")
                var t=requestGet(server_url+"/file_get_event/"+event_id.toString()+"/"+CredentialsString)

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
            var t=requestGet(server_url+"/file_event/"+event+"/"+CredentialsString)
            Log.e("WEB SEND", event)
        }.start()
    }
    fun clear(){
        Thread{
            val url = URL(server_url+"/file_clear/"+CredentialsString)
            val connection = url.openConnection() as HttpsURLConnection
            val inputSR = InputStreamReader(connection.inputStream, "UTF-8")
            var t=inputSR.readText()
            Log.e("WEB CLEAR", "CLEAR")
        }.start()
    }

}