package com.example.beacons_app.models

import com.example.beacons_app.models.Horario
import com.example.beacons_app.models.Usuario

data class HorarioUsuario(
    val horario: Horario = Horario(),
    val periodo: String = "",
    val semana: Int = 0,
    val usuario: Usuario = Usuario()
) {
    constructor() : this(Horario(), "", 0, Usuario())
}

