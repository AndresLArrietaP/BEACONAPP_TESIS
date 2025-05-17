package com.example.beacons_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.models.AsistenciaUsuario

class SharedViewModel : ViewModel() {
    val usuario = MutableLiveData<Usuario?>()
    val asistenciaUsuario = MutableLiveData<AsistenciaUsuario?>()
}
