package com.example.appga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appga.ui.screens.admin.AdminScreen
import com.example.appga.ui.screens.courses.CourseDetailScreen
import com.example.appga.ui.screens.courses.CoursesScreen
import com.example.appga.ui.screens.enrollment.EnrollmentScreen
import com.example.appga.ui.screens.grades.GradeRecordingScreen
import com.example.appga.ui.screens.home.HomeScreen
import com.example.appga.ui.screens.instructors.InstructorDetailScreen
import com.example.appga.ui.screens.instructors.InstructorsScreen
import com.example.appga.ui.screens.login.LoginScreen
import com.example.appga.ui.screens.programs.ProgramDetailScreen
import com.example.appga.ui.screens.programs.ProgramsScreen
import com.example.appga.ui.screens.students.StudentDetailScreen
import com.example.appga.ui.screens.students.StudentHistoryScreen
import com.example.appga.ui.screens.students.StudentsScreen
import com.example.appga.ui.screens.terms.TermsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    
    // Course screens
    object Courses : Screen("courses")
    object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    
    // Program screens
    object Programs : Screen("programs")
    object ProgramDetail : Screen("program/{programId}") {
        fun createRoute(programId: String) = "program/$programId"
    }
    
    // Instructor screens
    object Instructors : Screen("instructors")
    object InstructorDetail : Screen("instructor/{instructorId}") {
        fun createRoute(instructorId: String) = "instructor/$instructorId"
    }
    object InstructorCourses : Screen("instructor/courses")
    
    // Student screens
    object Students : Screen("students")
    object StudentDetail : Screen("student/{studentId}") {
        fun createRoute(studentId: String) = "student/$studentId"
    }
    object StudentHistory : Screen("student/{studentId}/history") {
        fun createRoute(studentId: String) = "student/$studentId/history"
    }
    
    // Term screens
    object Terms : Screen("terms")
    
    // Enrollment screens
    object Enrollment : Screen("enrollment/{studentId}") {
        fun createRoute(studentId: String) = "enrollment/$studentId"
    }
    
    // Grade Recording screens
    object GradeRecording : Screen("grades/{groupId}") {
        fun createRoute(groupId: String) = "grades/$groupId"
    }
    
    // Admin screens
    object Admin : Screen("admin")
}

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    authViewModel: com.example.appga.viewmodel.AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { userRole ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(authViewModel)
        }
        
        // Course routes
        composable(Screen.Courses.route) {
            CoursesScreen(
                onCourseClick = { courseId ->
                    navController.navigate(Screen.CourseDetail.createRoute(courseId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseIdStr = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseId = courseIdStr.toIntOrNull() ?: 0
            CourseDetailScreen(
                courseId = courseId,
                onBackClick = { navController.popBackStack() },
                onGroupClick = { group ->
                    group.id?.let { navController.navigate(Screen.GradeRecording.createRoute(it.toString())) }
                }
            )
        }
        
        // Program routes
        composable(Screen.Programs.route) {
            ProgramsScreen(
                onProgramClick = { programId ->
                    navController.navigate(Screen.ProgramDetail.createRoute(programId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ProgramDetail.route,
            arguments = listOf(navArgument("programId") { type = NavType.StringType })
        ) { backStackEntry ->
            val programId = backStackEntry.arguments?.getString("programId") ?: ""
            ProgramDetailScreen(
                programId = programId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Instructor routes
        composable(Screen.Instructors.route) {
            InstructorsScreen(
                onInstructorClick = { instructorId ->
                    navController.navigate(Screen.InstructorDetail.createRoute(instructorId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.InstructorDetail.route,
            arguments = listOf(navArgument("instructorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val instructorId = backStackEntry.arguments?.getString("instructorId") ?: ""
            InstructorDetailScreen(
                instructorId = instructorId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Instructor Courses route
        composable(Screen.InstructorCourses.route) {
            CoursesScreen(
                onCourseClick = { courseId -> 
                    navController.navigate(Screen.CourseDetail.createRoute(courseId))
                },
                onBackClick = { navController.popBackStack() },
                filterByInstructor = true
            )
        }
        
        // Student routes
        composable(Screen.Students.route) {
            StudentsScreen(
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentDetail.createRoute(studentId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.StudentDetail.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentDetailScreen(
                studentId = studentId,
                onViewHistoryClick = {
                    navController.navigate(Screen.StudentHistory.createRoute(studentId))
                },
                onEnrollClick = {
                    navController.navigate(Screen.Enrollment.createRoute(studentId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.StudentHistory.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentHistoryScreen(
                studentId = studentId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Term routes
        composable(Screen.Terms.route) {
            TermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Enrollment routes
        composable(
            route = Screen.Enrollment.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            EnrollmentScreen(
                studentId = studentId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Grade Recording routes
        composable(
            route = Screen.GradeRecording.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupIdStr = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupId = groupIdStr.toIntOrNull() ?: 0
            GradeRecordingScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Admin routes
        composable(Screen.Admin.route) {
            AdminScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
