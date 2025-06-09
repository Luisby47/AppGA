package com.example.appga.ui.screens.enrollment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.*
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.StudentViewModel
import com.example.appga.viewmodel.TermViewModel

@Composable
fun EnrollmentScreen(
    studentId: String,
    onBackClick: () -> Unit,
    studentViewModel: StudentViewModel = viewModel(),
    termViewModel: TermViewModel = viewModel()
) {
    val studentState by studentViewModel.studentState.collectAsState()
    val studentEnrollmentsState by studentViewModel.studentEnrollmentsState.collectAsState()
    val activeTermState by termViewModel.activeTermState.collectAsState()
    
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var selectedTerm by remember { mutableStateOf<Ciclo?>(null) }
    
    // Get the active term when the screen loads
    LaunchedEffect(Unit) {
        studentViewModel.getStudentById(studentId.toInt())
        termViewModel.loadActiveTerm()
    }
    
    // Set the selected term to active term when it loads
    LaunchedEffect(activeTermState) {
        if (activeTermState is Resource.Success) {
            selectedTerm = (activeTermState as Resource.Success<Ciclo>).data
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Matrícula de Estudiante",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (studentState is Resource.Success) {
                FloatingActionButton(
                    onClick = { showAddCourseDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Student info
            when (studentState) {
                is Resource.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                is Resource.Error -> {
                    val errorMessage = (studentState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    val student = (studentState as Resource.Success<Alumno>).data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = student.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Cédula: ${student.cedula}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (student.codigoCarrera != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Carrera: ${student.codigoCarrera}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // Term selector
            when (activeTermState) {
                is Resource.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                is Resource.Error -> {
                    val errorMessage = (activeTermState as Resource.Error).message
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error al cargar período: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is Resource.Success -> {
                    val activeTerm = (activeTermState as Resource.Success<Ciclo>).data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Período:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        Text(
                            text = "${activeTerm.anio}-${activeTerm.numero}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        FilledTonalButton(
                            onClick = { /* Show term selection dialog - not implemented in this version */ }
                        ) {
                            Text("Cambiar Período")
                        }
                    }
                }
            }
            
            // Current enrollments
            Text(
                text = "Matrículas Actuales",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            when (studentEnrollmentsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (studentEnrollmentsState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar matrículas: $errorMessage")
                }
                is Resource.Success -> {
                    val allEnrollments = (studentEnrollmentsState as Resource.Success<List<MatriculaWithDetails>>).data
                    
                    // Filter enrollments for the selected term
                    val currentTermEnrollments = if (selectedTerm != null) {
                        allEnrollments.filter { enrollment ->
                            enrollment.ciclo?.anio == selectedTerm?.anio &&
                            enrollment.ciclo?.numero == selectedTerm?.numero
                        }
                    } else {
                        allEnrollments
                    }
                    
                    if (currentTermEnrollments.isEmpty()) {
                        EmptyListMessage(message = "No hay matrículas para este período")
                    } else {
                        CurrentEnrollmentsList(
                            enrollments = currentTermEnrollments,
                            onDeleteEnrollment = { enrollment ->
                                studentId.toIntOrNull()?.let { studentIdInt ->
                                    enrollment.matricula.id?.let { enrollmentId ->
                                        studentViewModel.removeEnrollment(enrollmentId, studentIdInt)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Add Course Dialog (simplified - in a real app, this would involve selecting from available groups)
        if (showAddCourseDialog) {
            AlertDialog(
                onDismissRequest = { showAddCourseDialog = false },
                title = { Text("Agregar Curso") },
                text = { 
                    Text("En una implementación real, aquí se mostraría un selector de grupos disponibles para el estudiante.") 
                },
                confirmButton = {
                    TextButton(onClick = { showAddCourseDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@Composable
fun CurrentEnrollmentsList(
    enrollments: List<MatriculaWithDetails>,
    onDeleteEnrollment: (MatriculaWithDetails) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(enrollments) { enrollment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${enrollment.curso?.codigo} - ${enrollment.curso?.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Grupo ${enrollment.grupo?.numeroGrupo} | Créditos: ${enrollment.curso?.creditos}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (enrollment.grupo?.horario != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Horario: ${enrollment.grupo.horario}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (enrollment.matricula.nota != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nota: ${enrollment.matricula.nota}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (enrollment.matricula.nota >= 70) 
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Only allow removing if there's no grade yet
                    if (enrollment.matricula.nota == null) {
                        IconButton(onClick = { onDeleteEnrollment(enrollment) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Matrícula",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
