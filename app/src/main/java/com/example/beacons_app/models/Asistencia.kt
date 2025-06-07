package com.example.beacons_app.models

import com.example.beacons_app.DataBeacon.EstadoConexion

data class Asistencia(
    var asistencia_id: Int = 0,
    val beacons_id: Int = 0,
    val beacon_uuid: String = "",
    val modalidad_id: Int = 0,
    val horario_id: Int = 0,
    val estado: Boolean = false,
    val descripcion: String = "",
    val conexion: String = "Uninitialized"
) {
    constructor() : this(
        0,
        0,
        "",
        0,
        0,
        false,
        "",
        "")
}