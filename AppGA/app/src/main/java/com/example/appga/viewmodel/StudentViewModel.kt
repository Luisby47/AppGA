package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.*
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _studentsState = MutableStateFlow<Resource<List<Alumno>>>(Resource.Loading)
    val studentsState: StateFlow<Resource<List<Alumno>>> = _studentsState
    
    private val _studentState = MutableStateFlow<Resource<Alumno>>(Resource.Loading)
    val studentState: StateFlow<Resource<Alumno>> = _studentState
    
    private val _studentEnrollmentsState = MutableStateFlow<Resource<List<MatriculaWithDetails>>>(Resource.Loading)
    val studentEnrollmentsState: StateFlow<Resource<List<MatriculaWithDetails>>> = _studentEnrollmentsState
    
    private val _studentWithDetailsState = MutableStateFlow<Resource<AlumnoWithEnrollment>>(Resource.Loading)
    val studentWithDetailsState: StateFlow<Resource<AlumnoWithEnrollment>> = _studentWithDetailsState
    
    private val _filteredStudents = MutableStateFlow<List<Alumno>>(emptyList())
    val filteredStudents: StateFlow<List<Alumno>> = _filteredStudents
    
    private var allStudents: List<Alumno> = emptyList()
    
    init {
        loadAllStudents()
    }
    
    fun loadAllStudents() {
        viewModelScope.launch {
            _studentsState.value = Resource.Loading
            
            try {
                val result = apiService.getAllAlumnos()
                
                result.fold(
                    onSuccess = { students ->
                        allStudents = students
                        _filteredStudents.value = students
                        _studentsState.value = Resource.Success(students)
                    },
                    onFailure = { e ->
                        _studentsState.value = Resource.Error(e.message ?: "Error al cargar estudiantes")
                    }
                )
            } catch (e: Exception) {
                _studentsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getStudentById(id: Int) {
        viewModelScope.launch {
            _studentState.value = Resource.Loading
            
            try {
                val result = apiService.getAlumnoById(id)
                
                result.fold(
                    onSuccess = { student ->
                        _studentState.value = Resource.Success(student)
                        // Load student's enrollments
                        loadStudentEnrollments(id)
                    },
                    onFailure = { e ->
                        _studentState.value = Resource.Error(e.message ?: "Error al cargar estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getStudentByCedula(cedula: String) {
        viewModelScope.launch {
            _studentState.value = Resource.Loading
            
            try {
                val result = apiService.getAlumnoByCedula(cedula)
                
                result.fold(
                    onSuccess = { student ->
                        _studentState.value = Resource.Success(student)
                        // Load student's enrollments if we have an ID
                        student.id?.let { loadStudentEnrollments(it) }
                    },
                    onFailure = { e ->
                        _studentState.value = Resource.Error(e.message ?: "Error al cargar estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun loadStudentEnrollments(studentId: Int) {
        viewModelScope.launch {
            _studentEnrollmentsState.value = Resource.Loading
            
            try {
                val enrollmentsResult = apiService.getMatriculasByAlumno(studentId)
                
                enrollmentsResult.fold(
                    onSuccess = { enrollments ->
                        val enrollmentsWithDetails = mutableListOf<MatriculaWithDetails>()
                        
                        for (enrollment in enrollments) {
                            // Get grupo for this enrollment
                            val grupoResult = apiService.getGrupoById(enrollment.grupoId)
                            
                            grupoResult.fold(
                                onSuccess = { grupo ->
                                    // Get curso for this grupo
                                    val cursoResult = apiService.getCursoByCodigo(grupo.codigoCurso)
                                    
                                    cursoResult.fold(
                                        onSuccess = { curso ->
                                            // Get ciclo for this grupo
                                            val cicloResult = apiService.getCiclosByAnio(grupo.anio)
                                            
                                            var ciclo: Ciclo? = null
                                            cicloResult.fold(
                                                onSuccess = { ciclos ->
                                                    ciclo = ciclos.find { it.numero == grupo.numeroCiclo }
                                                },
                                                onFailure = { /* Ignore error, we can proceed without ciclo */ }
                                            )
                                            
                                            // Add to the list with all details
                                            enrollmentsWithDetails.add(
                                                MatriculaWithDetails(
                                                    matricula = enrollment,
                                                    curso = curso,
                                                    grupo = grupo,
                                                    ciclo = ciclo
                                                )
                                            )
                                        },
                                        onFailure = { /* Skip this enrollment if we can't get curso */ }
                                    )
                                },
                                onFailure = { /* Skip this enrollment if we can't get grupo */ }
                            )
                        }
                        
                        _studentEnrollmentsState.value = Resource.Success(enrollmentsWithDetails)
                        
                        // If we have student info, also update the combined state
                        val studentState = _studentState.value
                        if (studentState is Resource.Success) {
                            val student = studentState.data
                            
                            // If student has a carrera, get its name
                            var carreraNombre: String? = null
                            student.codigoCarrera?.let { codigoCarrera ->
                                val carreraResult = apiService.getCarreraByCodigo(codigoCarrera)
                                carreraResult.fold(
                                    onSuccess = { carrera -> carreraNombre = carrera.nombre },
                                    onFailure = { /* Ignore error, we can proceed without carrera name */ }
                                )
                            }
                            
                            _studentWithDetailsState.value = Resource.Success(
                                AlumnoWithEnrollment(
                                    alumno = student,
                                    carreraNombre = carreraNombre,
                                    matriculas = enrollmentsWithDetails
                                )
                            )
                        }
                    },
                    onFailure = { e ->
                        _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error al cargar matrículas del estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun createStudent(student: Alumno) {
        viewModelScope.launch {
            _studentState.value = Resource.Loading
            
            try {
                val result = apiService.createAlumno(student)
                
                result.fold(
                    onSuccess = { newStudent ->
                        _studentState.value = Resource.Success(newStudent)
                        loadAllStudents() // Refresh student list
                    },
                    onFailure = { e ->
                        _studentState.value = Resource.Error(e.message ?: "Error al crear estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateStudent(id: Int, student: Alumno) {
        viewModelScope.launch {
            _studentState.value = Resource.Loading
            
            try {
                val result = apiService.updateAlumno(id, student)
                
                result.fold(
                    onSuccess = { updatedStudent ->
                        _studentState.value = Resource.Success(updatedStudent)
                        loadAllStudents() // Refresh student list
                    },
                    onFailure = { e ->
                        _studentState.value = Resource.Error(e.message ?: "Error al actualizar estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteStudent(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteAlumno(id)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            loadAllStudents() // Refresh student list
                        } else {
                            _studentsState.value = Resource.Error("Error al eliminar estudiante")
                        }
                    },
                    onFailure = { e ->
                        _studentsState.value = Resource.Error(e.message ?: "Error al eliminar estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun enrollStudentInGroup(studentId: Int, groupId: Int) {
        viewModelScope.launch {
            try {
                val matricula = Matricula(
                    alumnoId = studentId,
                    grupoId = groupId
                )
                
                val result = apiService.createMatricula(matricula)
                
                result.fold(
                    onSuccess = { newMatricula ->
                        // Refresh student enrollments
                        loadStudentEnrollments(studentId)
                    },
                    onFailure = { e ->
                        _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error al matricular estudiante")
                    }
                )
            } catch (e: Exception) {
                _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun removeEnrollment(enrollmentId: Int, studentId: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteMatricula(enrollmentId)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            // Refresh student enrollments
                            loadStudentEnrollments(studentId)
                        } else {
                            _studentEnrollmentsState.value = Resource.Error("Error al eliminar matrícula")
                        }
                    },
                    onFailure = { e ->
                        _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error al eliminar matrícula")
                    }
                )
            } catch (e: Exception) {
                _studentEnrollmentsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun filterStudents(query: String, filterType: StudentFilterType) {
        if (query.isBlank()) {
            _filteredStudents.value = allStudents
            return
        }
        
        val filteredList = when (filterType) {
            StudentFilterType.NAME -> allStudents.filter { it.nombre.contains(query, ignoreCase = true) }
            StudentFilterType.ID -> allStudents.filter { it.cedula.contains(query, ignoreCase = true) }
            StudentFilterType.MAJOR -> allStudents.filter { 
                it.codigoCarrera != null && it.codigoCarrera.contains(query, ignoreCase = true) 
            }
        }
        
        _filteredStudents.value = filteredList
    }
    
    fun resetStudentState() {
        _studentState.value = Resource.Loading
        _studentEnrollmentsState.value = Resource.Loading
        _studentWithDetailsState.value = Resource.Loading
    }
}

enum class StudentFilterType {
    NAME, ID, MAJOR
}
