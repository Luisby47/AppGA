package com.example.appga.ui.screens.instructors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Profesor
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.InstructorFilterType
import com.example.appga.viewmodel.InstructorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorsScreen(
    onInstructorClick: (String) -> Unit,
    onBackClick: () -> Unit,
    instructorViewModel: InstructorViewModel = viewModel()
) {
    val instructorsState by instructorViewModel.instructorsState.collectAsState()
    val filteredInstructors by instructorViewModel.filteredInstructors.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(InstructorFilterType.NAME) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var instructorToEdit by remember { mutableStateOf<Profesor?>(null) }
    
    LaunchedEffect(Unit) {
        instructorViewModel.loadAllInstructors()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Gestión de Profesores",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    instructorToEdit = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Profesor")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and filter bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        instructorViewModel.filterInstructors(it, filterType)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar profesores...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledTonalButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Text("Filtro: ${
                        when (filterType) {
                            InstructorFilterType.NAME -> "Nombre"
                            InstructorFilterType.ID -> "Cédula"
                        }
                    }")
                }
            }
            
            when (instructorsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (instructorsState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredInstructors.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron profesores")
                    } else {
                        InstructorsList(
                            instructors = filteredInstructors,
                            onInstructorClick = { instructor ->
                                instructor.id?.let { onInstructorClick(it.toString()) }
                            },
                            onEditClick = { instructor ->
                                instructorToEdit = instructor
                                showAddEditDialog = true
                            },
                            onDeleteClick = { instructor ->
                                instructor.id?.let { instructorViewModel.deleteInstructor(it) }
                            }
                        )
                    }
                }
            }
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Seleccionar Filtro") },
                text = {
                    Column {
                        InstructorFilterType.values().forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = filterType == type,
                                    onClick = { 
                                        filterType = type
                                        instructorViewModel.filterInstructors(searchQuery, type)
                                    }
                                )
                                
                                Text(
                                    text = when (type) {
                                        InstructorFilterType.NAME -> "Nombre"
                                        InstructorFilterType.ID -> "Cédula"
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        
        // Add/Edit Dialog
        if (showAddEditDialog) {
            InstructorDialog(
                instructor = instructorToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { instructor ->
                    if (instructor.id == null) {
                        instructorViewModel.createInstructor(instructor)
                    } else {
                        instructorViewModel.updateInstructor(instructor.id, instructor)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun InstructorsList(
    instructors: List<Profesor>,
    onInstructorClick: (Profesor) -> Unit,
    onEditClick: (Profesor) -> Unit,
    onDeleteClick: (Profesor) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(instructors) { instructor ->
            AppCard(
                title = instructor.nombre,
                subtitle = "Cédula: ${instructor.cedula} | ${instructor.email}",
                onClick = { onInstructorClick(instructor) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEditClick(instructor) }) {
                        Text("Editar")
                    }
                    
                    TextButton(onClick = { onDeleteClick(instructor) }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorDialog(
    instructor: Profesor?,
    onDismiss: () -> Unit,
    onSave: (Profesor) -> Unit
) {
    val isNewInstructor = instructor == null
    val title = if (isNewInstructor) "Agregar Profesor" else "Editar Profesor"
    
    var cedula by remember { mutableStateOf(instructor?.cedula ?: "") }
    var nombre by remember { mutableStateOf(instructor?.nombre ?: "") }
    var telefono by remember { mutableStateOf(instructor?.telefono ?: "") }
    var email by remember { mutableStateOf(instructor?.email ?: "") }
    
    var cedulaError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    
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
                    value = cedula,
                    onValueChange = { 
                        cedula = it
                        cedulaError = it.isBlank()
                    },
                    label = { Text("Cédula") },
                    isError = cedulaError,
                    supportingText = { if (cedulaError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = isNewInstructor // Don't allow changing the ID for existing instructors
                )
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { 
                        nombre = it
                        nombreError = it.isBlank()
                    },
                    label = { Text("Nombre") },
                    isError = nombreError,
                    supportingText = { if (nombreError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = it.isBlank() || !it.contains("@")
                    },
                    label = { Text("Email") },
                    isError = emailError,
                    supportingText = { if (emailError) Text("Email inválido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    cedulaError = cedula.isBlank()
                    nombreError = nombre.isBlank()
                    emailError = email.isBlank() || !email.contains("@")
                    
                    if (!cedulaError && !nombreError && !emailError) {
                        val updatedInstructor = Profesor(
                            id = instructor?.id,
                            cedula = cedula,
                            nombre = nombre,
                            telefono = telefono.ifBlank { null },
                            email = email
                        )
                        
                        onSave(updatedInstructor)
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
