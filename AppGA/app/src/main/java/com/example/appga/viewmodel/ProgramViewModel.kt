package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.*
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgramViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _programsState = MutableStateFlow<Resource<List<Carrera>>>(Resource.Loading)
    val programsState: StateFlow<Resource<List<Carrera>>> = _programsState
    
    private val _programState = MutableStateFlow<Resource<Carrera>>(Resource.Loading)
    val programState: StateFlow<Resource<Carrera>> = _programState
    
    private val _programCoursesState = MutableStateFlow<Resource<List<CarreraCurso>>>(Resource.Loading)
    val programCoursesState: StateFlow<Resource<List<CarreraCurso>>> = _programCoursesState
    
    private val _programWithCoursesState = MutableStateFlow<Resource<CarreraWithCursos>>(Resource.Loading)
    val programWithCoursesState: StateFlow<Resource<CarreraWithCursos>> = _programWithCoursesState
    
    private val _filteredPrograms = MutableStateFlow<List<Carrera>>(emptyList())
    val filteredPrograms: StateFlow<List<Carrera>> = _filteredPrograms
    
    private var allPrograms: List<Carrera> = emptyList()
    
    init {
        loadAllPrograms()
    }
    
    fun loadAllPrograms() {
        viewModelScope.launch {
            _programsState.value = Resource.Loading
            
            try {
                val result = apiService.getAllCarreras()
                
                result.fold(
                    onSuccess = { programs ->
                        allPrograms = programs
                        _filteredPrograms.value = programs
                        _programsState.value = Resource.Success(programs)
                    },
                    onFailure = { e ->
                        _programsState.value = Resource.Error(e.message ?: "Error al cargar programas")
                    }
                )
            } catch (e: Exception) {
                _programsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getProgramById(id: Int) {
        viewModelScope.launch {
            _programState.value = Resource.Loading
            
            try {
                val result = apiService.getCarreraById(id)
                
                result.fold(
                    onSuccess = { program ->
                        _programState.value = Resource.Success(program)
                        loadProgramCourses(program.codigo)
                    },
                    onFailure = { e ->
                        _programState.value = Resource.Error(e.message ?: "Error al cargar programa")
                    }
                )
            } catch (e: Exception) {
                _programState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getProgramByCodigo(codigo: String) {
        viewModelScope.launch {
            _programState.value = Resource.Loading
            
            try {
                val result = apiService.getCarreraByCodigo(codigo)
                
                result.fold(
                    onSuccess = { program ->
                        _programState.value = Resource.Success(program)
                        loadProgramCourses(program.codigo)
                    },
                    onFailure = { e ->
                        _programState.value = Resource.Error(e.message ?: "Error al cargar programa")
                    }
                )
            } catch (e: Exception) {
                _programState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun loadProgramCourses(programCode: String) {
        viewModelScope.launch {
            _programCoursesState.value = Resource.Loading
            
            try {
                val result = apiService.getCarreraCursosByCarrera(programCode)
                
                result.fold(
                    onSuccess = { programCourses ->
                        _programCoursesState.value = Resource.Success(programCourses)
                        loadProgramWithCourses(programCode, programCourses)
                    },
                    onFailure = { e ->
                        _programCoursesState.value = Resource.Error(e.message ?: "Error al cargar cursos del programa")
                    }
                )
            } catch (e: Exception) {
                _programCoursesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    private suspend fun loadProgramWithCourses(programCode: String, programCourses: List<CarreraCurso>) {
        _programWithCoursesState.value = Resource.Loading
        
        try {
            val programResult = apiService.getCarreraByCodigo(programCode)
            
            programResult.fold(
                onSuccess = { program ->
                    val coursesWithOrder = mutableListOf<CursoWithOrder>()
                    
                    for (programCourse in programCourses) {
                        val courseResult = apiService.getCursoByCodigo(programCourse.codigoCurso)
                        
                        courseResult.fold(
                            onSuccess = { course ->
                                coursesWithOrder.add(CursoWithOrder(course, programCourse.orden))
                            },
                            onFailure = { /* Skip this course */ }
                        )
                    }
                    
                    // Sort courses by order
                    val sortedCourses = coursesWithOrder.sortedBy { it.orden }
                    
                    val programWithCourses = CarreraWithCursos(program, sortedCourses)
                    _programWithCoursesState.value = Resource.Success(programWithCourses)
                },
                onFailure = { e ->
                    _programWithCoursesState.value = Resource.Error(e.message ?: "Error al cargar programa con cursos")
                }
            )
        } catch (e: Exception) {
            _programWithCoursesState.value = Resource.Error(e.message ?: "Error desconocido")
        }
    }
    
    fun createProgram(program: Carrera) {
        viewModelScope.launch {
            _programState.value = Resource.Loading
            
            try {
                val result = apiService.createCarrera(program)
                
                result.fold(
                    onSuccess = { newProgram ->
                        _programState.value = Resource.Success(newProgram)
                        loadAllPrograms() // Refresh program list
                    },
                    onFailure = { e ->
                        _programState.value = Resource.Error(e.message ?: "Error al crear programa")
                    }
                )
            } catch (e: Exception) {
                _programState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateProgram(id: Int, program: Carrera) {
        viewModelScope.launch {
            _programState.value = Resource.Loading
            
            try {
                val result = apiService.updateCarrera(id, program)
                
                result.fold(
                    onSuccess = { updatedProgram ->
                        _programState.value = Resource.Success(updatedProgram)
                        loadAllPrograms() // Refresh program list
                    },
                    onFailure = { e ->
                        _programState.value = Resource.Error(e.message ?: "Error al actualizar programa")
                    }
                )
            } catch (e: Exception) {
                _programState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteProgram(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteCarrera(id)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            loadAllPrograms() // Refresh program list
                        } else {
                            _programsState.value = Resource.Error("Error al eliminar programa")
                        }
                    },
                    onFailure = { e ->
                        _programsState.value = Resource.Error(e.message ?: "Error al eliminar programa")
                    }
                )
            } catch (e: Exception) {
                _programsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun addCourseToProgramAndReorder(programCode: String, courseCode: String, newPosition: Int) {
        viewModelScope.launch {
            try {
                // Get existing courses for the program
                val existingCoursesResult = apiService.getCarreraCursosByCarrera(programCode)
                
                existingCoursesResult.fold(
                    onSuccess = { existingCourses ->
                        // First, check if the course is already in the program
                        val existingCourse = existingCourses.find { it.codigoCurso == courseCode }
                        
                        if (existingCourse != null) {
                            // Course exists, update its order
                            val updatedCourse = existingCourse.copy(orden = newPosition)
                            apiService.updateCarreraCurso(existingCourse.id!!, updatedCourse)
                        } else {
                            // Course does not exist, add it
                            val newProgramCourse = CarreraCurso(
                                codigoCarrera = programCode,
                                codigoCurso = courseCode,
                                orden = newPosition
                            )
                            apiService.addCursoToCarrera(newProgramCourse)
                        }
                        
                        // Update orders of other courses
                        existingCourses
                            .filter { it.codigoCurso != courseCode && it.orden >= newPosition }
                            .forEach { course ->
                                val updatedCourse = course.copy(orden = course.orden + 1)
                                apiService.updateCarreraCurso(course.id!!, updatedCourse)
                            }
                        
                        // Reload program courses
                        loadProgramCourses(programCode)
                    },
                    onFailure = { e ->
                        _programCoursesState.value = Resource.Error(e.message ?: "Error al actualizar cursos del programa")
                    }
                )
            } catch (e: Exception) {
                _programCoursesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun removeCourseFromProgram(programCourseId: Int, programCode: String) {
        viewModelScope.launch {
            try {
                val result = apiService.removeCursoFromCarrera(programCourseId)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            // Reload program courses
                            loadProgramCourses(programCode)
                        } else {
                            _programCoursesState.value = Resource.Error("Error al eliminar curso del programa")
                        }
                    },
                    onFailure = { e ->
                        _programCoursesState.value = Resource.Error(e.message ?: "Error al eliminar curso del programa")
                    }
                )
            } catch (e: Exception) {
                _programCoursesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun filterPrograms(query: String, filterType: ProgramFilterType) {
        if (query.isBlank()) {
            _filteredPrograms.value = allPrograms
            return
        }
        
        val filteredList = when (filterType) {
            ProgramFilterType.NAME -> allPrograms.filter { it.nombre.contains(query, ignoreCase = true) }
            ProgramFilterType.ID -> allPrograms.filter { it.codigo.contains(query, ignoreCase = true) }
        }
        
        _filteredPrograms.value = filteredList
    }
    
    fun resetProgramState() {
        _programState.value = Resource.Loading
        _programCoursesState.value = Resource.Loading
        _programWithCoursesState.value = Resource.Loading
    }
}

enum class ProgramFilterType {
    NAME, ID
}
