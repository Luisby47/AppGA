package com.example.appga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    
    // Courses
    const val COURSES = "courses"
    const val COURSE_DETAIL = "courses/{courseId}"
    
    // Programs
    const val PROGRAMS = "programs"
    const val PROGRAM_DETAIL = "programs/{programId}"
    
    // Instructors
    const val INSTRUCTORS = "instructors"
    const val INSTRUCTOR_DETAIL = "instructors/{instructorId}"
    const val INSTRUCTOR_COURSES = "instructor/courses"
    
    // Students
    const val STUDENTS = "students"
    const val STUDENT_DETAIL = "students/{studentId}"
    const val STUDENT_HISTORY = "students/{studentId}/history"
    
    // Terms
    const val TERMS = "terms"
    
    // Enrollment
    const val ENROLLMENT = "enrollment/{studentId}"
    
    // Grades
    const val GRADE_RECORDING = "grades/{groupId}"
    
    // Administration
    const val ADMIN = "admin"
    
    // Helper functions to generate routes with parameters
    fun courseDetail(courseId: Int) = "courses/$courseId"
    fun programDetail(programId: String) = "programs/$programId"
    fun instructorDetail(instructorId: String) = "instructors/$instructorId"
    fun studentDetail(studentId: String) = "students/$studentId"
    fun studentHistory(studentId: String) = "students/$studentId/history"
    fun enrollment(studentId: String) = "enrollment/$studentId"
    fun gradeRecording(groupId: Int) = "grades/$groupId"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN,
    modifier: Modifier = Modifier,
    authViewModel: com.example.appga.viewmodel.AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(Routes.HOME) {
            HomeScreen(authViewModel = authViewModel)
        }
        
        // Courses Screens
        composable(Routes.COURSES) {
            CoursesScreen(
                onBackClick = { navController.navigateUp() },
                onCourseClick = { courseId -> 
                    navController.navigate(Routes.courseDetail(courseId.toInt()))
                }
            )
        }
        
        composable(
            route = Routes.COURSE_DETAIL,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: return@composable
            
            CourseDetailScreen(
                courseId = courseId,
                onBackClick = { navController.navigateUp() },
                onGroupClick = { group ->
                    group.id?.let { navController.navigate(Routes.gradeRecording(it)) }
                }
            )
        }
        
        // Programs Screens
        composable(Routes.PROGRAMS) {
            ProgramsScreen(
                onBackClick = { navController.navigateUp() },
                onProgramClick = { programId ->
                    navController.navigate(Routes.programDetail(programId.toString()))
                }
            )
        }
        
        composable(
            route = Routes.PROGRAM_DETAIL,
            arguments = listOf(navArgument("programId") { type = NavType.StringType })
        ) { backStackEntry ->
            val programId = backStackEntry.arguments?.getString("programId") ?: return@composable
            
            ProgramDetailScreen(
                programId = programId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Instructors Screens
        composable(Routes.INSTRUCTORS) {
            InstructorsScreen(
                onBackClick = { navController.navigateUp() },
                onInstructorClick = { instructorId ->
                    navController.navigate(Routes.instructorDetail(instructorId.toString()))
                }
            )
        }
        
        composable(
            route = Routes.INSTRUCTOR_DETAIL,
            arguments = listOf(navArgument("instructorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val instructorId = backStackEntry.arguments?.getString("instructorId") ?: return@composable
            
            InstructorDetailScreen(
                instructorId = instructorId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Students Screens
        composable(Routes.STUDENTS) {
            StudentsScreen(
                onBackClick = { navController.navigateUp() },
                onStudentClick = { studentId ->
                    navController.navigate(Routes.studentDetail(studentId.toString()))
                }
            )
        }
        
        composable(
            route = Routes.STUDENT_DETAIL,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            
            StudentDetailScreen(
                studentId = studentId,
                onBackClick = { navController.navigateUp() },
                onViewHistoryClick = {
                    navController.navigate(Routes.studentHistory(studentId))
                },
                onEnrollClick = {
                    navController.navigate(Routes.enrollment(studentId))
                }
            )
        }
        
        composable(
            route = Routes.STUDENT_HISTORY,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            
            StudentHistoryScreen(
                studentId = studentId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Terms Screen
        composable(Routes.TERMS) {
            TermsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Enrollment Screen
        composable(
            route = Routes.ENROLLMENT,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            
            EnrollmentScreen(
                studentId = studentId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Grade Recording Screen
        composable(
            route = Routes.GRADE_RECORDING,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: return@composable
            
            GradeRecordingScreen(
                groupId = groupId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Administration Screen
        composable(Routes.ADMIN) {
            AdminScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Instructor Courses
        composable(Routes.INSTRUCTOR_COURSES) {
            // TODO: Create an InstructorCoursesScreen component
            // For now, redirect to courses
            CoursesScreen(
                onBackClick = { navController.navigateUp() },
                onCourseClick = { courseId -> 
                    navController.navigate(Routes.courseDetail(courseId.toInt()))
                },
                filterByInstructor = true
            )
        }
    }
}
