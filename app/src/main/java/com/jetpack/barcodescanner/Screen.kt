package com.jetpack.barcodescanner

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object CameraPreviewScreen : Screen("camera_preview_screen")
    object DetailScreen : Screen("detail_screen")
    object ConfirmScreen : Screen("confirm_screen")
    object StartScreen : Screen("start_screen")
    object StopScreen : Screen("stop_screen")
    object CategorySubmitScreen : Screen("category_submit_screen")
    object StopSubmitScreen : Screen("stop_submit_screen")
    object PostActivityScreen : Screen("post_activity_screen")
    object SubmissionSuccessfulScreen : Screen("submission_successful_screen")
    object ListRunningMesinScreen: Screen("list_running_mesin_screen")


    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
