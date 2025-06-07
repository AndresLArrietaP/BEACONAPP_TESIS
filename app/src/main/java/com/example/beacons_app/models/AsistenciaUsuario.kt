package com.example.beacons_app.models

data class AsistenciaUsuario(
    var usuario: Usuario = Usuario(),  // REQUIERE que Usuario ya tenga constructor vacío (que ya lo tienes ✅)
    var asistencia: Asistencia = Asistencia(), // igual, ya tiene constructor vacío ✅
    var fecha_registro: String = "",
    var hora_registro: String = ""
) {
    // Constructor vacío obligatorio para Firebase
    constructor() : this(
        Usuario(),
        Asistencia(),
        "",
        ""
    )
}
