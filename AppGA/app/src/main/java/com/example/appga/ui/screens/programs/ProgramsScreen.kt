package com.example.appga.ui.screens.programs

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
import com.example.appga.model.Carrera
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.ProgramFilterType
import com.example.appga.viewmodel.ProgramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    onProgramClick: (String) -> Unit,
    onBackClick: () -> Unit,
    programViewModel: ProgramViewModel = viewModel()
) {
    val programsState by programViewModel.programsState.collectAsState()
    val filteredPrograms by programViewModel.filteredPrograms.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(ProgramFilterType.NAME) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var programToEdit by remember { mutableStateOf<Carrera?>(null) }
    
    LaunchedEffect(Unit) {
        programViewModel.loadAllPrograms()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Gestión de Programas",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    programToEdit = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Programa")
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
                        programViewModel.filterPrograms(it, filterType)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar programas...") },
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
                            ProgramFilterType.NAME -> "Nombre"
                            ProgramFilterType.ID -> "Código"
                        }
                    }")
                }
            }
            
            when (programsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (programsState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredPrograms.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron programas")
                    } else {
                        ProgramsList(
                            programs = filteredPrograms,
                            onProgramClick = { program ->
                                program.id?.let { onProgramClick(it.toString()) }
                            },
                            onEditClick = { program ->
                                programToEdit = program
                                showAddEditDialog = true
                            },
                            onDeleteClick = { program ->
                                program.id?.let { programViewModel.deleteProgram(it) }
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
                        ProgramFilterType.values().forEach { type ->
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
                                        programViewModel.filterPrograms(searchQuery, type)
                                    }
                                )
                                
                                Text(
                                    text = when (type) {
                                        ProgramFilterType.NAME -> "Nombre"
                                        ProgramFilterType.ID -> "Código"
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
            ProgramDialog(
                program = programToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { program ->
                    if (program.id == null) {
                        programViewModel.createProgram(program)
                    } else {
                        programViewModel.updateProgram(program.id, program)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ProgramsList(
    programs: List<Carrera>,
    onProgramClick: (Carrera) -> Unit,
    onEditClick: (Carrera) -> Unit,
    onDeleteClick: (Carrera) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(programs) { program ->
            AppCard(
                title = "${program.codigo} - ${program.nombre}",
                subtitle = "Título: ${program.titulo}",
                onClick = { onProgramClick(program) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEditClick(program) }) {
                        Text("Editar")
                    }
                    
                    TextButton(onClick = { onDeleteClick(program) }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDialog(
    program: Carrera?,
    onDismiss: () -> Unit,
    onSave: (Carrera) -> Unit
) {
    val isNewProgram = program == null
    val title = if (isNewProgram) "Agregar Programa" else "Editar Programa"
    
    var codigo by remember { mutableStateOf(program?.codigo ?: "") }
    var nombre by remember { mutableStateOf(program?.nombre ?: "") }
    var titulo by remember { mutableStateOf(program?.titulo ?: "") }
    
    var codigoError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var tituloError by remember { mutableStateOf(false) }
    
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
                    value = codigo,
                    onValueChange = { 
                        codigo = it
                        codigoError = it.isBlank()
                    },
                    label = { Text("Código") },
                    isError = codigoError,
                    supportingText = { if (codigoError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = isNewProgram // Don't allow changing the code for existing programs
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
                    value = titulo,
                    onValueChange = { 
                        titulo = it
                        tituloError = it.isBlank()
                    },
                    label = { Text("Título") },
                    isError = tituloError,
                    supportingText = { if (tituloError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    codigoError = codigo.isBlank()
                    nombreError = nombre.isBlank()
                    tituloError = titulo.isBlank()
                    
                    if (!codigoError && !nombreError && !tituloError) {
                        val updatedProgram = Carrera(
                            id = program?.id,
                            codigo = codigo,
                            nombre = nombre,
                            titulo = titulo
                        )
                        
                        onSave(updatedProgram)
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
