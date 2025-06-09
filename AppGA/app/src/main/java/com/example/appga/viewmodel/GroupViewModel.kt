package com.example.appga.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Grupo
import com.example.appga.model.Resource
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel(
    private val apiService: ApiService = ApiService()
) : ViewModel() {
    
    private val _groupsState = MutableStateFlow<Resource<List<Grupo>>>(Resource.Loading)
    val groupsState: StateFlow<Resource<List<Grupo>>> = _groupsState.asStateFlow()
    
    private val _filteredGroups = mutableStateOf<List<Grupo>>(emptyList())
    val filteredGroups: State<List<Grupo>> = _filteredGroups
    
    private val _currentGroup = mutableStateOf<Resource<Grupo?>>(Resource.Loading)
    val currentGroup: State<Resource<Grupo?>> = _currentGroup
    
    // For the active term
    private var activeTerm: Pair<Int, Int>? = null // (year, term)
    
    fun setActiveTerm(year: Int, term: Int) {
        activeTerm = Pair(year, term)
    }
    
    fun loadAllGroups() {
        _groupsState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                val result = apiService.getAllGrupos()
                if (result.isSuccess) {
                    val groups = result.getOrNull() ?: emptyList()
                    _groupsState.value = Resource.Success(groups)
                    _filteredGroups.value = groups
                } else {
                    _groupsState.value = Resource.Error("Error al cargar los grupos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _groupsState.value = Resource.Error("Error al cargar los grupos: ${e.message}")
            }
        }
    }
    
    fun loadGroupsByCourseId(courseId: Int) {
        loadGroupsByCourse(courseId)
    }
    
    fun loadGroupsByCourse(courseId: Int) {
        _groupsState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                // Handle possible invalid courseId
                if (courseId <= 0) {
                    _groupsState.value = Resource.Success(emptyList())
                    _filteredGroups.value = emptyList()
                    return@launch
                }
                
                val result = apiService.getGruposByCurso(courseId.toString())
                if (result.isSuccess) {
                    val groups = result.getOrNull() ?: emptyList()
                    _groupsState.value = Resource.Success(groups)
                    _filteredGroups.value = groups
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    _groupsState.value = Resource.Success(emptyList()) // Use empty list instead of error to prevent crashes
                    _filteredGroups.value = emptyList()
                    println("Error al cargar los grupos: $errorMsg") // Log error but don't crash
                }
            } catch (e: Exception) {
                // Return empty list instead of error to prevent UI crashes
                _groupsState.value = Resource.Success(emptyList())
                _filteredGroups.value = emptyList()
                println("Error al cargar los grupos: ${e.message}") // Log error but don't crash
            }
        }
    }
    
    fun loadGroupsByInstructor(instructorId: String) {
        _groupsState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                val result = apiService.getGruposByProfesor(instructorId)
                if (result.isSuccess) {
                    val groups = result.getOrNull() ?: emptyList()
                    _groupsState.value = Resource.Success(groups)
                    _filteredGroups.value = groups
                } else {
                    _groupsState.value = Resource.Error("Error al cargar los grupos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _groupsState.value = Resource.Error("Error al cargar los grupos: ${e.message}")
            }
        }
    }
    
    fun loadGroupsByTerm(year: Int, term: Int) {
        _groupsState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                val result = apiService.getGruposByCiclo(year, term.toString())
                if (result.isSuccess) {
                    val groups = result.getOrNull() ?: emptyList()
                    _groupsState.value = Resource.Success(groups)
                    _filteredGroups.value = groups
                } else {
                    _groupsState.value = Resource.Error("Error al cargar los grupos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _groupsState.value = Resource.Error("Error al cargar los grupos: ${e.message}")
            }
        }
    }
    
    fun loadGroupById(groupId: Int) {
        _currentGroup.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                val result = apiService.getGrupoById(groupId)
                if (result.isSuccess) {
                    val group = result.getOrNull()
                    _currentGroup.value = Resource.Success(group)
                } else {
                    _currentGroup.value = Resource.Error("Error al cargar el grupo: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _currentGroup.value = Resource.Error("Error al cargar el grupo: ${e.message}")
            }
        }
    }
    
    fun createGroup(group: Grupo, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.createGrupo(group)
                if (result.isSuccess) {
                    val newGroup = result.getOrNull()
                    if (newGroup != null && newGroup.id != null) {
                        loadAllGroups() // Refresh the list
                        onSuccess(newGroup.id)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateGroup(group: Grupo, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.updateGrupo(group.id ?: 0, group)
                if (result.isSuccess) {
                    loadAllGroups() // Refresh the list
                    onSuccess()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteGroup(groupId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteGrupo(groupId)
                if (result.isSuccess) {
                    loadAllGroups() // Refresh the list
                    onSuccess()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun filterGroups(query: String) {
        if (_groupsState.value is Resource.Success) {
            val allGroups = (_groupsState.value as Resource.Success<List<Grupo>>).data
            
            if (query.isBlank()) {
                _filteredGroups.value = allGroups
            } else {
                _filteredGroups.value = allGroups.filter { grupo ->
                    grupo.codigoCurso.contains(query, ignoreCase = true) ||
                    grupo.numeroGrupo.toString().contains(query, ignoreCase = true) ||
                    grupo.cedulaProfesor?.contains(query, ignoreCase = true) ?: false
                }
            }
        }
    }
}
