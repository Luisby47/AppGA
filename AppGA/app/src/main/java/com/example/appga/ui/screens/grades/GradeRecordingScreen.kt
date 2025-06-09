package com.example.appga.ui.screens.grades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.*
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.CourseViewModel
import com.example.appga.viewmodel.InstructorViewModel
import com.example.appga.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeRecordingScreen(
    groupId: Int,
    onBackClick: () -> Unit,
    instructorViewModel: InstructorViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel(),
    studentViewModel: StudentViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // In a real implementation, we would fetch the specific group and its enrollments
    // For this demo, we'll create some placeholder data
    
    var groupWithDetails by remember { mutableStateOf<GrupoWithDetails?>(null) }
    var enrollments by remember { mutableStateOf<List<MatriculaWithStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Map to store grade edits
    val gradeEdits = remember { mutableStateMapOf<Int, Int>() }
    
    // Simulate loading group and enrollments data
    LaunchedEffect(groupId) {
        // In a real implementation, we would make API calls to fetch this data
        // For the demo, we'll create some simulated data after a brief delay
        isLoading = true
        errorMessage = null
        
        try {
            // Simulate API call delay
            kotlinx.coroutines.delay(1000)
            
            // Create simulated data
            val curso = Curso(
                id = 1,
                codigo = "CS101",
                nombre = "Introducción a la Programación",
                creditos = 4,
                horasSemanales = 4
            )
            
            val grupo = Grupo(
                id = groupId,
                anio = 2025,
                numeroCiclo = "1",
                codigoCurso = "CS101",
                numeroGrupo = 1,
                horario = "Lunes y Miércoles 9:00-11:00",
                cedulaProfesor = "123456789"
            )
            
            val profesor = Profesor(
                id = 1,
                cedula = "123456789",
                nombre = "Dr. Juan Pérez",
                telefono = "8888-8888",
                email = "jperez@universidad.ac.cr"
            )
            
            // Create simulated enrollments
            val simulatedEnrollments = listOf(
                MatriculaWithStudent(
                    matricula = Matricula(
                        id = 1,
                        alumnoId = 1,
                        grupoId = groupId,
                        nota = 85
                    ),
                    alumno = Alumno(
                        id = 1,
                        cedula = "A001",
                        nombre = "Ana García",
                        email = "ana@estudiante.ac.cr",
                        fechaNacimiento = "2000-05-15"
                    )
                ),
                MatriculaWithStudent(
                    matricula = Matricula(
                        id = 2,
                        alumnoId = 2,
                        grupoId = groupId,
                        nota = null
                    ),
                    alumno = Alumno(
                        id = 2,
                        cedula = "A002",
                        nombre = "Carlos Rodríguez",
                        email = "carlos@estudiante.ac.cr",
                        fechaNacimiento = "2001-03-22"
                    )
                ),
                MatriculaWithStudent(
                    matricula = Matricula(
                        id = 3,
                        alumnoId = 3,
                        grupoId = groupId,
                        nota = 70
                    ),
                    alumno = Alumno(
                        id = 3,
                        cedula = "A003",
                        nombre = "María Fernández",
                        email = "maria@estudiante.ac.cr",
                        fechaNacimiento = "2000-11-10"
                    )
                )
            )
            
            // Initialize the map with current grades
            simulatedEnrollments.forEach { enrollment ->
                enrollment.matricula.id?.let { id ->
                    enrollment.matricula.nota?.let { nota ->
                        gradeEdits[id] = nota
                    }
                }
            }
            
            groupWithDetails = GrupoWithDetails(
                grupo = grupo,
                curso = curso,
                profesor = profesor,
                matriculas = simulatedEnrollments
            )
            
            enrollments = simulatedEnrollments
            isLoading = false
            
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error al cargar datos: ${e.message}"
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Registro de Calificaciones",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isLoading && errorMessage == null && enrollments.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        // In a real implementation, we would save the grades via API call
                        scope.launch {
                            snackbarHostState.showSnackbar("Calificaciones guardadas correctamente")
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar Calificaciones")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingIndicator()
                errorMessage != null -> EmptyListMessage(message = errorMessage!!)
                groupWithDetails == null -> EmptyListMessage(message = "No se encontró el grupo")
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Group info
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
                                    text = groupWithDetails!!.curso?.nombre ?: "Curso",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Grupo ${groupWithDetails!!.grupo.numeroGrupo} | ${groupWithDetails!!.grupo.anio}-${groupWithDetails!!.grupo.numeroCiclo}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                if (groupWithDetails!!.grupo.horario != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Horario: ${groupWithDetails!!.grupo.horario}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Profesor: ${groupWithDetails!!.profesor?.nombre ?: "No asignado"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Students and grades
                        Text(
                            text = "Estudiantes Matriculados",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (enrollments.isEmpty()) {
                            EmptyListMessage(message = "No hay estudiantes matriculados en este grupo")
                        } else {
                            StudentGradesList(
                                enrollments = enrollments,
                                gradeEdits = gradeEdits,
                                onGradeChanged = { enrollmentId, grade ->
                                    if (grade == null || (grade in 0..100)) {
                                        if (grade == null) {
                                            gradeEdits.remove(enrollmentId)
                                        } else {
                                            gradeEdits[enrollmentId] = grade
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentGradesList(
    enrollments: List<MatriculaWithStudent>,
    gradeEdits: Map<Int, Int>,
    onGradeChanged: (Int, Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(enrollments) { enrollment ->
            val enrollmentId = enrollment.matricula.id ?: return@items
            val currentGrade = gradeEdits[enrollmentId] ?: enrollment.matricula.nota
            
            var gradeText by remember(enrollmentId, currentGrade) { 
                mutableStateOf(currentGrade?.toString() ?: "") 
            }
            
            var isError by remember { mutableStateOf(false) }
            
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
                            text = enrollment.alumno?.nombre ?: "Estudiante",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Cédula: ${enrollment.alumno?.cedula ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    OutlinedTextField(
                        value = gradeText,
                        onValueChange = { value ->
                            gradeText = value
                            isError = false
                            
                            if (value.isBlank()) {
                                onGradeChanged(enrollmentId, null)
                            } else {
                                val grade = value.toIntOrNull()
                                if (grade != null && grade in 0..100) {
                                    onGradeChanged(enrollmentId, grade)
                                } else {
                                    isError = true
                                }
                            }
                        },
                        label = { Text("Nota") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text("0-100")
                            }
                        }
                    )
                }
            }
        }
    }
}
