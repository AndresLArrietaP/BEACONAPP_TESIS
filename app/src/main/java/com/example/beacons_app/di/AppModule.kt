
// AppModule.kt
package com.example.beacons_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.DataBeacon.BLE.GestorAsistenciaBLEReceptor
import com.example.beacons_app.models.Beacon
import com.example.beacons_app.models.Asistencia
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.util.Recurso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideBluetoothAdapter(@ApplicationContext ctx: Context): BluetoothAdapter {
        val manager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    fun provideGestorAsistencia(
        bluetoothAdapter: BluetoothAdapter,
        @ApplicationContext ctx: Context,
        database: FirebaseDatabase,
        auth: FirebaseAuth
    ): GestorAsistenciaReceptor = GestorAsistenciaBLEReceptor(
        bluetoothAdapter, ctx, database, auth
    )
}
