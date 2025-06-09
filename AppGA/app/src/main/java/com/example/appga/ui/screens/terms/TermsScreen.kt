package com.example.appga.ui.screens.terms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Ciclo
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.TermViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onBackClick: () -> Unit,
    termViewModel: TermViewModel = viewModel()
) {
    val termsState by termViewModel.termsState.collectAsState()
    val filteredTerms by termViewModel.filteredTerms.collectAsState()
    val activeTermState by termViewModel.activeTermState.collectAsState()
    
    var searchYear by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var termToEdit by remember { mutableStateOf<Ciclo?>(null) }
    
    LaunchedEffect(Unit) {
        termViewModel.loadAllTerms()
        termViewModel.loadActiveTerm()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Gestión de Períodos",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    termToEdit = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Período")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search by year
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchYear,
                    onValueChange = { 
                        searchYear = it
                        if (it.isNotBlank() && it.toIntOrNull() != null) {
                            termViewModel.filterTermsByYear(it.toInt())
                        } else {
                            termViewModel.filterTermsByYear(0) // Reset filter
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar por año...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
            }
            
            // Active term banner
            when (activeTermState) {
                is Resource.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                is Resource.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error al cargar período activo: ${(activeTermState as Resource.Error).message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is Resource.Success -> {
                    val activeTerm = (activeTermState as Resource.Success<Ciclo>).data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Período Activo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${activeTerm.anio}-${activeTerm.numero}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            if (activeTerm.fechaInicio != null && activeTerm.fechaFin != null) {
                                Text(
                                    text = "Del ${activeTerm.fechaInicio} al ${activeTerm.fechaFin}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Terms list
            when (termsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (termsState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredTerms.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron períodos")
                    } else {
                        TermsList(
                            terms = filteredTerms,
                            activeTermId = if (activeTermState is Resource.Success) {
                                (activeTermState as Resource.Success<Ciclo>).data.id
                            } else null,
                            onSetActiveClick = { term ->
                                term.id?.let { termViewModel.setActiveTerm(it) }
                            },
                            onEditClick = { term ->
                                termToEdit = term
                                showAddEditDialog = true
                            },
                            onDeleteClick = { term ->
                                term.id?.let { termViewModel.deleteTerm(it) }
                            }
                        )
                    }
                }
            }
        }
        
        // Add/Edit Dialog
        if (showAddEditDialog) {
            TermDialog(
                term = termToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { term ->
                    if (term.id == null) {
                        termViewModel.createTerm(term)
                    } else {
                        termViewModel.updateTerm(term.id, term)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun TermsList(
    terms: List<Ciclo>,
    activeTermId: Int?,
    onSetActiveClick: (Ciclo) -> Unit,
    onEditClick: (Ciclo) -> Unit,
    onDeleteClick: (Ciclo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(terms) { term ->
            val isActive = term.id == activeTermId
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${term.anio}-${term.numero}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            if (term.fechaInicio != null && term.fechaFin != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Del ${term.fechaInicio} al ${term.fechaFin}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Período Activo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!isActive) {
                            TextButton(
                                onClick = { onSetActiveClick(term) }
                            ) {
                                Text("Establecer como Activo")
                            }
                        }
                        
                        TextButton(
                            onClick = { onEditClick(term) }
                        ) {
                            Text("Editar")
                        }
                        
                        TextButton(
                            onClick = { onDeleteClick(term) },
                            enabled = !isActive // Don't allow deleting the active term
                        ) {
                            Text(
                                "Eliminar", 
                                color = if (!isActive) MaterialTheme.colorScheme.error else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermDialog(
    term: Ciclo?,
    onDismiss: () -> Unit,
    onSave: (Ciclo) -> Unit
) {
    val isNewTerm = term == null
    val title = if (isNewTerm) "Agregar Período" else "Editar Período"
    
    var anio by remember { mutableStateOf(term?.anio?.toString() ?: "") }
    var numero by remember { mutableStateOf(term?.numero ?: "") }
    var fechaInicio by remember { mutableStateOf(term?.fechaInicio ?: "") }
    var fechaFin by remember { mutableStateOf(term?.fechaFin ?: "") }
    var activo by remember { mutableStateOf(term?.activo ?: false) }
    
    var anioError by remember { mutableStateOf(false) }
    var numeroError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = anio,
                    onValueChange = { 
                        anio = it
                        anioError = it.toIntOrNull() == null
                    },
                    label = { Text("Año") },
                    isError = anioError,
                    supportingText = { if (anioError) Text("Ingrese un año válido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = numero,
                    onValueChange = { 
                        numero = it
                        numeroError = it.isBlank()
                    },
                    label = { Text("Número de Ciclo") },
                    isError = numeroError,
                    supportingText = { if (numeroError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = { fechaInicio = it },
                    label = { Text("Fecha de Inicio (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { fechaFin = it },
                    label = { Text("Fecha de Fin (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = activo,
                        onCheckedChange = { activo = it }
                    )
                    
                    Text(
                        text = "Establecer como período activo",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    anioError = anio.toIntOrNull() == null
                    numeroError = numero.isBlank()
                    
                    if (!anioError && !numeroError) {
                        val updatedTerm = Ciclo(
                            id = term?.id,
                            anio = anio.toInt(),
                            numero = numero,
                            fechaInicio = fechaInicio.ifBlank { null },
                            fechaFin = fechaFin.ifBlank { null },
                            activo = activo
                        )
                        
                        onSave(updatedTerm)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
