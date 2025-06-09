package com.example.appga.ui.screens.instructors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Grupo
import com.example.appga.model.Profesor
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseViewModel
import com.example.appga.viewmodel.InstructorViewModel

@Composable
fun InstructorDetailScreen(
    instructorId: String,
    onBackClick: () -> Unit,
    instructorViewModel: InstructorViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel()
) {
    val instructorState by instructorViewModel.instructorState.collectAsState()
    val instructorGroupsState by instructorViewModel.instructorGroupsState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var instructorToEdit by remember { mutableStateOf<Profesor?>(null) }
    
    // Map to store course names for each group
    val courseNames = remember { mutableStateMapOf<String, String>() }
    
    LaunchedEffect(instructorId) {
        instructorViewModel.getInstructorById(instructorId.toInt())
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del Profesor",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (instructorState is Resource.Success) {
                FloatingActionButton(
                    onClick = {
                        instructorToEdit = (instructorState as Resource.Success<Profesor>).data
                        showEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Profesor")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (instructorState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (instructorState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    val instructor = (instructorState as Resource.Success<Profesor>).data
                    InstructorDetailContent(
                        instructor = instructor,
                        instructorGroupsState = instructorGroupsState,
                        courseNames = courseNames,
                        onLoadCourseInfo = { courseCode ->
                            // Load course info if not already loaded
                            if (!courseNames.containsKey(courseCode)) {
                                courseViewModel.getCourseByCodigo(courseCode)
                            }
                        }
                    )
                }
            }
        }
        
        // Observe course state changes and update course names map
        val courseState by courseViewModel.courseState.collectAsState()
        LaunchedEffect(courseState) {
            if (courseState is Resource.Success) {
                val course = (courseState as Resource.Success).data
                courseNames[course.codigo] = course.nombre
            }
        }
        
        // Edit Dialog
        if (showEditDialog && instructorToEdit != null) {
            InstructorDialog(
                instructor = instructorToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { instructor ->
                    instructor.id?.let { instructorViewModel.updateInstructor(it, instructor) }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun InstructorDetailContent(
    instructor: Profesor,
    instructorGroupsState: Resource<List<Grupo>>,
    courseNames: Map<String, String>,
    onLoadCourseInfo: (String) -> Unit,
    onEditClick: (Grupo) -> Unit = {},
    onDeleteClick: (Grupo) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        text = instructor.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cédula: ${instructor.cedula}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (!instructor.telefono.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Teléfono: ${instructor.telefono}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Email: ${instructor.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Grupos Asignados",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        when (instructorGroupsState) {
            is Resource.Loading -> {
                item {
                    LoadingIndicator()
                }
            }
            is Resource.Error -> {
                item {
                    val errorMessage = (instructorGroupsState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar grupos: $errorMessage")
                }
            }
            is Resource.Success -> {
                val groups = (instructorGroupsState as Resource.Success<List<Grupo>>).data
                
                if (groups.isEmpty()) {
                    item {
                        EmptyListMessage(message = "No hay grupos asignados a este profesor")
                    }
                } else {
                    items(groups) { group ->
                        // Request course info if needed
                        LaunchedEffect(group.codigoCurso) {
                            onLoadCourseInfo(group.codigoCurso)
                        }
                        
                        val courseName = courseNames[group.codigoCurso] ?: "Cargando..."
                        
                        AppCard(
                            title = "$courseName - Grupo ${group.numeroGrupo}",
                            subtitle = "Ciclo: ${group.anio}-${group.numeroCiclo} | Horario: ${group.horario ?: "No definido"}"
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEditClick(group) }) {
                                    Text("Editar")
                                }

                                TextButton(onClick = { onDeleteClick(group) }) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
