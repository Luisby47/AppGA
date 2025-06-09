package com.example.appga.ui.screens.admin

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appga.model.Resource
import com.example.appga.model.Usuario
import com.example.appga.ui.components.AppCard
import com.example.appga.ui.components.AppTopBar
import com.example.appga.ui.components.EmptyListMessage
import com.example.appga.ui.components.LoadingIndicator
import com.example.appga.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBackClick: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val usersState by adminViewModel.usersState
    val filteredUsers by adminViewModel.filteredUsers
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<Usuario?>(null) }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Administración de Usuarios",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    userToEdit = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
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
                    adminViewModel.filterUsers(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar usuarios...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true
            )
            
            when (usersState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Error -> {
                    val errorMessage = (usersState as Resource.Error).message
                    EmptyListMessage(message = "Error: $errorMessage")
                }
                is Resource.Success -> {
                    if (filteredUsers.isEmpty()) {
                        EmptyListMessage(message = "No se encontraron usuarios")
                    } else {
                        UsersList(
                            users = filteredUsers,
                            onEditClick = { user ->
                                userToEdit = user
                                showAddEditDialog = true
                            },
                            onDeleteClick = { user ->
                                user.id?.let { userId ->
                            adminViewModel.deleteUser(
                                userId,
                                onSuccess = { /* Handle success - could show a snackbar */ },
                                onError = { errorMsg -> /* Handle error - could show a snackbar */ }
                            )
                        }
                            }
                        )
                    }
                }
            }
        }
        
        // Add/Edit Dialog
        if (showAddEditDialog) {
            UserDialog(
                user = userToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { user ->
                    if (user.id == null) {
                        adminViewModel.createUser(
                            user,
                            onSuccess = { /* Handle success */ },
                            onError = { errorMsg -> /* Handle error */ }
                        )
                    } else {
                        adminViewModel.updateUser(
                            user,
                            onSuccess = { /* Handle success */ },
                            onError = { errorMsg -> /* Handle error */ }
                        )
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun UsersList(
    users: List<Usuario>,
    onEditClick: (Usuario) -> Unit,
    onDeleteClick: (Usuario) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            AppCard(
                title = user.cedula,
                subtitle = "Rol: ${
                    when (user.rol) {
                        "admin" -> "Administrador"
                        "registrador" -> "Registrador"
                        "profesor" -> "Profesor"
                        "alumno" -> "Estudiante"
                        else -> user.rol
                    }
                }",
                onClick = { onEditClick(user) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEditClick(user) }) {
                        Text("Editar")
                    }
                    
                    TextButton(onClick = { onDeleteClick(user) }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDialog(
    user: Usuario?,
    onDismiss: () -> Unit,
    onSave: (Usuario) -> Unit
) {
    val isNewUser = user == null
    val title = if (isNewUser) "Agregar Usuario" else "Editar Usuario"
    
    var cedula by remember { mutableStateOf(user?.cedula ?: "") }
    var clave by remember { mutableStateOf("") } // Never show the actual password
    var rol by remember { mutableStateOf(user?.rol ?: "registrador") }
    
    var cedulaError by remember { mutableStateOf(false) }
    var claveError by remember { mutableStateOf(false) }
    
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
                    value = cedula,
                    onValueChange = { 
                        cedula = it
                        cedulaError = it.isBlank()
                    },
                    label = { Text("Cédula / Usuario") },
                    isError = cedulaError,
                    supportingText = { if (cedulaError) Text("Campo requerido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = isNewUser // Don't allow changing the ID for existing users
                )
                
                OutlinedTextField(
                    value = clave,
                    onValueChange = { 
                        clave = it
                        claveError = it.isBlank() && isNewUser
                    },
                    label = { Text(if (isNewUser) "Contraseña" else "Nueva Contraseña (dejar en blanco para no cambiar)") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = claveError,
                    supportingText = { if (claveError) Text("Campo requerido para nuevos usuarios") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                Text(
                    text = "Rol",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                Column {
                    listOf("admin", "registrador").forEach { rolOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = rol == rolOption,
                                onClick = { rol = rolOption }
                            )
                            
                            Text(
                                text = when (rolOption) {
                                    "admin" -> "Administrador"
                                    "registrador" -> "Registrador"
                                    else -> rolOption
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    cedulaError = cedula.isBlank()
                    claveError = clave.isBlank() && isNewUser
                    
                    if (!cedulaError && !claveError) {
                        val updatedUser = Usuario(
                            id = user?.id,
                            cedula = cedula,
                            clave = if (clave.isBlank() && !isNewUser) user?.clave ?: "" else clave,
                            rol = rol
                        )
                        
                        onSave(updatedUser)
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
