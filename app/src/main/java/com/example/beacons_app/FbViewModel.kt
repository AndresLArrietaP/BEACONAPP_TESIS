package com.example.beacons_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.Event
import com.example.beacons_app.models.Curso
import com.example.beacons_app.models.Horario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FbViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    companion object {
        private const val TAG = "FbViewModel"
    }

    // Estado de sesión iniciada
    val signedIn = mutableStateOf(false)
    // Estado de operación en progreso (ej. mostrando indicador de carga durante autenticación)
    val inProgress = mutableStateOf(false)
    // Evento de notificación para mostrar mensajes (éxito/error) a la IU
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val cursos = mutableStateOf<List<Curso>>(emptyList())

    // Referencia a la raíz de la base de datos en tiempo real
    private val databaseRef = database.reference
    private val refUsuarios = database.getReference("users")
    private val refCursos = database.getReference("curso")
    private val refHorarios = database.getReference("horario")
    private val refHorarioUsuario = database.getReference("horario_usuario")
    /**
     * Registra un nuevo usuario con correo y contraseña, luego guarda la información adicional del [usuario] en la base de datos.
     */
    fun onSignup(email: String, pass: String, usuario: Usuario) {
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Registro exitoso
                    signedIn.value = true
                    val userId = auth.currentUser?.uid ?: ""
                    // Obtener el próximo ID de usuario disponible y guardar datos en "users"
                    getNextUserId { nextId ->
                        val newUser = usuario.copy(usuario_id = nextId)
                        saveUserToDatabase(userId, newUser)
                    }
                    handleException(it.exception, "Registro exitoso")
                } else {
                    // Registro fallido
                    handleException(it.exception, "Registro fallido")
                }
                inProgress.value = false
            }
    }

    /**
     * Calcula el siguiente identificador de usuario (usuario_id) de forma secuencial consultando la base de datos.
     */
    private fun getNextUserId(callback: (Int) -> Unit) {
        databaseRef.child("users").orderByChild("usuario_id").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Tomar el mayor usuario_id existente y sumar 1
                        val lastId = snapshot.children.mapNotNull {
                            it.child("usuario_id").getValue(Int::class.java)
                        }.maxOrNull() ?: 0
                        callback(lastId + 1)
                    } else {
                        // Si no existe ningún usuario aún, comenzar IDs desde 1
                        callback(1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // En caso de error en la consulta, devolver 1 por defecto
                    callback(1)
                }
            })
    }

    /**
     * Almacena el objeto [usuario] bajo el ID de Firebase [userId] en la rama "users" de la base de datos.
     */
    fun saveUserToDatabase(userId: String, usuario: Usuario) {
        databaseRef.child("users").child(userId).setValue(usuario)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    popupNotification.value = Event("Usuario guardado exitosamente")
                } else {
                    handleException(it.exception, "Error al guardar usuario")
                }
            }
    }

    /**
     * Inicia sesión con el correo y contraseña proporcionados. Si tiene éxito, obtiene el objeto [Usuario] desde la base de datos y lo retorna vía [callback].
     */
    fun login(email: String, pass: String, callback: (Usuario?) -> Unit) {
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    val userId = auth.currentUser?.uid ?: ""
                    // Leer datos del usuario desde "users/{userId}"
                    databaseRef.child("users").child(userId).get()
                        .addOnSuccessListener { snapshot ->
                            val usuario = snapshot.getValue(Usuario::class.java)
                            callback(usuario)
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                    handleException(task.exception, "Inicio exitoso")
                } else {
                    handleException(task.exception, "Inicio fallido")
                    callback(null)
                }
                inProgress.value = false
            }
    }

    /**
     * Cierra la sesión actual de Firebase.
     */
    fun logout() {
        auth.signOut()
        signedIn.value = false
    }

    fun getDocentes(onResult: (List<Usuario>) -> Unit) {
        database.getReference("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
                    .filter { it.u_tipo == "Docente" }
                println("DEBUG Usuarios filtrados -> ${lista.size}")
                lista.forEach { println("DEBUG Usuario -> $it") }
                onResult(lista)
            }
            .addOnFailureListener { exception ->
                println("ERROR al obtener docentes -> ${exception.localizedMessage}")
                onResult(emptyList())
            }
    }

    fun obtenerCursosDisponiblesFiltrados(idUsuario: Int, onResult: (List<Pair<Int, String>>) -> Unit) {
        refCursos.get().addOnSuccessListener { cursosSnapshot ->
            val todosLosCursos = cursosSnapshot.children.mapNotNull {
                val id = it.child("id_curso").getValue(Int::class.java)
                val nombre = it.child("nombre").getValue(String::class.java)
                if (id != null && nombre != null) id to nombre else null
            }

            refHorarioUsuario.child(idUsuario.toString()).get().addOnSuccessListener { horariosSnap ->
                val cursosAsignados = horariosSnap.children.mapNotNull {
                    it.child("horario/id_curso").getValue(Int::class.java)
                }.toSet()

                val disponibles = todosLosCursos.filterNot { cursosAsignados.contains(it.first) }
                onResult(disponibles)
            }.addOnFailureListener {
                onResult(todosLosCursos)
            }
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    fun obtenerDiasYHorasDisponibles(idUsuario: Int, idCurso: Long, onResult: (Map<String, List<Int>>) -> Unit) {
        refHorarios.get().addOnSuccessListener { snapshot ->
            val ocupadasPorCurso = mutableMapOf<String, MutableList<Int>>()
            snapshot.children.forEach { child ->
                val cursoId = child.child("id_curso").getValue(Long::class.java) ?: return@forEach
                val dia = child.child("dia").getValue(String::class.java) ?: return@forEach
                val horas = child.child("horas").children.mapNotNull { it.getValue(Int::class.java) }

                if (cursoId != idCurso) { // ✅ PERMITIR que el mismo curso repita horas
                    ocupadasPorCurso.getOrPut(dia) { mutableListOf() }.addAll(horas)
                }
            }

            refHorarioUsuario.child(idUsuario.toString()).get().addOnSuccessListener { userHorSnap ->
                val ocupadasPorUsuario = mutableMapOf<String, MutableList<Int>>()
                userHorSnap.children.forEach { child ->
                    val dia = child.child("horario/dia").getValue(String::class.java) ?: return@forEach
                    val horas = child.child("horario/horas").children.mapNotNull { it.getValue(Int::class.java) }
                    ocupadasPorUsuario.getOrPut(dia) { mutableListOf() }.addAll(horas)
                }

                val resultado = mutableMapOf<String, List<Int>>()
                val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
                val todasHoras = (1..7).toList()
                for (dia in diasSemana) {
                    val horasOcupadas = (ocupadasPorCurso[dia] ?: emptyList()) + (ocupadasPorUsuario[dia] ?: emptyList())
                    resultado[dia] = todasHoras - horasOcupadas.toSet()
                }

                onResult(resultado)
            }.addOnFailureListener {
                onResult(emptyMap())
            }
        }.addOnFailureListener {
            onResult(emptyMap())
        }
    }



    fun obtenerCurso(idCurso: Int, onResult: (Curso?) -> Unit) {
        refCursos.orderByChild("id_curso").equalTo(idCurso.toDouble()).get()
            .addOnSuccessListener { snapshot ->
                val curso = snapshot.children.firstOrNull()?.getValue(Curso::class.java)
                onResult(curso)
            }.addOnFailureListener {
                onResult(null)
            }
    }

    fun obtenerAulasDisponibles(dia: String, horas: List<Long>, onResult: (List<String>) -> Unit) {
        refHorarios.get().addOnSuccessListener { snapshot ->
            val usados = snapshot.children.mapNotNull { child ->
                val diaH = child.child("dia").getValue(String::class.java)
                val horasH = child.child("horas").children.mapNotNull { it.getValue(Long::class.java) }
                val aula = child.child("aula").getValue(String::class.java)
                if (!aula.isNullOrBlank() && diaH == dia && horasH.any { horas.contains(it) }) aula else null
            }
            val disponibles = generarAulasFacultad() - usados.toSet()
            onResult(disponibles.sorted())
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    fun guardarHorarioCompleto(idUsuario: Int, horario: Horario, onResult: (Boolean, String) -> Unit) {
        refCursos.orderByChild("id_curso").equalTo(horario.id_curso.toDouble()).get()
            .addOnSuccessListener { courseSnapshot ->
                if (!courseSnapshot.exists()) {
                    onResult(false, "Curso no encontrado")
                    return@addOnSuccessListener
                }
                val cursoSnap = courseSnapshot.children.firstOrNull()
                val cursoSel = cursoSnap?.getValue(Curso::class.java)
                if (cursoSnap == null || cursoSel == null) {
                    onResult(false, "Curso no encontrado")
                    return@addOnSuccessListener
                }
                val targetCycle = cursoSel.ciclo

                // Verificar conflictos en horario del docente
                refHorarioUsuario.child(idUsuario.toString()).get()
                    .addOnSuccessListener { userHorSnapshot ->
                        var conflictoDocente = false
                        for (child in userHorSnapshot.children) {
                            val diaExistente = child.child("horario/dia").getValue(String::class.java) ?: ""
                            if (diaExistente == horario.dia) {
                                val horasExistentes = child.child("horario/horas").children
                                    .mapNotNull { it.getValue(Long::class.java) }
                                if (horasExistentes.any { horario.horas.contains(it) }) {
                                    conflictoDocente = true
                                    break
                                }
                            }
                        }
                        if (conflictoDocente) {
                            onResult(false, "Existe cruce con otro curso del mismo ciclo o en el horario del docente")
                            return@addOnSuccessListener
                        }

                        // Verificar conflicto con cursos del mismo ciclo (distintos al actual curso)
                        refHorarios.get().addOnSuccessListener { horariosSnap ->
                            val cursosConflicto = mutableSetOf<Long>()
                            for (hChild in horariosSnap.children) {
                                val diaHorario = hChild.child("dia").getValue(String::class.java) ?: continue
                                if (diaHorario != horario.dia) continue

                                val horasHorario = hChild.child("horas").children
                                    .mapNotNull { it.getValue(Long::class.java) }
                                if (!horasHorario.any { horario.horas.contains(it) }) continue

                                val idCursoExistente = hChild.child("id_curso").getValue(Long::class.java) ?: continue
                                if (idCursoExistente == horario.id_curso) continue // MISMO CURSO: se permite

                                cursosConflicto.add(idCursoExistente)
                            }

                            fun guardarNuevoHorario() {
                                refHorarios.orderByChild("id_horario").limitToLast(1).get()
                                    .addOnSuccessListener { snapshot ->
                                        val maxId = snapshot.children.mapNotNull {
                                            it.child("id_horario").getValue(Long::class.java)
                                        }.maxOrNull() ?: 0L
                                        val siguienteId = (maxId + 1).toString()
                                        val nuevoGrupo = cursoSel.grupos + 1
                                        val horarioAGuardar = horario.copy(
                                            id_horario = siguienteId.toLong(),
                                            grupo = nuevoGrupo.toLong()
                                        )

                                        refUsuarios.orderByChild("usuario_id").equalTo(idUsuario.toDouble()).limitToFirst(1)
                                            .get()
                                            .addOnSuccessListener { usuarioSnap ->
                                                val userSnap = usuarioSnap.children.firstOrNull()
                                                if (userSnap == null) {
                                                    onResult(false, "Usuario no encontrado")
                                                    return@addOnSuccessListener
                                                }
                                                val usuarioObj = userSnap.getValue(Usuario::class.java)
                                                if (usuarioObj == null) {
                                                    onResult(false, "Error al convertir usuario")
                                                    return@addOnSuccessListener
                                                }

                                                val datosUsuario = mapOf(
                                                    "usuario_id" to usuarioObj.usuario_id,
                                                    "u_tipo" to usuarioObj.u_tipo,
                                                    "u_nombres" to usuarioObj.u_nombres,
                                                    "u_apellidos" to usuarioObj.u_apellidos,
                                                    "cu" to usuarioObj.cu,
                                                    "u_contrasena" to usuarioObj.u_contrasena,
                                                    "u_nrotelefonico" to usuarioObj.u_nrotelefonico,
                                                    "notificacion" to usuarioObj.notificacion
                                                )

                                                val horarioUsuarioData = mapOf(
                                                    "horario" to horarioAGuardar,
                                                    "periodo" to "2025-1",
                                                    "semana" to 0,
                                                    "usuario" to datosUsuario
                                                )

                                                val updates = hashMapOf<String, Any>(
                                                    "horario/$siguienteId" to horarioAGuardar,
                                                    "horario_usuario/$idUsuario/$siguienteId" to horarioUsuarioData,
                                                    "curso/${cursoSnap.key}/grupos" to nuevoGrupo
                                                )

                                                databaseRef.updateChildren(updates).addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        onResult(true, "Horario guardado")
                                                    } else {
                                                        onResult(false, "Error al guardar")
                                                    }
                                                }
                                            }.addOnFailureListener {
                                                onResult(false, "Error al recuperar datos del usuario")
                                            }
                                    }.addOnFailureListener {
                                        onResult(false, "Error al generar nuevo id_horario")
                                    }
                            }

                            if (cursosConflicto.isNotEmpty()) {
                                refCursos.get().addOnSuccessListener { cursosSnap ->
                                    val conflictoCiclo = cursosSnap.children.any { cChild ->
                                        val idCurso = cChild.child("id_curso").getValue(Long::class.java)
                                        val cicloCurso = cChild.child("ciclo").getValue(Int::class.java)
                                        idCurso != null && cicloCurso != null &&
                                                cicloCurso == targetCycle && cursosConflicto.contains(idCurso)
                                    }

                                    if (conflictoCiclo) {
                                        onResult(false, "Existe cruce con otro curso del mismo ciclo o en el horario del docente")
                                    } else {
                                        guardarNuevoHorario()
                                    }
                                }.addOnFailureListener {
                                    onResult(false, "Error al verificar cursos")
                                }
                            } else {
                                guardarNuevoHorario()
                            }
                        }.addOnFailureListener {
                            onResult(false, "Error al verificar horarios globales")
                        }
                    }.addOnFailureListener {
                        onResult(false, "Error al verificar horario del docente")
                    }
            }.addOnFailureListener {
                onResult(false, "Error al obtener curso")
            }
    }




    /*fun guardarHorarioCompleto(idUsuario: Int, horario: Horario, onResult: (Boolean, String) -> Unit) {
        // Obtener el curso seleccionado y su referencia
        refCursos.orderByChild("id_curso").equalTo(horario.id_curso.toDouble()).get()
            .addOnSuccessListener { courseSnapshot ->
                if (!courseSnapshot.exists()) {
                    onResult(false, "Curso no encontrado")
                    return@addOnSuccessListener
                }
                val cursoSnap = courseSnapshot.children.firstOrNull()
                val cursoSel = cursoSnap?.getValue(Curso::class.java)
                if (cursoSnap == null || cursoSel == null) {
                    onResult(false, "Curso no encontrado")
                    return@addOnSuccessListener
                }
                val targetCycle = cursoSel.ciclo

                // Verificar conflictos en horarios del docente (mismo día y horas superpuestas)
                refHorarioUsuario.child(idUsuario.toString()).get()
                    .addOnSuccessListener { userHorSnapshot ->
                        var conflictoDocente = false
                        for (child in userHorSnapshot.children) {
                            val diaExistente = child.child("horario/dia").getValue(String::class.java) ?: ""
                            if (diaExistente == horario.dia) {
                                val horasExistentes = child.child("horario/horas").children
                                    .mapNotNull { it.getValue(Long::class.java) }
                                if (horasExistentes.any { horario.horas.contains(it) }) {
                                    conflictoDocente = true
                                    break
                                }
                            }
                        }
                        if (conflictoDocente) {
                            onResult(false, "Existe cruce con otro curso del mismo ciclo o en el horario del docente")
                            return@addOnSuccessListener
                        }

                        // Verificar conflictos con cursos del mismo ciclo a nivel global (mismo día y hora)
                        refHorarios.get().addOnSuccessListener { horariosSnap ->
                            val cursosConflicto = mutableSetOf<Long>()
                            for (hChild in horariosSnap.children) {
                                val diaHorario = hChild.child("dia").getValue(String::class.java) ?: ""
                                if (diaHorario != horario.dia) continue
                                val horasHorario = hChild.child("horas").children
                                    .mapNotNull { it.getValue(Long::class.java) }
                                if (!horasHorario.any { horario.horas.contains(it) }) continue
                                val idCursoExistente = hChild.child("id_curso")
                                    .getValue(Long::class.java) ?: 0L
                                if (idCursoExistente == horario.id_curso) continue // Mismo curso
                                cursosConflicto.add(idCursoExistente)
                            }

                            // Función para guardar el nuevo horario
                            fun guardarNuevoHorario() {
                                // Paso 1: obtener el siguiente id_horario incremental
                                refHorarios.orderByChild("id_horario").limitToLast(1).get()
                                    .addOnSuccessListener { snapshot ->
                                        val maxId = snapshot.children.mapNotNull {
                                            it.child("id_horario").getValue(Long::class.java)
                                        }.maxOrNull() ?: 0L
                                        val siguienteId = (maxId + 1).toString()

                                        val nuevoGrupo = cursoSel.grupos + 1
                                        val horarioAGuardar = horario.copy(
                                            id_horario = siguienteId.toLong(),
                                            grupo = nuevoGrupo.toLong()
                                        )


                                        // 🔄 Asegúrate que refUsuarios apunte a la ruta raíz "usuario"
                                        refUsuarios.orderByChild("usuario_id").equalTo(idUsuario.toDouble()).limitToFirst(1)
                                            .get()
                                            .addOnSuccessListener { usuarioSnap ->
                                                Log.d("DEBUG", "Snapshot encontrado: ${usuarioSnap.exists()} con ${usuarioSnap.childrenCount} hijos")
                                                val userSnap = usuarioSnap.children.firstOrNull()
                                                if (userSnap == null) {
                                                    onResult(false, "Usuario no encontrado")
                                                    return@addOnSuccessListener
                                                }

                                                val usuarioObj = userSnap.getValue(Usuario::class.java)
                                                if (usuarioObj == null) {
                                                    onResult(false, "Error al convertir usuario")
                                                    return@addOnSuccessListener
                                                }

                                                val datosUsuario = mapOf(
                                                    "usuario_id" to usuarioObj.usuario_id,
                                                    "u_tipo" to usuarioObj.u_tipo,
                                                    "u_nombres" to usuarioObj.u_nombres,
                                                    "u_apellidos" to usuarioObj.u_apellidos,
                                                    "cu" to usuarioObj.cu,
                                                    "u_contrasena" to usuarioObj.u_contrasena,
                                                    "u_nrotelefonico" to usuarioObj.u_nrotelefonico,
                                                    "notificacion" to usuarioObj.notificacion
                                                )

                                                val horarioUsuarioData = mapOf(
                                                    "horario" to horarioAGuardar,
                                                    "periodo" to "2025-1",
                                                    "semana" to 0,
                                                    "usuario" to datosUsuario
                                                )

                                                val updates = hashMapOf<String, Any>(
                                                    "horario/$siguienteId" to horarioAGuardar,
                                                    "horario_usuario/$idUsuario/$siguienteId" to horarioUsuarioData,
                                                    "curso/${cursoSnap.key}/grupos" to nuevoGrupo
                                                )

                                                databaseRef.updateChildren(updates).addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        onResult(true, "Horario guardado")
                                                    } else {
                                                        onResult(false, "Error al guardar")
                                                    }
                                                }
                                            }.addOnFailureListener {
                                                Log.e("ERROR", "Fallo al recuperar usuario", it)
                                                onResult(false, "Error al recuperar datos del usuario")
                                            }

                                    }.addOnFailureListener {
                                        Log.e("ERROR", "Fallo al obtener nuevo ID de horario", it)
                                        onResult(false, "Error al generar nuevo id_horario")
                                    }
                            }



                            if (cursosConflicto.isNotEmpty()) {
                                // Verificar si alguno pertenece al mismo ciclo
                                refCursos.get().addOnSuccessListener { cursosSnap ->
                                    var conflictoCiclo = false
                                    for (cChild in cursosSnap.children) {
                                        val idCurso = cChild.child("id_curso").getValue(Long::class.java)
                                        val cicloCurso = cChild.child("ciclo").getValue(Int::class.java)
                                        if (idCurso != null && cicloCurso != null
                                            && cicloCurso == targetCycle
                                            && cursosConflicto.contains(idCurso.toLong())) {
                                            conflictoCiclo = true
                                            break
                                        }
                                    }
                                    if (conflictoCiclo) {
                                        onResult(false, "Existe cruce con otro curso del mismo ciclo o en el horario del docente")
                                    } else {
                                        guardarNuevoHorario()
                                    }
                                }.addOnFailureListener { e ->
                                    handleException(e, "Error al verificar cursos")
                                    onResult(false, "Error al verificar cursos")
                                }
                            } else {
                                guardarNuevoHorario()
                            }
                        }.addOnFailureListener { e ->
                            handleException(e, "Error al verificar horarios globales")
                            onResult(false, "Error al verificar horarios globales")
                        }
                    }.addOnFailureListener { e ->
                        handleException(e, "Error al verificar horario del docente")
                        onResult(false, "Error al verificar horario del docente")
                    }
            }.addOnFailureListener { e ->
                handleException(e, "Error al obtener curso")
                onResult(false, "Error al obtener curso")
            }
    }*/

    private fun generarAulasFacultad(): List<String> {
        val aulas = mutableListOf<String>()
        listOf("", "-NP").forEach { sufijo ->
            listOf(
                1 to 14,  // 101-114
                2 to 14,  // 201-214
                3 to 14   // 301-314
            ).forEach { (piso, maxAula) ->
                for (num in 1..maxAula) {
                    val numeroAula = piso * 100 + num
                    aulas.add("$numeroAula$sufijo")
                }
            }
        }
        return aulas
    }

    fun obtenerHorariosPorUsuario(idUsuario: Int, onResult: (List<Horario>) -> Unit) {
        refHorarioUsuario.child(idUsuario.toString())
            .get().addOnSuccessListener { snapshot ->
                val lista = snapshot.children.mapNotNull { it.child("horario").getValue(Horario::class.java) }
                onResult(lista)
            }.addOnFailureListener {
                onResult(emptyList())
            }
    }


    fun cargarCursos() {
        refCursos.get().addOnSuccessListener { snapshot ->
            cursos.value = snapshot.children.mapNotNull { it.getValue(Curso::class.java) }
        }.addOnFailureListener {
            cursos.value = emptyList()
        }
    }
    /*fun obtenerNombreCurso(idCurso: Long): String {
        return cursos.value.find { it.id_curso == idCurso }?.nombre ?: "Curso $idCurso"
    }*/

    fun obtenerNombreCursoPorId(idCurso: Long, callback: (String) -> Unit) {
        refCursos.orderByChild("id_curso").equalTo(idCurso.toDouble()).get()
            .addOnSuccessListener { snapshot ->
                val nombre = snapshot.children.firstOrNull()
                    ?.child("nombre")?.getValue(String::class.java) ?: "Curso $idCurso"
                callback(nombre)
            }
            .addOnFailureListener {
                callback("Curso $idCurso")
            }
    }





    /**
     * Maneja excepciones de Firebase u otras operaciones, registrando el mensaje de error y actualizando [popupNotification] con un mensaje para la IU.
     * Si se proporciona [customMessage], se mostrará junto con el mensaje de error para mayor claridad.
     */
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        if (message.isNotEmpty()) {
            Log.d(TAG, "PopupNotification: $message")
        }
    }
}