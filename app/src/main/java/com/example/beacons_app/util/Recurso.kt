package com.example.beacons_app.util

sealed class Recurso<out T:Any>{
    data class Success<out T:Any> (val data:T):Recurso<T>()
    data class Error(val errorMessage:String):Recurso<Nothing>()
    data class Loading<out T:Any>(val data:T? = null, val message:String? = null):Recurso<T>()

    object Idle : Recurso<Nothing>()

}