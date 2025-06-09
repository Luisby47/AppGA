package com.example.appga.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Alumno
import com.example.appga.model.MatriculaWithDetails
import com.example.appga.model.Resource
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.StudentViewModel

@Composable
fun StudentHistoryScreen(
    studentId: String,
    onBackClick: () -> Unit,
    studentViewModel: StudentViewModel = viewModel()
) {
    val studentState by studentViewModel.studentState.collectAsState()
    val studentEnrollmentsState by studentViewModel.studentEnrollmentsState.collectAsState()
    
    LaunchedEffect(studentId) {
        studentViewModel.getStudentById(studentId.toInt())
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Historial Académico",
                onBackClick = onBackClick
            )
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
                    StudentHistoryContent(
                        student = student,
                        enrollmentsState = studentEnrollmentsState
                    )
                }
            }
        }
    }
}

@Composable
fun StudentHistoryContent(
    student: Alumno,
    enrollmentsState: Resource<List<MatriculaWithDetails>>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student basic info
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
                    
                    if (!student.codigoCarrera.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Carrera: ${student.codigoCarrera}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Academic summary
        item {
            Text(
                text = "Resumen Académico",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Academic summary calculation
        item {
            when (enrollmentsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (enrollmentsState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar matrículas: $errorMessage")
                }
                is Resource.Success -> {
                    val enrollments = (enrollmentsState as Resource.Success<List<MatriculaWithDetails>>).data
                    
                    if (enrollments.isEmpty()) {
                        EmptyListMessage(message = "No hay historial académico disponible")
                    } else {
                        // Calculate academic stats
                        val completedCourses = enrollments.filter { it.matricula.nota != null }
                        val totalCredits = completedCourses.sumOf { it.curso?.creditos ?: 0 }
                        val weightedSum = completedCourses.sumOf { (it.matricula.nota ?: 0) * (it.curso?.creditos ?: 0) }
                        val gpa = if (totalCredits > 0) weightedSum.toFloat() / totalCredits else 0f
                        
                        // Display academic summary
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cursos Matriculados: ${enrollments.size}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Cursos Completados: ${completedCourses.size}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Créditos Aprobados: $totalCredits",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Promedio: ${String.format("%.2f", gpa)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Courses history section header
        item {
            Text(
                text = "Historial por Ciclo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Courses by term
        when (enrollmentsState) {
            is Resource.Loading -> {
                item {
                    LoadingIndicator()
                }
            }
            is Resource.Error -> {
                item {
                    val errorMessage = (enrollmentsState as Resource.Error).message
                    EmptyListMessage(message = "Error al cargar matrículas: $errorMessage")
                }
            }
            is Resource.Success -> {
                val enrollments = (enrollmentsState as Resource.Success<List<MatriculaWithDetails>>).data
                
                if (enrollments.isEmpty()) {
                    item {
                        EmptyListMessage(message = "No hay historial académico disponible")
                    }
                } else {
                    // Group enrollments by term
                    val enrollmentsByTerm = enrollments.groupBy { 
                        "${it.ciclo?.anio ?: "N/A"}-${it.ciclo?.numero ?: "N/A"}"
                    }
                    
                    // Headers
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Curso",
                                modifier = Modifier.weight(0.4f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Créditos",
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Grupo",
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nota",
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Display enrollments grouped by term
                    enrollmentsByTerm.forEach { (term, termsEnrollments) ->
                        item {
                            Text(
                                text = "Ciclo: $term",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        items(termsEnrollments) { enrollment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${enrollment.curso?.codigo ?: "N/A"} - ${enrollment.curso?.nombre ?: "N/A"}",
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = "${enrollment.curso?.creditos ?: "N/A"}",
                                    modifier = Modifier.weight(0.2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${enrollment.grupo?.numeroGrupo ?: "N/A"}",
                                    modifier = Modifier.weight(0.2f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${enrollment.matricula.nota ?: "N/A"}",
                                    modifier = Modifier.weight(0.2f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = if ((enrollment.matricula.nota ?: 0) >= 70) 
                                        MaterialTheme.colorScheme.primary
                                    else if (enrollment.matricula.nota != null)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
