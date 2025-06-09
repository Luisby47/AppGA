package com.example.appga.ui.screens.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.*
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseViewModel
import com.example.appga.viewmodel.ProgramViewModel

@Composable
fun ProgramDetailScreen(
    programId: String,
    onBackClick: () -> Unit,
    programViewModel: ProgramViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel()
) {
    val programState by programViewModel.programState.collectAsState()
    val programWithCoursesState by programViewModel.programWithCoursesState.collectAsState()
    val coursesState by courseViewModel.coursesState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var programToEdit by remember { mutableStateOf<Carrera?>(null) }
    
    LaunchedEffect(programId) {
        programViewModel.getProgramById(programId.toInt())
        courseViewModel.loadAllCourses() // Load all courses for the add course dialog
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del Programa",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (programState is Resource.Success) {
                FloatingActionButton(
                    onClick = {
                        programToEdit = (programState as Resource.Success<Carrera>).data
                        showEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Programa")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (programState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (programState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    val program = (programState as Resource.Success<Carrera>).data
                    ProgramDetailContent(
                        program = program,
                        programWithCoursesState = programWithCoursesState,
                        onAddCourseClick = { showAddCourseDialog = true },
                        onRemoveCourseClick = { programCourseId, programCode ->
                            programViewModel.removeCourseFromProgram(programCourseId, programCode)
                        },
                        onMoveCourseUp = { courseCode, currentPosition, programCode ->
                            if (currentPosition > 0) {
                                programViewModel.addCourseToProgramAndReorder(
                                    programCode = programCode,
                                    courseCode = courseCode,
                                    newPosition = currentPosition - 1
                                )
                            }
                        },
                        onMoveCourseDown = { courseCode, currentPosition, programCode ->
                            // We need the total count of courses to check if we're at the end
                            val totalCourses = if (programWithCoursesState is Resource.Success) {
                                (programWithCoursesState as Resource.Success<CarreraWithCursos>).data.cursos.size
                            } else {
                                0
                            }
                            
                            if (currentPosition < totalCourses - 1) {
                                programViewModel.addCourseToProgramAndReorder(
                                    programCode = programCode,
                                    courseCode = courseCode,
                                    newPosition = currentPosition + 1
                                )
                            }
                        }
                    )
                }
            }
        }
        
        // Edit Program Dialog
        if (showEditDialog && programToEdit != null) {
            ProgramDialog(
                program = programToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { program ->
                    program.id?.let { programViewModel.updateProgram(it, program) }
                    showEditDialog = false
                }
            )
        }
        
        // Add Course Dialog
        if (showAddCourseDialog && programState is Resource.Success) {
            val program = (programState as Resource.Success<Carrera>).data
            AddCourseDialog(
                programCode = program.codigo,
                coursesState = coursesState,
                onDismiss = { showAddCourseDialog = false },
                onCourseSelected = { courseCode ->
                    // Get the current number of courses in the program to add at the end
                    val currentCourseCount = if (programWithCoursesState is Resource.Success) {
                        (programWithCoursesState as Resource.Success<CarreraWithCursos>).data.cursos.size
                    } else {
                        0
                    }
                    
                    programViewModel.addCourseToProgramAndReorder(
                        programCode = program.codigo,
                        courseCode = courseCode,
                        newPosition = currentCourseCount // Add at the end
                    )
                    
                    showAddCourseDialog = false
                }
            )
        }
    }
}

@Composable
fun ProgramDetailContent(
    program: Carrera,
    programWithCoursesState: Resource<CarreraWithCursos>,
    onAddCourseClick: () -> Unit,
    onRemoveCourseClick: (Int, String) -> Unit,
    onMoveCourseUp: (String, Int, String) -> Unit,
    onMoveCourseDown: (String, Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Program info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = program.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Código: ${program.codigo}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Título: ${program.titulo}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Courses section header with add button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cursos del Programa",
                    style = MaterialTheme.typography.titleLarge
                )
                
                FilledTonalButton(
                    onClick = onAddCourseClick
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Curso")
                }
            }
        }
        
        // Courses list
        when (programWithCoursesState) {
            is Resource.Loading -> {
                item {
                    LoadingIndicator()
                }
            }
            is Resource.Error -> {
                item {
                    val errorMessage = (programWithCoursesState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar cursos: $errorMessage")
                }
            }
            is Resource.Success -> {
                val programWithCourses = (programWithCoursesState as Resource.Success<CarreraWithCursos>).data
                val courses = programWithCourses.cursos
                
                if (courses.isEmpty()) {
                    item {
                        EmptyListMessage(message = "No hay cursos asignados a este programa")
                    }
                } else {
                    // Header row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Orden",
                                modifier = Modifier.weight(0.1f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Código",
                                modifier = Modifier.weight(0.2f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nombre",
                                modifier = Modifier.weight(0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Acciones",
                                modifier = Modifier.weight(0.2f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Course rows
                    itemsIndexed(courses) { index, courseWithOrder ->
                        ProgramCourseItem(
                            course = courseWithOrder,
                            index = index,
                            programCode = program.codigo,
                            isFirst = index == 0,
                            isLast = index == courses.size - 1,
                            onRemove = onRemoveCourseClick,
                            onMoveUp = onMoveCourseUp,
                            onMoveDown = onMoveCourseDown
                        )
                        
                        if (index < courses.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramCourseItem(
    course: CursoWithOrder,
    index: Int,
    programCode: String,
    isFirst: Boolean,
    isLast: Boolean,
    onRemove: (Int, String) -> Unit,
    onMoveUp: (String, Int, String) -> Unit,
    onMoveDown: (String, Int, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${course.orden}",
            modifier = Modifier.weight(0.1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = course.curso.codigo,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            text = course.curso.nombre,
            modifier = Modifier.weight(0.5f)
        )
        
        Row(
            modifier = Modifier.weight(0.2f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { onMoveUp(course.curso.codigo, course.orden, programCode) },
                enabled = !isFirst
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Mover Arriba",
                    tint = if (!isFirst) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            
            IconButton(
                onClick = { onMoveDown(course.curso.codigo, course.orden, programCode) },
                enabled = !isLast
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Mover Abajo",
                    tint = if (!isLast) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            
            IconButton(
                onClick = {
                    // We need the course.id from the CarreraCurso entity, but our model might not have it
                    // In a real app, you'd have the ID available
                    // For now, we'll use a placeholder ID of 0
                    onRemove(0, programCode)
                }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    programCode: String,
    coursesState: Resource<List<Curso>>,
    onDismiss: () -> Unit,
    onCourseSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredCourses by remember { mutableStateOf<List<Curso>>(emptyList()) }
    
    // Update filtered courses when search query changes or courses state changes
    LaunchedEffect(searchQuery, coursesState) {
        if (coursesState is Resource.Success) {
            val allCourses = (coursesState as Resource.Success<List<Curso>>).data
            filteredCourses = if (searchQuery.isBlank()) {
                allCourses
            } else {
                allCourses.filter { 
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.codigo.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Curso al Programa") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Curso") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
                
                when (coursesState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    is Resource.Error -> {
                        val errorMessage = (coursesState as Resource.Error).message
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    is Resource.Success -> {
                        if (filteredCourses.isEmpty()) {
                            Text(
                                text = "No se encontraron cursos",
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                items(filteredCourses) { course ->
                                    CourseSelectionItem(
                                        course = course,
                                        onCourseSelected = { onCourseSelected(course.codigo) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CourseSelectionItem(
    course: Curso,
    onCourseSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onCourseSelected
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Código: ${course.codigo} | Créditos: ${course.creditos}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
