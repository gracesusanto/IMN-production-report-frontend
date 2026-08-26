package com.jetpack.barcodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.common.util.concurrent.ListenableFuture
import com.jetpack.barcodescanner.ui.ImnViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class Category {
    TOOLING, MESIN, OPERATOR, INVALID
}

enum class MesinStatus {
    RUNNING, IDLE, SETUP
}

// Create a list of cities
val mKategoriList = listOf(
    "BR : Briefing",
    "BT : Breaktime",
    "NP : No Plan",
    "TL : Trial",
    "TS : Tooling Setting",
    "TP : Tooling Problem",
    "MP : Machine Problem",
    "CM : Change Material",
    "QC : Quality Check",
    "NM : No Material",
    "RP : Reporting",
    "STO : Stock Opname",
    "X : Lain - lain"
)

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            BackHandler(true) {
                // Or do nothing
                Log.e("VIEW", "Click back not allowed")
            }
            MainScreen(navController = navController)
        }
        composable(route = Screen.SelectActivityScreen.route) {
            SelectActivityScreen(navController = navController)
        }
        composable(
            route = Screen.CameraPreviewScreen.route + "/{scanItem}",
            arguments = listOf(
                navArgument("scanItem") {
                    type = NavType.StringType
                    defaultValue = Category.TOOLING.name
                }
            )
        ) { entry ->
            CameraPreview(
                scanItem = entry.arguments?.getString("scanItem"),
                navController = navController
            )
        }
        composable(
            route = Screen.DetailScreen.route + "/{category}/{barCodeVal}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = Category.TOOLING.name
                    nullable = false
                },
                navArgument("barCodeVal") {
                    type = NavType.StringType
                    defaultValue = "NONE"
                    nullable = false
                }
            )
        ) { entry ->
            DetailScreen(
                category = entry.arguments?.getString("category"),
                barCodeVal = entry.arguments?.getString("barCodeVal"),
                navController = navController
            )
        }
        composable(route = Screen.ConfirmScreen.route) {
            ConfirmScreen(navController = navController)
        }
        composable(route = Screen.StartScreen.route + "/{mesinStatus}",
            arguments = listOf(
                navArgument("mesinStatus") {
                    type = NavType.StringType
                    defaultValue = "IDLE"
                }
            )
        ) { entry ->
            StartScreen(
                mesinStatus = entry.arguments?.getString("mesinStatus"),
                navController = navController
            )
        }
        composable(route = Screen.StopScreen.route) {
            StopScreen(navController = navController)
        }
        composable(
            route = Screen.CategorySubmitScreen.route + "/{rejectQty}/{reworkQty}/{keterangan}",
            arguments = listOf(
                navArgument("rejectQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("reworkQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("keterangan") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) { entry ->
            CategorySubmitScreen(
                rejectQty = entry.arguments?.getString("rejectQty"),
                reworkQty = entry.arguments?.getString("reworkQty"),
                keterangan = entry.arguments?.getString("keterangan"),
                navController = navController
            )
        }
        composable(route = Screen.StopSubmitScreen.route) {
            StopSubmitScreen(navController = navController)
        }
        composable(
            route = Screen.PostActivityScreen.route + "/{categoryDowntime}/{outputQty}/{rejectQty}/{reworkQty}/{coilNo}/{lotNo}/{packNo}/{keterangan}",
            arguments = listOf(
                navArgument("categoryDowntime") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("outputQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("rejectQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("reworkQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("coilNo") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("lotNo") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("packNo") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("keterangan") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) { entry ->
            PostActivityScreen(
                categoryDowntime = entry.arguments?.getString("categoryDowntime"),
                outputQty = entry.arguments?.getString("outputQty"),
                rejectQty = entry.arguments?.getString("rejectQty"),
                reworkQty = entry.arguments?.getString("reworkQty"),
                coilNo = entry.arguments?.getString("coilNo"),
                lotNo = entry.arguments?.getString("lotNo"),
                packNo = entry.arguments?.getString("packNo"),
                keterangan = entry.arguments?.getString("keterangan"),
                navController = navController
            )
        }
        composable(route = Screen.SubmissionSuccessfulScreen.route) {
            BackHandler(true) {
                // Or do nothing
                Log.e("VIEW", "Click back not allowed")
            }

            SubmissionSuccessfulScreen(navController = navController)
        }
        composable(route = Screen.ListRunningMesinScreen.route) {
            ListRunningMesinScreen(navController = navController)
        }
    }
}

data class OperatorMachine(
    val mesinId: String,
    val toolingId: String,
    val category: String,
    val status: String
)

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = AppDataStore(context)
    val coroutineScope = rememberCoroutineScope()

    val operator = remember { mutableStateOf("") }
    val operatorMachines = remember { mutableStateOf(emptyList<OperatorMachine>()) }
    val isCallSuccessful = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }

    fun fetchOperatorData() {
        isLoading.value = true
        coroutineScope.launch {
            dataStore.saveSelectedCategory("") // Reset category when fetching data

            val operatorId = dataStore.getSelectedOperator.firstOrNull() ?: "NONE"
            operator.value = operatorId

            API.getOperatorStatus(operatorId,
                { response ->
                    isCallSuccessful.value = true
                    isError.value = false  // Reset error flag on success
                    val machines = response.getJSONArray("machines")
                    operatorMachines.value = (0 until machines.length()).mapNotNull { i ->
                        val machine = machines.getJSONObject(i)
                        val mesinId = if (machine.isNull("mesinId")) "" else machine.optString("mesinId").trim()
                        val toolingId = if (machine.isNull("toolingId")) "" else machine.optString("toolingId").trim()

                        if (mesinId.isBlank() || toolingId.isBlank()) {
                            Log.w("API_NAVIGATION", "Ignoring active machine with missing mesin/tooling ID")
                            return@mapNotNull null
                        }

                        OperatorMachine(
                            mesinId = mesinId,
                            toolingId = toolingId,
                            category = machine.getString("category"),
                            status = machine.getString("mesinStatus")
                        )
                    }
                    isLoading.value = false
                },
                { error ->
                    isCallSuccessful.value = true
                    isError.value = true  // Set error flag when API fails
                    error.message?.let { Log.e("API_NAVIGATION", it) }
                    isLoading.value = false
                }
            )
        }
    }

    // Fetch operator data on launch
    LaunchedEffect(true) {
        fetchOperatorData()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        if (operator.value == "NONE" || operator.value.isEmpty()) {
            Button(onClick = {
                navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.OPERATOR.name))
            }) {
                Text(text = "Login")
            }
        } else {
            val operatorNameUpdated = operator.value.substring(3).replace("-", " ")
            Log.i("DataStore", operatorNameUpdated)
            Text(text = "Halo, $operatorNameUpdated!", fontSize = 25.sp)
            Spacer(modifier = Modifier.height(45.dp))

            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(12.dp))
            } else if (isCallSuccessful.value) {
                if (isError.value) {
                    Text(
                        text = "Server sedang error. Segera kontak Pak Saeful atau Pak Taryana.",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { fetchOperatorData() }) {
                        Text(text = "Coba Lagi")
                    }
                } else {
                    val hasNoopOperator = operatorMachines.value.any { it.status == "STOP" }

                    if (operatorMachines.value.isEmpty() || !hasNoopOperator) {
                        Button(onClick = {
                            coroutineScope.launch {
                                dataStore.resetSelectedStateOnly()
                                if (operatorMachines.value.isEmpty()) {
                                    navController.navigate(Screen.SelectActivityScreen.route)
                                } else {
                                    navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.TOOLING.name))
                                }
                            }
                        }) {
                            Text(text = "Mulai Aktivitas Baru")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (operatorMachines.value.isNotEmpty()) {
                        Text(text = "Pilih Mesin:", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(operatorMachines.value) { machine ->
                                val buttonText = when (machine.status) {
                                    "RUNNING" -> "Stop Running Mesin ${machine.mesinId} Tooling ${machine.toolingId}"
                                    "DOWNTIME" -> "Ganti Kategori Downtime ${machine.category} Mesin ${machine.mesinId} Tooling ${machine.toolingId}"
                                    "STOP" -> "Mulai Aktivitas Baru dan Akhiri ${machine.category}"
                                    else -> null
                                }

                                buttonText?.let {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            coroutineScope.launch {
                                                dataStore.saveSelectedMesin(machine.mesinId)
                                                dataStore.saveSelectedTooling(machine.toolingId)
                                                dataStore.saveSelectedCategory(machine.category)
                                                dataStore.saveLastMesin(machine.mesinId)
                                                dataStore.saveLastTooling(machine.toolingId)
                                                when (machine.status) {
                                                    "RUNNING" -> navController.navigate(Screen.ConfirmScreen.route)
                                                    "DOWNTIME" -> navController.navigate(Screen.ConfirmScreen.route)
                                                    "STOP" -> navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.TOOLING.name))
                                                }
                                            }
                                        }
                                    ) {
                                        Text(text = it)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.OPERATOR.name))
        }) {
            Text(text = "Ganti Akun")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { fetchOperatorData() }) {
            Text(text = "Refresh Data")
        }
    }
}


