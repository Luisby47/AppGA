package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Grupo
import com.example.appga.model.Profesor
import com.example.appga.model.Resource
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InstructorViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _instructorsState = MutableStateFlow<Resource<List<Profesor>>>(Resource.Loading)
    val instructorsState: StateFlow<Resource<List<Profesor>>> = _instructorsState
    
    private val _instructorState = MutableStateFlow<Resource<Profesor>>(Resource.Loading)
    val instructorState: StateFlow<Resource<Profesor>> = _instructorState
    
    private val _instructorGroupsState = MutableStateFlow<Resource<List<Grupo>>>(Resource.Loading)
    val instructorGroupsState: StateFlow<Resource<List<Grupo>>> = _instructorGroupsState
    
    private val _filteredInstructors = MutableStateFlow<List<Profesor>>(emptyList())
    val filteredInstructors: StateFlow<List<Profesor>> = _filteredInstructors
    
    private var allInstructors: List<Profesor> = emptyList()
    
    init {
        loadAllInstructors()
    }
    
    fun loadAllInstructors() {
        viewModelScope.launch {
            _instructorsState.value = Resource.Loading
            
            try {
                val result = apiService.getAllProfesores()
                
                result.fold(
                    onSuccess = { instructors ->
                        allInstructors = instructors
                        _filteredInstructors.value = instructors
                        _instructorsState.value = Resource.Success(instructors)
                    },
                    onFailure = { e ->
                        _instructorsState.value = Resource.Error(e.message ?: "Error al cargar profesores")
                    }
                )
            } catch (e: Exception) {
                _instructorsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getInstructorById(id: Int) {
        viewModelScope.launch {
            _instructorState.value = Resource.Loading
            
            try {
                val result = apiService.getProfesorById(id)
                
                result.fold(
                    onSuccess = { instructor ->
                        _instructorState.value = Resource.Success(instructor)
                        // Load instructor's groups if necessary
                        loadInstructorGroups(instructor.cedula)
                    },
                    onFailure = { e ->
                        _instructorState.value = Resource.Error(e.message ?: "Error al cargar profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getInstructorByCedula(cedula: String) {
        viewModelScope.launch {
            _instructorState.value = Resource.Loading
            
            try {
                val result = apiService.getProfesorByCedula(cedula)
                
                result.fold(
                    onSuccess = { instructor ->
                        _instructorState.value = Resource.Success(instructor)
                        // Load instructor's groups if necessary
                        loadInstructorGroups(instructor.cedula)
                    },
                    onFailure = { e ->
                        _instructorState.value = Resource.Error(e.message ?: "Error al cargar profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun loadInstructorGroups(cedula: String) {
        viewModelScope.launch {
            _instructorGroupsState.value = Resource.Loading
            
            try {
                val result = apiService.getGruposByProfesor(cedula)
                
                result.fold(
                    onSuccess = { groups ->
                        _instructorGroupsState.value = Resource.Success(groups)
                    },
                    onFailure = { e ->
                        _instructorGroupsState.value = Resource.Error(e.message ?: "Error al cargar grupos del profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorGroupsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun createInstructor(instructor: Profesor) {
        viewModelScope.launch {
            _instructorState.value = Resource.Loading
            
            try {
                val result = apiService.createProfesor(instructor)
                
                result.fold(
                    onSuccess = { newInstructor ->
                        _instructorState.value = Resource.Success(newInstructor)
                        loadAllInstructors() // Refresh instructor list
                    },
                    onFailure = { e ->
                        _instructorState.value = Resource.Error(e.message ?: "Error al crear profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateInstructor(id: Int, instructor: Profesor) {
        viewModelScope.launch {
            _instructorState.value = Resource.Loading
            
            try {
                val result = apiService.updateProfesor(id, instructor)
                
                result.fold(
                    onSuccess = { updatedInstructor ->
                        _instructorState.value = Resource.Success(updatedInstructor)
                        loadAllInstructors() // Refresh instructor list
                    },
                    onFailure = { e ->
                        _instructorState.value = Resource.Error(e.message ?: "Error al actualizar profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteInstructor(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteProfesor(id)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            loadAllInstructors() // Refresh instructor list
                        } else {
                            _instructorsState.value = Resource.Error("Error al eliminar profesor")
                        }
                    },
                    onFailure = { e ->
                        _instructorsState.value = Resource.Error(e.message ?: "Error al eliminar profesor")
                    }
                )
            } catch (e: Exception) {
                _instructorsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun filterInstructors(query: String, filterType: InstructorFilterType) {
        if (query.isBlank()) {
            _filteredInstructors.value = allInstructors
            return
        }
        
        val filteredList = when (filterType) {
            InstructorFilterType.NAME -> allInstructors.filter { it.nombre.contains(query, ignoreCase = true) }
            InstructorFilterType.ID -> allInstructors.filter { it.cedula.contains(query, ignoreCase = true) }
        }
        
        _filteredInstructors.value = filteredList
    }
    
    fun resetInstructorState() {
        _instructorState.value = Resource.Loading
        _instructorGroupsState.value = Resource.Loading
    }
}

enum class InstructorFilterType {
    NAME, ID
}
