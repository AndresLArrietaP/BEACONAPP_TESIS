package com.example.beacons_app.models

data class Usuario(
    val usuario_id: Int = 0,
    val u_tipo: String = "",
    val u_nombres: String = "",
    val u_apellidos: String = "",
    val cu: String = "",
    val u_contrasena: String = "",
    val u_nrotelefonico: String = "",
    val notificacion: Boolean = false
){
    constructor() : this(
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        false)
}