@Composable
fun SelectActivityScreen(navController: NavController) {
    val nonMachineActivities = listOf(
        "BR : Briefing",
        "BT : Breaktime",
        "NP : No Plan",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pilih Aktivitas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Create a manual grid (2 columns)
        for (rowActivities in nonMachineActivities.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowActivities.forEach { activityName ->
                    ActivityButton(activityName) {
                        navController.navigate(
                            Screen.PostActivityScreen.withArgs(
                                activityName,
                                "0",
                                "0",
                                "0",
                                "-",
                                "-",
                                "-",
                                "-"
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scan Barcode Button
        Button(
            onClick = {
                navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.TOOLING.name))
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(text = "Scan Barcode")
        }
    }
}

// Reusable Button for Activities
@Composable
fun ActivityButton(activityName: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = activityName,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun DetailScreen(
    category: String?,
    barCodeVal: String?,
    navController: NavController,
    imnViewModel: ImnViewModel = viewModel()
) {

    val detailsUiState by imnViewModel.detailsUiState.collectAsState()
    val details = detailsUiState.details
    val isCallSuccessful = detailsUiState.isCallSuccessful

    val currState: Category = Category.valueOf(category ?: Category.INVALID.name)

    val context = LocalContext.current
    val dataStore = AppDataStore(context)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        if (currState != Category.INVALID) {
            API.getDetail(
                currState,
                barCodeVal ?: "",
                { response ->
                    coroutineScope.launch {
                        when (currState) {
                            Category.TOOLING -> dataStore.saveSelectedTooling(barCodeVal ?: "")
                            Category.MESIN -> dataStore.saveSelectedMesin(barCodeVal ?: "")
                            Category.OPERATOR -> dataStore.saveSelectedOperator(barCodeVal ?: "")
                            else -> {}
                        }
                        imnViewModel.updateDetails(
                            detailText = prettifyJson(response.toString(2)),
                            isSuccessful = true
                        )
                    }
                },
                { error ->
                    imnViewModel.updateDetails(
                        detailText = "Barcode not found",
                        isSuccessful = false
                    )
                    error.message?.let { Log.e("API", it) }
                }
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = currState.name,
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
        Text(text = details)
        Spacer(Modifier.height(30.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isCallSuccessful) {
                Button(onClick = {
                    when (currState) {
                        Category.TOOLING -> navController.navigate(
                            Screen.CameraPreviewScreen.withArgs(
                                Category.MESIN.name
                            )
                        )

                        Category.MESIN -> navController.navigate(Screen.ConfirmScreen.route)
                        Category.OPERATOR -> navController.navigate(Screen.MainScreen.route)
                        else -> {
                            navController.navigate(Screen.CameraPreviewScreen.withArgs(currState.name))
                        }
                    }
                }
                ) {
                    Text(text = "Yes")
                }
                Button(onClick = {
                    navController.navigate(Screen.CameraPreviewScreen.withArgs(currState.name))
                }
                ) {
                    Text(text = "No")
                }
            } else {
                Button(onClick = {
                    navController.navigate(Screen.CameraPreviewScreen.withArgs(currState.name))
                }
                ) {
                    Text(text = "Kembali ke Scan")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ConfirmScreen(
    navController: NavController
) {
    var mesinStatus by remember { mutableStateOf(MesinStatus.IDLE) }  // Default to IDLE
    var detailText by remember { mutableStateOf("") }
    var isCallSuccessful by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isDuplicateActivity by remember { mutableStateOf(false) }

    var operatorsOnMachine by remember { mutableStateOf(emptyList<Map<String, String>>()) }
    var machinesByOperator by remember { mutableStateOf(emptyList<Map<String, String>>()) }

    val context = LocalContext.current
    val dataStore = AppDataStore(context)

    val tooling = dataStore.getSelectedTooling.collectAsState(initial = "")
    val mesin = dataStore.getSelectedMesin.collectAsState(initial = "")
    val operator = dataStore.getSelectedOperator.collectAsState(initial = "")
    val category = dataStore.getSelectedCategory.collectAsState(initial = "")

    LaunchedEffect(tooling.value, mesin.value, operator.value) {
        API.getActivityStatus(
            mesinId = mesin.value ?: "",
            operatorId = operator.value ?: "",
            toolingId = tooling.value ?: "",
            currCategory = category.value ?: "",
            ResponseListener = { response ->
                isCallSuccessful = true
                mesinStatus = MesinStatus.valueOf(response.getString("mesin_status"))
                isDuplicateActivity = false  // Reset duplicate flag on success

                if (response.has("error") && !response.isNull("error") && response.getString("error")
                        .isNotEmpty()
                ) {
                    isDuplicateActivity = true
                    detailText = response.getString("error")
                } else {
                    mesinStatus = MesinStatus.valueOf(response.getString("mesin_status"))
                }

                // Parse list of operators currently running this machine
                operatorsOnMachine =
                    (0 until response.getJSONArray("operators_on_machine").length()).map { i ->
                        val operatorData =
                            response.getJSONArray("operators_on_machine").getJSONObject(i)
                        mapOf(
                            "Operator" to operatorData.getString("operator_id"),
                            "Tooling" to operatorData.getString("tooling_id"),
                            "Kategori" to operatorData.optString("category", "None")
                        )
                    }

                // Parse list of machines this operator is running
                machinesByOperator =
                    (0 until response.getJSONArray("machines_by_operator").length()).map { i ->
                        val machineData =
                            response.getJSONArray("machines_by_operator").getJSONObject(i)
                        mapOf(
                            "Mesin" to machineData.getString("mesin_id"),
                            "Tooling" to machineData.getString("tooling_id"),
                            "Kategori" to machineData.optString("category", "None")
                        )
                    }
            },
            ErrorListener = { error ->
                isCallSuccessful = true
                isError = true

                val errorMsg = error.message ?: "Gagal memeriksa status aktivitas."
                Log.e("API", errorMsg)

                detailText = errorMsg
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Tooling: ${tooling.value}\nMesin: ${mesin.value}\nOperator: ${operator.value}",
            fontSize = 18.sp
        )

        Spacer(Modifier.height(20.dp))

        if (isCallSuccessful) {
            if (isError) {
                Text(text = detailText, color = Color.Red)
                Spacer(Modifier.height(12.dp))

                Button(onClick = {
                    navController.navigate(Screen.MainScreen.route)
                }) {
                    Text(text = "Kembali ke Halaman Utama")
                }
            } else {
                // Show warning if the operator is already running the same activity
                if (isDuplicateActivity) {
                    Text(
                        text = "Anda sedang running di mesin dan tooling ini. Mohon pilih aktivitas dari menu utama, jangan memulai aktivitas baru.",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(onClick = {
                        navController.navigate(Screen.MainScreen.route)
                    }) {
                        Text(text = "Kembali ke Halaman Utama")
                    }
                } else {
                    // Show warning if other operators are already running this machine
                    if (operatorsOnMachine.isNotEmpty()) {
                        Text(
                            "Operator lain sedang menggunakan mesin ini:",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                        operatorsOnMachine.forEach { op ->
                            // Filter out empty values and dynamically construct the details
                            val details = op.entries
                                .filter { (_, value) ->
                                    value.isNotEmpty()
                                } // Filter non-empty values
                                .joinToString(", ") { (key, value) -> "$key: $value" } // Join key-value pairs

                            if (details.isNotEmpty()) { // Only display if there are valid key-value pairs
                                Text(text = details)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

// Show warning if the operator is running other machines
                    if (machinesByOperator.isNotEmpty()) {
                        Text(
                            "Anda sedang menjalankan mesin lain:",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                        machinesByOperator.forEach { mc ->
                            // Filter out empty values and dynamically construct the details
                            val details = mc.entries
                                .filter { (_, value) ->
                                    value.isNotEmpty()
                                } // Filter non-empty values
                                .joinToString(", ") { (key, value) -> "$key: $value" } // Join key-value pairs

                            if (details.isNotEmpty()) { // Only display if there are valid key-value pairs
                                Text(text = details)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            when (mesinStatus) {
                                MesinStatus.IDLE -> navController.navigate(
                                    Screen.StartScreen.withArgs(
                                        mesinStatus.name
                                    )
                                )

                                MesinStatus.RUNNING -> navController.navigate(Screen.StopScreen.route)
                                MesinStatus.SETUP -> navController.navigate(
                                    Screen.StartScreen.withArgs(
                                        mesinStatus.name
                                    )
                                )
                            }
                        }) {
                            Text(text = "Lanjutkan")
                        }

                        Button(onClick = {
                            navController.navigate(Screen.MainScreen.route)
                        }) {
                            Text(text = "Kembali ke Halaman Utama")
                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(25.dp))
        }
    }
}


@Composable
fun CameraPreview(
    scanItem: String?,
    navController: NavController,
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    val barCodeVal = remember { mutableStateOf("") }
    var lastScanned = ""

    // State for the text input
    var textInput by remember { mutableStateOf("") }

    // Function to handle the submission
    fun handleSubmit() {
        if (textInput.isNotEmpty()) {
            if (textInput.first() != scanItem?.first()) {
                Log.e("HASIL", "INVALID BARCODE")
            } else {
                navController.navigate(
                    Screen.DetailScreen.withArgs(scanItem, textInput)
                )
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = "Scan $scanItem",
            fontSize = 27.sp,
            modifier = Modifier.padding(16.dp)
        )
        // Text Input
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Enter Barcode") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Submit Button
        Button(
            onClick = { handleSubmit() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
        AndroidView(
            factory = { AndroidViewContext ->
                PreviewView(AndroidViewContext).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier
                .clip(shape = RoundedCornerShape(20.dp))
                .weight(1f)
                .aspectRatio(1f),
            update = { previewView ->
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val barcodeAnalyser = BarCodeAnalyser { barcodes ->
                        barcodes.forEach { barcode ->
                            barcode.rawValue?.let { barcodeValue ->
                                if (barcodeValue != lastScanned) {
                                    barCodeVal.value = barcodeValue
                                    if (barcodeValue.first() != scanItem?.first()) {
                                        Log.e("HASIL", "INVALID BARCODE")
                                    } else {
                                        navController.navigate(
                                            Screen.DetailScreen.withArgs(
                                                scanItem, barcodeValue
                                            )
                                        )
                                    }
                                }
                                lastScanned = barcodeValue
                            }
                        }
                    }
                    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
        Text(barCodeVal.value)
    }
}

fun getCategoryNextState(currState: Category): Category {
    return when (currState) {
        Category.TOOLING -> Category.MESIN;
        Category.MESIN -> Category.OPERATOR;
        else -> Category.INVALID;
    }
}

@Composable
fun StartScreen(
    mesinStatus: String?,
    imnViewModel: ImnViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = AppDataStore(context)
    val coroutineScope = rememberCoroutineScope()

    val userInputState by imnViewModel.userInputState.collectAsState()
    val rejectQty = userInputState.rejectQty
    val reworkQty = userInputState.reworkQty
    val keterangan = userInputState.keterangan

    val onRejectQtyUpdate: (String) -> Unit = { imnViewModel.updateRejectQty(it) }
    val onReworkQtyUpdate: (String) -> Unit = { imnViewModel.updateReworkQty(it) }
    val onKeteranganUpdate: (String) -> Unit = { imnViewModel.updateKeterangan(it) }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        if (mesinStatus == MesinStatus.SETUP.name) {
            InputNumber(rejectQty, onRejectQtyUpdate, "qty reject")
            InputNumber(reworkQty, onReworkQtyUpdate, "qty rework")
            Spacer(modifier = Modifier.height(8.dp))
        }
        InputText(keterangan, onKeteranganUpdate, "Keterangan Tambahan")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            coroutineScope.launch {
                val selectedMesin = dataStore.getSelectedMesin.firstOrNull().orEmpty()
                val selectedTooling = dataStore.getSelectedTooling.firstOrNull().orEmpty()
                val lastMesin = dataStore.getLastMesin.firstOrNull().orEmpty()
                val lastTooling = dataStore.getLastTooling.firstOrNull().orEmpty()

                val mesinToUse = if (selectedMesin.isNotBlank()) selectedMesin else lastMesin
                val toolingToUse = if (selectedTooling.isNotBlank()) selectedTooling else lastTooling

                if (mesinToUse.isBlank() || toolingToUse.isBlank()) {
                    // force scan flow (tooling first), or show error
                    // navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.TOOLING.name))
                    return@launch
                }

                dataStore.saveSelectedMesin(mesinToUse)
                dataStore.saveSelectedTooling(toolingToUse)

                val formattedKeterangan = if (keterangan.isEmpty()) "-" else keterangan
                navController.navigate(
                    Screen.PostActivityScreen.withArgs(
                        "U : Utility",
                        "0",
                        rejectQty,
                        reworkQty,
                        "-",
                        "-",
                        "-",
                        formattedKeterangan
                    )
                )
            }
        }) {
            Text(text = "Start")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val formattedKeterangan = if (keterangan.isEmpty()) "-" else keterangan
            navController.navigate(
                Screen.CategorySubmitScreen.withArgs(
                    rejectQty,
                    reworkQty,
                    formattedKeterangan
                )
            )
        }
        ) {
            Text(text = "Ganti Kategori Downtime")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
        ) {
            Text(text = "Kembali ke Halaman Utama")
        }
    }
}

@Composable
fun StopScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        Button(onClick = {
            navController.navigate(Screen.StopSubmitScreen.route)
        }
        ) {
            Text(text = "Stop")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
        ) {
            Text(text = "Kembali ke Halaman Utama")
        }
    }
}

@Composable
fun StopSubmitScreen(
    imnViewModel: ImnViewModel = viewModel(),
    navController: NavController
) {
    val userInputState by imnViewModel.userInputState.collectAsState()
    val outputQty = userInputState.outputQty
    val rejectQty = userInputState.rejectQty
    val reworkQty = userInputState.reworkQty
    var coilNo = userInputState.coilNo
    var lotNo = userInputState.lotNo
    var packNo = userInputState.packNo
    var keterangan = userInputState.keterangan

    val onOutputQtyUpdate: (String) -> Unit = { imnViewModel.updateOutputQty(it) }
    val onRejectQtyUpdate: (String) -> Unit = { imnViewModel.updateRejectQty(it) }
    val onReworkQtyUpdate: (String) -> Unit = { imnViewModel.updateReworkQty(it) }
    val onCoilNoUpdate: (String) -> Unit = { imnViewModel.updateCoilNo(it) }
    val onLotNoUpdate: (String) -> Unit = { imnViewModel.updateLotNo(it) }
    val onPackNoUpdate: (String) -> Unit = { imnViewModel.updatePackNo(it) }
    val onKeteranganUpdate: (String) -> Unit = { imnViewModel.updateKeterangan(it) }

    val (mSelectedKategori, setSelectedState) = remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        InputNumber(outputQty, onOutputQtyUpdate, "qty output")
        InputNumber(rejectQty, onRejectQtyUpdate, "qty reject")
        InputNumber(reworkQty, onReworkQtyUpdate, "qty rework")
        InputText(coilNo, onCoilNoUpdate, "Coil no.")
        InputText(lotNo, onLotNoUpdate, "Lot no.")
        InputText(packNo, onPackNoUpdate, "Pack no.")
        InputText(keterangan, onKeteranganUpdate, "Keterangan Tambahan")

        Spacer(modifier = Modifier.height(8.dp))
        DowntimeCategoryDropdown(mSelectedKategori, setSelectedState)
        Button(
            onClick = {
                if (mSelectedKategori in mKategoriList) {
                    if (coilNo.isBlank()) {
                        coilNo = "-"
                    }
                    if (lotNo.isBlank()) {
                        lotNo = "-"
                    }
                    if (packNo.isBlank()) {
                        packNo = "-"
                    }
                    if (keterangan.isBlank()) {
                        keterangan = "-"
                    }
                    navController.navigate(
                        Screen.PostActivityScreen.withArgs(
                            mSelectedKategori,
                            outputQty,
                            rejectQty,
                            reworkQty,
                            coilNo,
                            lotNo,
                            packNo,
                            keterangan,
                        )
                    )
                }
            }
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun InputNumber(
    value: String,
    onUserChange: (String) -> Unit,
    category: String
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onUserChange,
        label = { Text("Mohon isi $category") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
    )
}

@Composable
fun InputText(
    value: String,
    onUserChange: (String) -> Unit,
    category: String
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onUserChange,
        label = { Text("Mohon isi $category") },
    )
}

@Composable
fun DowntimeCategoryDropdown(
    mSelectedText: String,
    setSelectedText: (String) -> Unit
) {
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }

    // Up Icon when expanded and down icon when collapsed
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Column(Modifier.padding(20.dp)) {

        // Create an Outlined Text Field
        // with icon and not expanded
        OutlinedTextField(
            readOnly = true,
            value = mSelectedText,
            onValueChange = { setSelectedText(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    mTextFieldSize = coordinates.size.toSize()
                },
            label = {
                if (mSelectedText in mKategoriList) {
                    Text("Kategori Tooling")
                } else {
                    Text("Kategori Tooling Invalid")
                }
            },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            },
            isError = mSelectedText !in mKategoriList,
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
        ) {
            mKategoriList.forEach { label ->
                DropdownMenuItem(onClick = {
                    setSelectedText(label)
                    mExpanded = false
                }) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
fun PostActivityScreen(
    categoryDowntime: String?,
    outputQty: String?,
    rejectQty: String?,
    reworkQty: String?,
    coilNo: String?,
    lotNo: String?,
    packNo: String?,
    keterangan: String?,
    imnViewModel: ImnViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = AppDataStore(context)
    val submissionState by imnViewModel.submissionState.collectAsStateWithLifecycle()
    var submissionPayload by remember { mutableStateOf<ActivitySubmissionPayload?>(null) }

    fun submit(payload: ActivitySubmissionPayload) {
        imnViewModel.submitActivity(
            tooling = payload.tooling,
            mesin = payload.mesin,
            operator = payload.operator,
            currCategory = payload.currCategory,
            categoryDowntime = payload.nextCategory,
            output = payload.output,
            rejectQty = payload.reject,
            reworkQty = payload.rework,
            coilNo = payload.coilNo,
            lotNo = payload.lotNo,
            packNo = payload.packNo,
            keterangan = payload.keterangan,
            navController = navController,
        )
    }

    LaunchedEffect(Unit) {
        imnViewModel.resetSubmissionState()
        val payload = ActivitySubmissionPayload(
            tooling = dataStore.getSelectedTooling.firstOrNull().orEmpty(),
            mesin = dataStore.getSelectedMesin.firstOrNull().orEmpty(),
            operator = dataStore.getSelectedOperator.firstOrNull().orEmpty(),
            currCategory = dataStore.getSelectedCategory.firstOrNull().orEmpty(),
            nextCategory = categoryDowntime.orEmpty(),
            output = outputQty.orEmpty(),
            reject = rejectQty.orEmpty(),
            rework = reworkQty.orEmpty(),
            coilNo = coilNo.orEmpty(),
            lotNo = lotNo.orEmpty(),
            packNo = packNo.orEmpty(),
            keterangan = keterangan.orEmpty(),
        )
        submissionPayload = payload
        submit(payload)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        when {
            submissionState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submitting...", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Backend URL: ${BuildConfig.BACKEND_URL}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            submissionState.isError -> {
                Text(
                    "Submission Failed",
                    fontSize = 20.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    submissionState.errorMessage,
                    fontSize = 14.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Backend URL: ${BuildConfig.BACKEND_URL}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        imnViewModel.resetSubmissionState()
                        submissionPayload?.let(::submit)
                    }
                ) {
                    Text("Retry")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        navController.navigate(Screen.MainScreen.route)
                    }
                ) {
                    Text("Back to Main")
                }
            }
            else -> {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preparing submission...", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CategorySubmitScreen(
    rejectQty: String?,
    reworkQty: String?,
    keterangan: String?,
    navController: NavController,
) {
    val (mSelectedState, setSelectedState) = remember { mutableStateOf("") }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        DowntimeCategoryDropdown(mSelectedState, setSelectedState)
        Button(
            onClick = {
                if (mSelectedState in mKategoriList) {
                    navController.navigate(
                        Screen.PostActivityScreen.withArgs(
                            mSelectedState,
                            "0",
                            rejectQty ?: "0",
                            reworkQty ?: "0",
                            "-",
                            "-",
                            "-",
                            keterangan ?: "-"
                        )
                    )
                }
            }
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun SubmissionSuccessfulScreen(
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        Text("Submission Berhasil!")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
        ) {
            Text(text = "Kembali ke halaman utama")
        }
    }
}

@Composable
fun ListRunningMesinScreen(
    navController: NavController
) {
    val listRunningMesin = remember { mutableStateOf("") }
    LaunchedEffect(true) {
        API.getListMesinStatus(
            { response -> listRunningMesin.value = response.getJSONArray("details").toString(2) },
            { error -> error.message?.let { Log.e("API", it) } }
        )
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp)

    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(11f)
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())

        ) {
            Text(text = listRunningMesin.value)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                navController.navigate(Screen.MainScreen.route)
            }, modifier = Modifier.weight(1f)
        ) {
            Text(text = "Kembali ke halaman utama")
        }
    }
}

fun prettifyJson(originalText: String): String {
    var text = originalText.filterNot {
        it == '\"' || it == '{' || it == '}' || it == '\\' || it == ','
    }

    text = text.replace("_", " ")

    var pos: Int = text.indexOf("\n")
    while (pos != -1) {
        if (pos + 2 >= text.length) {
            break
        }
        text = capitalizeAtIndex(text, pos + 2)
        pos = text.indexOf("\n", pos + 1)
    }
    return text
}

fun capitalizeAtIndex(word: String, pos: Int): String {
    return word.substring(0, pos) + word[pos + 1].uppercase() + word.substring(pos + 2)
}
