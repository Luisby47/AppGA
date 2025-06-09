package com.example.appga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appga.ui.navigation.AppNavHost
import com.example.appga.ui.navigation.Routes
import com.example.appga.ui.theme.AppGATheme
import com.example.appga.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val requiredRoles: List<String> = listOf("admin", "profesor", "alumno", "registrador")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppGATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AcademicManagementApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicManagementApp(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val userRole = currentUser?.rol ?: "admin" // Default to admin to show all options
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val drawerItems = listOf(
        DrawerItem(
            title = "Cursos",
            icon = Icons.Default.Book,
            route = Routes.COURSES,
            requiredRoles = listOf("admin", "profesor", "registrador", "alumno")
        ),
        DrawerItem(
            title = "Programas",
            icon = Icons.Default.School,
            route = Routes.PROGRAMS,
            requiredRoles = listOf("admin", "registrador", "alumno")
        ),
        DrawerItem(
            title = "Profesores",
            icon = Icons.Default.Person,
            route = Routes.INSTRUCTORS,
            requiredRoles = listOf("admin", "registrador", "alumno")
        ),
        DrawerItem(
            title = "Estudiantes",
            icon = Icons.Default.People,
            route = Routes.STUDENTS,
            requiredRoles = listOf("admin", "profesor", "registrador")
        ),
        DrawerItem(
            title = "Períodos",
            icon = Icons.Default.DateRange,
            route = Routes.TERMS,
            requiredRoles = listOf("admin", "registrador", "alumno")
        ),
        DrawerItem(
            title = "Administración",
            icon = Icons.Default.Settings,
            route = Routes.ADMIN,
            requiredRoles = listOf("admin")
        ),
        DrawerItem(
            title = "Mi Historial",
            icon = Icons.Default.History,
            route = Routes.STUDENT_HISTORY.replace("{studentId}", currentUser?.cedula ?: ""),
            requiredRoles = listOf("alumno")
        ),
        DrawerItem(
            title = "Mis Cursos",
            icon = Icons.Default.List,
            route = Routes.INSTRUCTOR_COURSES,
            requiredRoles = listOf("profesor")
        )
    ).filter { item -> 
        userRole in item.requiredRoles
    }
    
    val isInLoginScreen = currentDestination?.route == Routes.LOGIN
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (!isInLoginScreen) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Sistema de Gestión Académica",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    Divider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        label = { Text("Cerrar Sesión") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                            authViewModel.logout()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        },
        gesturesEnabled = !isInLoginScreen
    ) {
        Scaffold(
            topBar = {
                if (!isInLoginScreen) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = when (currentDestination?.route) {
                                    Routes.HOME -> "Inicio"
                                    Routes.COURSES -> "Cursos"
                                    Routes.PROGRAMS -> "Programas"
                                    Routes.INSTRUCTORS -> "Profesores"
                                    Routes.STUDENTS -> "Estudiantes"
                                    Routes.TERMS -> "Períodos"
                                    Routes.ADMIN -> "Administración"
                                    else -> "Sistema de Gestión Académica"
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                                authViewModel.logout()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        ) { paddingValues ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                authViewModel = authViewModel
            )
        }
    }
}