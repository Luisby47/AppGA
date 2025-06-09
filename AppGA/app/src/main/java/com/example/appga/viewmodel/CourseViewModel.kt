package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Curso
import com.example.appga.model.Resource
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _coursesState = MutableStateFlow<Resource<List<Curso>>>(Resource.Loading)
    val coursesState: StateFlow<Resource<List<Curso>>> = _coursesState
    
    private val _courseState = MutableStateFlow<Resource<Curso>>(Resource.Loading)
    val courseState: StateFlow<Resource<Curso>> = _courseState
    
    private val _filteredCourses = MutableStateFlow<List<Curso>>(emptyList())
    val filteredCourses: StateFlow<List<Curso>> = _filteredCourses
    
    private var allCourses: List<Curso> = emptyList()
    
    init {
        loadAllCourses()
    }
    
    fun loadAllCourses() {
        viewModelScope.launch {
            _coursesState.value = Resource.Loading
            
            try {
                val result = apiService.getAllCursos()
                
                result.fold(
                    onSuccess = { courses ->
                        allCourses = courses
                        _filteredCourses.value = courses
                        _coursesState.value = Resource.Success(courses)
                    },
                    onFailure = { e ->
                        _coursesState.value = Resource.Error(e.message ?: "Error al cargar cursos")
                    }
                )
            } catch (e: Exception) {
                _coursesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getCourseById(id: Int) {
        viewModelScope.launch {
            _courseState.value = Resource.Loading
            
            try {
                val result = apiService.getCursoById(id)
                
                result.fold(
                    onSuccess = { course ->
                        _courseState.value = Resource.Success(course)
                    },
                    onFailure = { e ->
                        _courseState.value = Resource.Error(e.message ?: "Error al cargar curso")
                    }
                )
            } catch (e: Exception) {
                _courseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getCourseByCodigo(codigo: String) {
        viewModelScope.launch {
            _courseState.value = Resource.Loading
            
            try {
                val result = apiService.getCursoByCodigo(codigo)
                
                result.fold(
                    onSuccess = { course ->
                        _courseState.value = Resource.Success(course)
                    },
                    onFailure = { e ->
                        _courseState.value = Resource.Error(e.message ?: "Error al cargar curso")
                    }
                )
            } catch (e: Exception) {
                _courseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun createCourse(curso: Curso) {
        viewModelScope.launch {
            _courseState.value = Resource.Loading
            
            try {
                val result = apiService.createCurso(curso)
                
                result.fold(
                    onSuccess = { newCourse ->
                        _courseState.value = Resource.Success(newCourse)
                        loadAllCourses() // Refresh course list
                    },
                    onFailure = { e ->
                        _courseState.value = Resource.Error(e.message ?: "Error al crear curso")
                    }
                )
            } catch (e: Exception) {
                _courseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateCourse(id: Int, curso: Curso) {
        viewModelScope.launch {
            _courseState.value = Resource.Loading
            
            try {
                val result = apiService.updateCurso(id, curso)
                
                result.fold(
                    onSuccess = { updatedCourse ->
                        _courseState.value = Resource.Success(updatedCourse)
                        loadAllCourses() // Refresh course list
                    },
                    onFailure = { e ->
                        _courseState.value = Resource.Error(e.message ?: "Error al actualizar curso")
                    }
                )
            } catch (e: Exception) {
                _courseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteCourse(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteCurso(id)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            loadAllCourses() // Refresh course list
                        } else {
                            _coursesState.value = Resource.Error("Error al eliminar curso")
                        }
                    },
                    onFailure = { e ->
                        _coursesState.value = Resource.Error(e.message ?: "Error al eliminar curso")
                    }
                )
            } catch (e: Exception) {
                _coursesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun filterCourses(query: String, filterType: CourseFilterType) {
        if (query.isBlank()) {
            _filteredCourses.value = allCourses
            return
        }
        
        val filteredList = when (filterType) {
            CourseFilterType.NAME -> allCourses.filter { it.nombre.contains(query, ignoreCase = true) }
            CourseFilterType.ID -> allCourses.filter { it.codigo.contains(query, ignoreCase = true) }
            CourseFilterType.MAJOR -> {
                // This would require additional API call to filter by major, for now we'll just return all
                allCourses
            }
        }
        
        _filteredCourses.value = filteredList
    }
    
    fun resetCourseState() {
        _courseState.value = Resource.Loading
    }
}

enum class CourseFilterType {
    NAME, ID, MAJOR
}
