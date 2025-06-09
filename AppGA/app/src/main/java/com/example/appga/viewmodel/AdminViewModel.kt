package com.example.appga.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Resource
import com.example.appga.model.Usuario
import com.example.appga.network.ApiService
import kotlinx.coroutines.launch

class AdminViewModel(
    private val apiService: ApiService = ApiService()
) : ViewModel() {
    
    private val _usersState = mutableStateOf<Resource<List<Usuario>>>(Resource.Loading)
    val usersState: State<Resource<List<Usuario>>> = _usersState
    
    private val _filteredUsers = mutableStateOf<List<Usuario>>(emptyList())
    val filteredUsers: State<List<Usuario>> = _filteredUsers
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        _usersState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                val result = apiService.getAllUsuarios()
                if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    _usersState.value = Resource.Success(users)
                    _filteredUsers.value = users
                } else {
                    _usersState.value = Resource.Error("Error al cargar los usuarios: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _usersState.value = Resource.Error("Error al cargar los usuarios: ${e.message}")
            }
        }
    }
    
    fun filterUsers(query: String) {
        if (_usersState.value is Resource.Success) {
            val allUsers = (_usersState.value as Resource.Success<List<Usuario>>).data
            
            if (query.isBlank()) {
                _filteredUsers.value = allUsers
            } else {
                _filteredUsers.value = allUsers.filter { 
                    it.cedula.contains(query, ignoreCase = true) ||
                    it.rol.contains(query, ignoreCase = true)
                }
            }
        }
    }
    
    fun createUser(user: Usuario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.createUsuario(user)
                if (result.isSuccess) {
                    loadUsers() // Refresh the list
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateUser(user: Usuario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.updateUsuario(user.id ?: 0, user)
                if (result.isSuccess) {
                    loadUsers() // Refresh the list
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteUser(userId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteUsuario(userId)
                if (result.isSuccess) {
                    loadUsers() // Refresh the list
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun changePassword(userId: Int, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Since there's no direct changePassword method in the API,
                // we'll first get the user, update the password, then update the user
                val getUserResult = apiService.getUsuarioById(userId)
                if (getUserResult.isSuccess) {
                    val user = getUserResult.getOrNull()
                    if (user != null) {
                        val updatedUser = user.copy(clave = newPassword)
                        val updateResult = apiService.updateUsuario(userId, updatedUser)
                        if (updateResult.isSuccess) {
                            onSuccess()
                        } else {
                            onError(updateResult.exceptionOrNull()?.message ?: "Error al cambiar contraseña")
                        }
                    } else {
                        onError("Usuario no encontrado")
                    }
                } else {
                    onError(getUserResult.exceptionOrNull()?.message ?: "Error al buscar usuario")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}
