package com.example.appga.ui.screens.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Alumno
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.StudentViewModel

@Composable
fun StudentDetailScreen(
    studentId: String,
    onViewHistoryClick: () -> Unit,
    onEnrollClick: () -> Unit,
    onBackClick: () -> Unit,
    studentViewModel: StudentViewModel = viewModel()
) {
    val studentState by studentViewModel.studentState.collectAsState()
    val studentWithDetailsState by studentViewModel.studentWithDetailsState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Alumno?>(null) }
    
    LaunchedEffect(studentId) {
        studentViewModel.getStudentById(studentId.toInt())
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del Estudiante",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (studentState is Resource.Success) {
                FloatingActionButton(
                    onClick = {
                        studentToEdit = (studentState as Resource.Success<Alumno>).data
                        showEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Estudiante")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (studentState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (studentState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    val student = (studentState as Resource.Success<Alumno>).data
                    StudentDetailContent(
                        student = student,
                        studentWithDetailsState = studentWithDetailsState,
                        onViewHistoryClick = onViewHistoryClick,
                        onEnrollClick = onEnrollClick
                    )
                }
            }
        }
        
        // Edit Dialog
        if (showEditDialog && studentToEdit != null) {
            StudentDialog(
                student = studentToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { student ->
                    student.id?.let { studentViewModel.updateStudent(it, student) }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun StudentDetailContent(
    student: Alumno,
    studentWithDetailsState: Resource<com.example.appga.model.AlumnoWithEnrollment>,
    onViewHistoryClick: () -> Unit,
    onEnrollClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student info card
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
                        text = student.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cédula: ${student.cedula}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (!student.telefono.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Teléfono: ${student.telefono}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Email: ${student.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Fecha de Nacimiento: ${student.fechaNacimiento}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (!student.codigoCarrera.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val carreraNombre = if (studentWithDetailsState is Resource.Success) {
                            (studentWithDetailsState as Resource.Success).data.carreraNombre ?: "No disponible"
                        } else {
                            "Cargando..."
                        }
                        
                        Text(
                            text = "Carrera: ${student.codigoCarrera} - $carreraNombre",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Actions buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onViewHistoryClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver Historial Académico")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = onEnrollClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Matrícula")
                }
            }
        }
        
        // Enrollments summary section
        item {
            Text(
                text = "Resumen de Matrículas Actuales",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Display current enrollments summary
        item {
            when (studentWithDetailsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (studentWithDetailsState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar matrículas: $errorMessage")
                }
                is Resource.Success -> {
                    val enrollments = (studentWithDetailsState as Resource.Success).data.matriculas
                    
                    if (enrollments.isEmpty()) {
                        EmptyListMessage(message = "No hay matrículas actuales")
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Just show a summary count for now
                            Text("Total de matrículas: ${enrollments.size}")
                            Text("Ver historial académico para más detalles")
                        }
                    }
                }
            }
        }
    }
}
