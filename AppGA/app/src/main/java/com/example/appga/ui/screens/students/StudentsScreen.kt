package com.example.appga.ui.screens.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Alumno
import com.example.appga.model.Resource
import com.example.appga.ui.components.*
import com.example.appga.viewmodel.StudentFilterType
import com.example.appga.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onStudentClick: (String) -> Unit,
    onBackClick: () -> Unit,
    studentViewModel: StudentViewModel = viewModel()
) {
    val studentsState by studentViewModel.studentsState.collectAsState()
    val filteredStudents by studentViewModel.filteredStudents.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(StudentFilterType.NAME) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Alumno?>(null) }
    
    LaunchedEffect(Unit) {
        studentViewModel.loadAllStudents()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Estudiantes",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    studentToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Agregar Estudiante",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda moderna
            ModernSearchBar(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    studentViewModel.filterStudents(it, filterType)
                },
                placeholder = "Buscar estudiantes..."
            )
            
            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar por:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                FilterChip(
                    onClick = { showFilterDialog = true },
                    label = { 
                        Text(when (filterType) {
                            StudentFilterType.NAME -> "Nombre"
                            StudentFilterType.ID -> "Cédula"
                            StudentFilterType.MAJOR -> "Carrera"
                        })
                    },
                    selected = true,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            
            when (studentsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (studentsState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredStudents.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron estudiantes")
                    } else {
                        ModernStudentsList(
                            students = filteredStudents,
                            onStudentClick = { student ->
                                student.id?.let { onStudentClick(it.toString()) }
                            },
                            onEditClick = { student ->
                                studentToEdit = student
                                showAddEditDialog = true
                            },
                            onDeleteClick = { student ->
                                student.id?.let { studentViewModel.deleteStudent(it) }
                            }
                        )
                    }
                }
            }
        }
        
        // Diálogos
        if (showFilterDialog) {
            ModernFilterDialog(
                filterType = filterType,
                onFilterSelected = { type ->
                    filterType = type
                    studentViewModel.filterStudents(searchQuery, type)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
        
        if (showAddEditDialog) {
            StudentDialog(
                student = studentToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { student ->
                    if (student.id == null) {
                        studentViewModel.createStudent(student)
                    } else {
                        studentViewModel.updateStudent(student.id, student)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ModernStudentsList(
    students: List<Alumno>,
    onStudentClick: (Alumno) -> Unit,
    onEditClick: (Alumno) -> Unit,
    onDeleteClick: (Alumno) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students) { student ->
            ModernCard(
                title = student.nombre,
                subtitle = "Cédula: ${student.cedula} • ${student.email}",
                icon = Icons.Default.Person,
                onClick = { onStudentClick(student) }
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                
                if (student.codigoCarrera != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Carrera: ${student.codigoCarrera}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onEditClick(student) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Editar", fontWeight = FontWeight.Medium)
                    }
                    
                    TextButton(
                        onClick = { onDeleteClick(student) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernFilterDialog(
    filterType: StudentFilterType,
    onFilterSelected: (StudentFilterType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Filtrar por",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                StudentFilterType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = filterType == type,
                            onClick = { onFilterSelected(type) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Text(
                            text = when (type) {
                                StudentFilterType.NAME -> "Nombre"
                                StudentFilterType.ID -> "Cédula"
                                StudentFilterType.MAJOR -> "Carrera"
                            },
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// Mantener el StudentDialog original pero con estilos modernos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDialog(
    student: Alumno?,
    onDismiss: () -> Unit,
    onSave: (Alumno) -> Unit
) {
    val isNewStudent = student == null
    val title = if (isNewStudent) "Agregar Estudiante" else "Editar Estudiante"
    
    var cedula by remember { mutableStateOf(student?.cedula ?: "") }
    var nombre by remember { mutableStateOf(student?.nombre ?: "") }
    var telefono by remember { mutableStateOf(student?.telefono ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var fechaNacimiento by remember { mutableStateOf(student?.fechaNacimiento ?: "") }
    var codigoCarrera by remember { mutableStateOf(student?.codigoCarrera ?: "") }
    
    var cedulaError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var fechaNacimientoError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ModernTextField(
                    value = cedula,
                    onValueChange = { 
                        cedula = it
                        cedulaError = it.isBlank()
                    },
                    label = "Cédula",
                    isError = cedulaError,
                    errorMessage = "Campo requerido",
                    modifier = Modifier.fillMaxWidth()
                )
                
                ModernTextField(
                    value = nombre,
                    onValueChange = { 
                        nombre = it
                        nombreError = it.isBlank()
                    },
                    label = "Nombre",
                    isError = nombreError,
                    errorMessage = "Campo requerido",
                    modifier = Modifier.fillMaxWidth()
                )
                
                ModernTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono",
                    modifier = Modifier.fillMaxWidth()
                )
                
                ModernTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = it.isBlank() || !it.contains("@")
                    },
                    label = "Email",
                    isError = emailError,
                    errorMessage = "Email inválido",
                    modifier = Modifier.fillMaxWidth()
                )
                
                ModernTextField(
                    value = fechaNacimiento,
                    onValueChange = { 
                        fechaNacimiento = it
                        fechaNacimientoError = it.isBlank()
                    },
                    label = "Fecha de Nacimiento (YYYY-MM-DD)",
                    isError = fechaNacimientoError,
                    errorMessage = "Formato: YYYY-MM-DD",
                    modifier = Modifier.fillMaxWidth()
                )
                
                ModernTextField(
                    value = codigoCarrera,
                    onValueChange = { codigoCarrera = it },
                    label = "Código de Carrera",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ModernButton(
                text = "Guardar",
                onClick = {
                    cedulaError = cedula.isBlank()
                    nombreError = nombre.isBlank()
                    emailError = email.isBlank() || !email.contains("@")
                    fechaNacimientoError = fechaNacimiento.isBlank()
                    
                    if (!cedulaError && !nombreError && !emailError && !fechaNacimientoError) {
                        val updatedStudent = Alumno(
                            id = student?.id,
                            cedula = cedula,
                            nombre = nombre,
                            telefono = telefono.ifBlank { null },
                            email = email,
                            fechaNacimiento = fechaNacimiento,
                            codigoCarrera = codigoCarrera.ifBlank { null }
                        )
                        
                        onSave(updatedStudent)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
