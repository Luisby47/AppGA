package com.example.appga.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Grupo
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseViewModel
import com.example.appga.viewmodel.GroupViewModel
import com.example.appga.viewmodel.InstructorViewModel
import com.example.appga.viewmodel.TermViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    courseId: Int? = null,
    onBackClick: () -> Unit,
    onGroupClick: (Int) -> Unit,
    groupViewModel: GroupViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel(),
    instructorViewModel: InstructorViewModel = viewModel(),
    termViewModel: TermViewModel = viewModel()
) {
    val groupsState by groupViewModel.groupsState.collectAsStateWithLifecycle()
    val filteredGroups by groupViewModel.filteredGroups
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(courseId) {
        if (courseId != null) {
            groupViewModel.loadGroupsByCourse(courseId)
            courseViewModel.getCourseById(courseId)
        } else {
            groupViewModel.loadAllGroups()
        }
        instructorViewModel.loadAllInstructors()
        termViewModel.loadAllTerms()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = if (courseId != null) "Grupos del Curso" else "Todos los Grupos",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGroupDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Grupo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    groupViewModel.filterGroups(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar grupos...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true
            )
            
            when (groupsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (groupsState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredGroups.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron grupos")
                    } else {
                        GroupsList(
                            groups = filteredGroups,
                            onGroupClick = onGroupClick
                        )
                    }
                }
            }
        }
        
        // Add Group Dialog
        if (showAddGroupDialog) {
            val coursesState by courseViewModel.coursesState.collectAsStateWithLifecycle()
            val courses = if (coursesState is Resource.Success) {
                (coursesState as Resource.Success).data
            } else {
                emptyList()
            }
            
            val instructorsState by instructorViewModel.instructorsState.collectAsStateWithLifecycle()
            val instructors = if (instructorsState is Resource.Success) {
                (instructorsState as Resource.Success).data
            } else {
                emptyList()
            }
            
            val termsState by termViewModel.termsState.collectAsStateWithLifecycle()
            val terms = if (termsState is Resource.Success) {
                (termsState as Resource.Success).data
            } else {
                emptyList()
            }
            
            GroupDialog(
                courseId = courseId,
                onDismiss = { showAddGroupDialog = false },
                onSave = { group ->
                    groupViewModel.createGroup(group) { newGroupId ->
                        // Refresh and potentially navigate to the new group
                        if (courseId != null) {
                            groupViewModel.loadGroupsByCourse(courseId)
                        } else {
                            groupViewModel.loadAllGroups()
                        }
                        showAddGroupDialog = false
                    }
                },
                availableCourses = courses,
                availableInstructors = instructors,
                availableTerms = terms
            )
        }
    }
}

@Composable
fun GroupsList(
    groups: List<Grupo>,
    onGroupClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            AppCard(
                title = "Grupo ${group.numeroGrupo}",
                subtitle = "Curso: ${group.codigoCurso} | Profesor: ${group.cedulaProfesor}",
                onClick = { group.id?.let { onGroupClick(it) } }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Horario: ${group.horario}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "Ciclo: ${group.anio}-${group.numeroCiclo}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDialog(
    courseId: Int? = null,
    onDismiss: () -> Unit,
    onSave: (Grupo) -> Unit,
    availableCourses: List<com.example.appga.model.Curso>,
    availableInstructors: List<com.example.appga.model.Profesor>,
    availableTerms: List<com.example.appga.model.Ciclo>
) {
    var selectedCourseCode by remember { mutableStateOf(courseId?.let { 
        availableCourses.find { course -> course.id == courseId }?.codigo
    } ?: "") }
    
    var selectedInstructorId by remember { mutableStateOf("") }
    var selectedTermYear by remember { mutableStateOf(0) }
    var selectedTermNumber by remember { mutableStateOf("") }
    var groupNumber by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }
    var instructorExpanded by remember { mutableStateOf(false) }
    var termExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Grupo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (courseId == null) {
                    // Course selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCourseCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Curso") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(vertical = 8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableCourses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text("${course.codigo} - ${course.nombre}") },
                                    onClick = {
                                        selectedCourseCode = course.codigo
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Instructor selection
                ExposedDropdownMenuBox(
                    expanded = instructorExpanded,
                    onExpandedChange = { instructorExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedInstructorId.ifEmpty { 
                            "Seleccione un profesor" 
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesor") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = instructorExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(vertical = 8.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = instructorExpanded,
                        onDismissRequest = { instructorExpanded = false }
                    ) {
                        availableInstructors.forEach { instructor ->
                            DropdownMenuItem(
                                text = { Text("${instructor.cedula} - ${instructor.nombre}") },
                                onClick = {
                                    selectedInstructorId = instructor.cedula
                                    instructorExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Term selection
                ExposedDropdownMenuBox(
                    expanded = termExpanded,
                    onExpandedChange = { termExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedTermYear > 0) "$selectedTermYear-$selectedTermNumber" else "Seleccione un ciclo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ciclo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = termExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(vertical = 8.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = termExpanded,
                        onDismissRequest = { termExpanded = false }
                    ) {
                        availableTerms.forEach { term ->
                            DropdownMenuItem(
                                text = { Text("${term.anio}-${term.numero}") },
                                onClick = {
                                    selectedTermYear = term.anio
                                    selectedTermNumber = term.numero
                                    termExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Group number
                OutlinedTextField(
                    value = groupNumber,
                    onValueChange = { groupNumber = it },
                    label = { Text("Número de Grupo") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                // Schedule
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Horario (ej. L-J 13:00-14:50)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validate inputs
                    if (selectedCourseCode.isNotEmpty() && 
                        selectedInstructorId.isNotEmpty() && 
                        selectedTermYear > 0 && 
                        groupNumber.isNotEmpty() && 
                        schedule.isNotEmpty()) {
                        
                        val group = Grupo(
                            id = null,
                            anio = selectedTermYear,
                            numeroCiclo = selectedTermNumber.toString(),
                            codigoCurso = selectedCourseCode,
                            numeroGrupo = groupNumber.toIntOrNull() ?: 1,
                            horario = schedule,
                            cedulaProfesor = selectedInstructorId
                        )
                        
                        onSave(group)
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
