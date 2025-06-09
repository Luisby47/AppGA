package com.example.appga.ui.screens.courses

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
import com.example.appga.model.Curso
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseFilterType
import com.example.appga.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onCourseClick: (String) -> Unit,
    onBackClick: () -> Unit,
    filterByInstructor: Boolean = false,
    courseViewModel: CourseViewModel = viewModel()
) {
    val coursesState by courseViewModel.coursesState.collectAsState()
    val filteredCourses by courseViewModel.filteredCourses.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(CourseFilterType.NAME) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Curso?>(null) }
    
    LaunchedEffect(Unit) {
        courseViewModel.loadAllCourses()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Gestión de Cursos",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    courseToEdit = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
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
                        courseViewModel.filterCourses(it, filterType)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar cursos...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledTonalButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Text("Filtro: ${filterType.name}")
                }
            }
            
            when (coursesState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (coursesState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredCourses.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron cursos")
                    } else {
                        CoursesList(
                            courses = filteredCourses,
                            onCourseClick = { course ->
                                course.id?.let { 
                                    try {
                                        onCourseClick(it.toString()) 
                                    } catch (e: Exception) {
                                        // Log error but don't crash
                                        println("Error al navegar al curso: ${e.message}")
                                    }
                                }
                            },
                            onEditClick = { course ->
                                courseToEdit = course
                                showAddEditDialog = true
                            },
                            onDeleteClick = { course ->
                                course.id?.let { courseViewModel.deleteCourse(it) }
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
                        CourseFilterType.values().forEach { type ->
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
                                        courseViewModel.filterCourses(searchQuery, type)
                                    }
                                )
                                
                                Text(
                                    text = when (type) {
                                        CourseFilterType.NAME -> "Nombre"
                                        CourseFilterType.ID -> "Código"
                                        CourseFilterType.MAJOR -> "Carrera"
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
            CourseDialog(
                course = courseToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { course ->
                    if (course.id == null) {
                        courseViewModel.createCourse(course)
                    } else {
                        courseViewModel.updateCourse(course.id, course)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun CoursesList(
    courses: List<Curso>,
    onCourseClick: (Curso) -> Unit,
    onEditClick: (Curso) -> Unit,
    onDeleteClick: (Curso) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(courses) { course ->
            AppCard(
                title = "${course.codigo} - ${course.nombre}",
                subtitle = "Créditos: ${course.creditos} | Horas: ${course.horasSemanales}",
                onClick = { onCourseClick(course) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEditClick(course) }) {
                        Text("Editar")
                    }
                    
                    TextButton(onClick = { onDeleteClick(course) }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(
    course: Curso?,
    onDismiss: () -> Unit,
    onSave: (Curso) -> Unit
) {
    val isNewCourse = course == null
    val title = if (isNewCourse) "Agregar Curso" else "Editar Curso"
    
    var codigo by remember { mutableStateOf(course?.codigo ?: "") }
    var nombre by remember { mutableStateOf(course?.nombre ?: "") }
    var creditos by remember { mutableStateOf(course?.creditos?.toString() ?: "") }
    var horasSemanales by remember { mutableStateOf(course?.horasSemanales?.toString() ?: "") }
    
    var codigoError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var creditosError by remember { mutableStateOf(false) }
    var horasSemanalesError by remember { mutableStateOf(false) }
    
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
                    enabled = isNewCourse // Don't allow changing the code for existing courses
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
                    value = creditos,
                    onValueChange = { 
                        creditos = it
                        creditosError = it.toIntOrNull() == null
                    },
                    label = { Text("Créditos") },
                    isError = creditosError,
                    supportingText = { if (creditosError) Text("Ingrese un número válido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = horasSemanales,
                    onValueChange = { 
                        horasSemanales = it
                        horasSemanalesError = it.toIntOrNull() == null
                    },
                    label = { Text("Horas Semanales") },
                    isError = horasSemanalesError,
                    supportingText = { if (horasSemanalesError) Text("Ingrese un número válido") },
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
                    creditosError = creditos.toIntOrNull() == null
                    horasSemanalesError = horasSemanales.toIntOrNull() == null
                    
                    if (!codigoError && !nombreError && !creditosError && !horasSemanalesError) {
                        val updatedCourse = Curso(
                            id = course?.id,
                            codigo = codigo,
                            nombre = nombre,
                            creditos = creditos.toInt(),
                            horasSemanales = horasSemanales.toInt()
                        )
                        
                        onSave(updatedCourse)
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
