package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Resource
import com.example.appga.model.Usuario
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _loginState = MutableStateFlow<Resource<Usuario>>(Resource.Loading)
    val loginState: StateFlow<Resource<Usuario>> = _loginState
    
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser
    
    fun login(cedula: String, clave: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            println("DEBUG: Iniciando login con cedula: $cedula, instancia de AuthViewModel: ${this@AuthViewModel}")
            
            try {
                val result = apiService.login(cedula, clave)
                
                println("DEBUG: Login response received: $result en instancia: ${this@AuthViewModel}")
                result.fold(
                    onSuccess = { user ->
                        println("DEBUG: User received - ID: ${user.id}, Cedula: ${user.cedula}, Rol: ${user.rol} en instancia: ${this@AuthViewModel}")
                        _currentUser.value = user
                        println("DEBUG: _currentUser actualizado a: ${_currentUser.value} en instancia: ${this@AuthViewModel}")
                        _loginState.value = Resource.Success(user)
                    },
                    onFailure = { e ->
                        println("DEBUG: Login error - ${e.message}")
                        _loginState.value = Resource.Error(e.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Exception during login: ${e.message}")
                _loginState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _loginState.value = Resource.Loading
    }
    
    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }
    
    fun isAdmin(): Boolean {
        return _currentUser.value?.rol == "admin"
    }
    
    fun isInstructor(): Boolean {
        return _currentUser.value?.rol == "profesor"
    }
    
    fun isStudent(): Boolean {
        return _currentUser.value?.rol == "alumno"
    }
    
    fun isRegistrar(): Boolean {
        return _currentUser.value?.rol == "registrador"
    }
    
    fun resetLoginState() {
        // Only reset if we're in a loading state to avoid disrupting other states
    }
}
