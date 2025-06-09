package com.example.appga.ui.screens.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Curso
import com.example.appga.model.Grupo
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseViewModel
import com.example.appga.viewmodel.GroupViewModel
import com.example.appga.viewmodel.TermViewModel

@Composable
fun CourseDetailScreen(
    courseId: Int,
    onBackClick: () -> Unit,
    onGroupClick: (Grupo) -> Unit = {},
    courseViewModel: CourseViewModel = viewModel(),
    termViewModel: TermViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    val courseState by courseViewModel.courseState.collectAsState()
    val activeTermState by termViewModel.activeTermState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Curso?>(null) }
    
    LaunchedEffect(courseId) {
        courseViewModel.getCourseById(courseId)
        termViewModel.loadActiveTerm()
        groupViewModel.loadGroupsByCourse(courseId)
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del Curso",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (courseState is Resource.Success) {
                FloatingActionButton(
                    onClick = {
                        courseToEdit = (courseState as Resource.Success<Curso>).data
                        showEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Curso")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (courseState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (courseState as Resource.Error).message
                    EmptyListMessage(message = "No se pudo cargar la información del curso: $errorMessage")
                }
                is Resource.Success -> {
                    val course = (courseState as Resource.Success<Curso>).data
                    CourseDetailContent(
                        course = course, 
                        onGroupClick = onGroupClick,
                        groupViewModel = groupViewModel
                    )
                }
            }
        }
        
        // Edit Dialog
        if (showEditDialog && courseToEdit != null) {
            CourseDialog(
                course = courseToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { course ->
                    course.id?.let { courseViewModel.updateCourse(it, course) }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun CourseDetailContent(
    course: Curso, 
    onGroupClick: (Grupo) -> Unit = {},
    groupViewModel: GroupViewModel
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
                        text = course.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Código: ${course.codigo}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Créditos: ${course.creditos}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Horas Semanales: ${course.horasSemanales}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Grupos del Curso",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Display groups if available
        item {
            // Use simpler approach to avoid property delegate issues
            val groupsState = groupViewModel.groupsState.collectAsState(initial = Resource.Loading).value
            when (groupsState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                is Resource.Error -> {
                    val error = groupsState as Resource.Error
                    EmptyListMessage(message = "Error: ${error.message}")
                }
                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val groups = (groupsState as Resource.Success<List<Grupo>>).data
                    if (groups.isEmpty()) {
                        EmptyListMessage(message = "No hay grupos disponibles para este curso")
                    } else {
                        GroupsList(
                            groups = groups,
                            onGroupClick = onGroupClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsList(groups: List<Grupo>, onGroupClick: (Grupo) -> Unit, onEditClick: (Grupo) -> Unit = {}, onDeleteClick: (Grupo) -> Unit = {}) {
    if (groups.isEmpty()) {
        EmptyListMessage(message = "No hay grupos disponibles para este curso")
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEachIndexed { _, group ->
                AppCard(
                    title = "Grupo ${group.numeroGrupo}",
                    subtitle = "Horario: ${group.horario ?: "No definido"}",
                    onClick = { onGroupClick(group) }
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
