package com.example.beacons_app.DataBeacon

sealed interface EstadoConexion {
    object Connected : EstadoConexion
    object Disconnected : EstadoConexion
    object Uninitialized : EstadoConexion
    object CurrentlyInitializing : EstadoConexion

    companion object {
        fun fromString(value: String): EstadoConexion = when (value) {
            "Connected" -> Connected
            "Disconnected" -> Disconnected
            "Uninitialized" -> Uninitialized
            "CurrentlyInitializing" -> CurrentlyInitializing
            else -> Uninitialized
        }

        fun toString(estado: EstadoConexion): String = when (estado) {
            Connected -> "Connected"
            Disconnected -> "Disconnected"
            Uninitialized -> "Uninitialized"
            CurrentlyInitializing -> "CurrentlyInitializing"
        }
    }
}
