package com.example.appga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appga.model.Ciclo
import com.example.appga.model.Resource
import com.example.appga.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TermViewModel : ViewModel() {
    
    private val apiService = ApiService()
    
    private val _termsState = MutableStateFlow<Resource<List<Ciclo>>>(Resource.Loading)
    val termsState: StateFlow<Resource<List<Ciclo>>> = _termsState
    
    private val _termState = MutableStateFlow<Resource<Ciclo>>(Resource.Loading)
    val termState: StateFlow<Resource<Ciclo>> = _termState
    
    private val _activeTermState = MutableStateFlow<Resource<Ciclo>>(Resource.Loading)
    val activeTermState: StateFlow<Resource<Ciclo>> = _activeTermState
    
    private val _filteredTerms = MutableStateFlow<List<Ciclo>>(emptyList())
    val filteredTerms: StateFlow<List<Ciclo>> = _filteredTerms
    
    private var allTerms: List<Ciclo> = emptyList()
    
    init {
        loadAllTerms()
        loadActiveTerm()
    }
    
    fun loadAllTerms() {
        viewModelScope.launch {
            _termsState.value = Resource.Loading
            
            try {
                val result = apiService.getAllCiclos()
                
                result.fold(
                    onSuccess = { terms ->
                        allTerms = terms
                        _filteredTerms.value = terms
                        _termsState.value = Resource.Success(terms)
                    },
                    onFailure = { e ->
                        _termsState.value = Resource.Error(e.message ?: "Error al cargar ciclos")
                    }
                )
            } catch (e: Exception) {
                _termsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getTermById(id: Int) {
        viewModelScope.launch {
            _termState.value = Resource.Loading
            
            try {
                val result = apiService.getCicloById(id)
                
                result.fold(
                    onSuccess = { term ->
                        _termState.value = Resource.Success(term)
                    },
                    onFailure = { e ->
                        _termState.value = Resource.Error(e.message ?: "Error al cargar ciclo")
                    }
                )
            } catch (e: Exception) {
                _termState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getTermsByYear(year: Int) {
        viewModelScope.launch {
            _filteredTerms.value = emptyList()
            
            try {
                val result = apiService.getCiclosByAnio(year)
                
                result.fold(
                    onSuccess = { terms ->
                        _filteredTerms.value = terms
                    },
                    onFailure = { /* Keep filtered terms empty */ }
                )
            } catch (e: Exception) {
                // Keep filtered terms empty
            }
        }
    }
    
    fun loadActiveTerm() {
        viewModelScope.launch {
            _activeTermState.value = Resource.Loading
            
            try {
                val result = apiService.getActiveCiclo()
                
                result.fold(
                    onSuccess = { activeTerm ->
                        _activeTermState.value = Resource.Success(activeTerm)
                    },
                    onFailure = { e ->
                        _activeTermState.value = Resource.Error(e.message ?: "Error al cargar ciclo activo")
                    }
                )
            } catch (e: Exception) {
                _activeTermState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun createTerm(term: Ciclo) {
        viewModelScope.launch {
            _termState.value = Resource.Loading
            
            try {
                val result = apiService.createCiclo(term)
                
                result.fold(
                    onSuccess = { newTerm ->
                        _termState.value = Resource.Success(newTerm)
                        loadAllTerms() // Refresh term list
                    },
                    onFailure = { e ->
                        _termState.value = Resource.Error(e.message ?: "Error al crear ciclo")
                    }
                )
            } catch (e: Exception) {
                _termState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateTerm(id: Int, term: Ciclo) {
        viewModelScope.launch {
            _termState.value = Resource.Loading
            
            try {
                val result = apiService.updateCiclo(id, term)
                
                result.fold(
                    onSuccess = { updatedTerm ->
                        _termState.value = Resource.Success(updatedTerm)
                        loadAllTerms() // Refresh term list
                        
                        // If this is the active term, refresh active term state
                        if (updatedTerm.activo) {
                            _activeTermState.value = Resource.Success(updatedTerm)
                        }
                    },
                    onFailure = { e ->
                        _termState.value = Resource.Error(e.message ?: "Error al actualizar ciclo")
                    }
                )
            } catch (e: Exception) {
                _termState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun setActiveTerm(id: Int) {
        viewModelScope.launch {
            _activeTermState.value = Resource.Loading
            
            try {
                val result = apiService.setActiveCiclo(id)
                
                result.fold(
                    onSuccess = { activeTerm ->
                        _activeTermState.value = Resource.Success(activeTerm)
                        loadAllTerms() // Refresh term list to update active status for all terms
                    },
                    onFailure = { e ->
                        _activeTermState.value = Resource.Error(e.message ?: "Error al establecer ciclo activo")
                    }
                )
            } catch (e: Exception) {
                _activeTermState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun deleteTerm(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.deleteCiclo(id)
                
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            loadAllTerms() // Refresh term list
                            loadActiveTerm() // Refresh active term in case the deleted term was active
                        } else {
                            _termsState.value = Resource.Error("Error al eliminar ciclo")
                        }
                    },
                    onFailure = { e ->
                        _termsState.value = Resource.Error(e.message ?: "Error al eliminar ciclo")
                    }
                )
            } catch (e: Exception) {
                _termsState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun filterTermsByYear(year: Int) {
        if (year <= 0) {
            _filteredTerms.value = allTerms
            return
        }
        
        val filteredList = allTerms.filter { it.anio == year }
        _filteredTerms.value = filteredList
    }
    
    fun resetTermState() {
        _termState.value = Resource.Loading
    }
}
