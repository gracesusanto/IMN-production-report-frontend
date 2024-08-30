package com.jetpack.barcodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class Category {
    TOOLING, MESIN, OPERATOR, INVALID
}

enum class MesinStatus {
    RUNNING, IDLE, SETUP
}

// Create a list of cities
val mCities = listOf(
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
        ) {
                entry -> StartScreen(
                mesinStatus = entry.arguments?.getString("mesinStatus"),
                navController = navController
            )
        }
        composable(route = Screen.StopScreen.route) {
            StopScreen(navController = navController)
        }
        composable(route = Screen.CategorySubmitScreen.route + "/{rejectQty}/{reworkQty}",
            arguments = listOf(
                navArgument("rejectQty") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("reworkQty") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {entry ->
            CategorySubmitScreen(
                rejectQty  = entry.arguments?.getString("rejectQty"),
                reworkQty = entry.arguments?.getString("reworkQty"),
                navController = navController
            )
        }
        composable(route = Screen.StopSubmitScreen.route) {
            StopSubmitScreen(navController = navController)
        }
        composable(
            route = Screen.PostActivityScreen.route + "/{activityType}/{categoryDowntime}/{outputQty}/{rejectQty}/{reworkQty}/{coilNo}/{lotNo}/{packNo}",
            arguments = listOf(
                navArgument("activityType") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                },
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
            )
        ) { entry ->
            PostActivityScreen(
                activityType = entry.arguments?.getString("activityType"),
                categoryDowntime = entry.arguments?.getString("categoryDowntime"),
                outputQty = entry.arguments?.getString("outputQty"),
                rejectQty  = entry.arguments?.getString("rejectQty"),
                reworkQty = entry.arguments?.getString("reworkQty"),
                coilNo = entry.arguments?.getString("coilNo"),
                lotNo = entry.arguments?.getString("lotNo"),
                packNo = entry.arguments?.getString("packNo"),
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

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = AppDataStore(context)

    val operator = remember { mutableStateOf("") }
    val operatorStatus = remember { mutableStateOf("IDLE") }
    val isOperatorRunning = remember { mutableStateOf(false) }
    val isCallSuccessful = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        val future: CompletableFuture<String?> = CompletableFuture()

        CoroutineScope(context = Dispatchers.IO).launch {
            future.complete(dataStore.getSelectedOperator.firstOrNull())
        }

        val operatorId = withContext(Dispatchers.IO) {
            future.get(5, TimeUnit.SECONDS)
        } ?: "NONE"
        operator.value = operatorId
        API.getOperatorStatus(operatorId,
            {
                    response ->
                isCallSuccessful.value = true
                CoroutineScope(context = Dispatchers.IO).launch {
                    dataStore.saveSelectedTooling(response.getString("toolingId"))
                    dataStore.saveSelectedMesin(response.getString("mesinId"))
                }
                isOperatorRunning.value = response.getBoolean("isRunning")
                operatorStatus.value = response.getString("operatorStatus")
            }, {
                    error ->
                isCallSuccessful.value = true
                isError.value = true
                error.message?.let { Log.e("API_NAVIGATION", it) }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        if (operator.value == "NONE" || operator.value == "") {
            Button(onClick = {
                navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.OPERATOR.name))
            }
            ) {
                Text(text = "Login")
            }
        } else {
            val operatorNameBefore = operator.value
            val operatorNameUpdated = operatorNameBefore.substring(3).replace("-", " ")
            Log.i("DataStore", operatorNameUpdated)
            Text(text = "Halo, $operatorNameUpdated!", fontSize = 25.sp)
            Spacer (modifier = Modifier.height(45.dp))
            if (isCallSuccessful.value) {
                if (isError.value) {
                    Text(text = R.string.connect_to_wifi.toString())
                } else {
                    if (operatorStatus.value != "RUNNING") {
                        Button(onClick = {
                            navController.navigate(
                                Screen.CameraPreviewScreen.withArgs(
                                    Category.TOOLING.name
                                )
                            )
                        }
                        ) { Text(text = "Mulai Aktivitas Baru") }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (isOperatorRunning.value) {
                        val buttonText = when (operatorStatus.value) {
                            "RUNNING" -> "Stop Running"
                            "DOWNTIME" -> "Ganti Kategori Downtime"
                            else -> {"Lanjutkan Aktivitas Anda"}
                        }
                        Button(onClick = { navController.navigate(Screen.ConfirmScreen.route) }
                        ) { Text(text = buttonText) }
                    }
                    Spacer (modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate(Screen.ListRunningMesinScreen.route)
                    }
                    ) {
                        Text(text = "List Running Mesin")
                    }
                    Spacer (modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.OPERATOR.name))
                    }
                    ) {
                        Text(text = "Ganti Akun")
                    }
                }
            } else { // Loading Button
                CircularProgressIndicator(modifier = Modifier.then(Modifier.size(25.dp)))
            }
        }
    }
}

@Composable
fun DetailScreen (
    category : String?,
    barCodeVal : String?,
    navController: NavController,
    imnViewModel: ImnViewModel = viewModel()
) {

    val detailsUiState by imnViewModel.detailsUiState.collectAsState()
    val details = detailsUiState.details
    val isCallSuccessful = detailsUiState.isCallSuccessful

    val currState: Category = Category.valueOf(category?:Category.INVALID.name)

    val context = LocalContext.current
    val dataStore = AppDataStore(context)

    LaunchedEffect(true) {
        if (currState != Category.INVALID) {
            API.getDetail(
                currState,
                barCodeVal?:"",
                { response ->
                    imnViewModel.updateDetails(
                        detailText = prettifyJson(response.toString(2)),
                        isSuccessful = true
                    )
                    CoroutineScope(context = Dispatchers.IO).launch {
                        when (currState) {
                            Category.TOOLING -> dataStore.saveSelectedTooling(barCodeVal?:"")
                            Category.MESIN -> dataStore.saveSelectedMesin(barCodeVal?:"")
                            Category.OPERATOR -> dataStore.saveSelectedOperator(barCodeVal?:"")
                            else -> {}
                        }
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
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isCallSuccessful) {
                Button(onClick = {
                    when (currState) {
                        Category.TOOLING -> navController.navigate(Screen.CameraPreviewScreen.withArgs(Category.MESIN.name))
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

@Composable
fun ConfirmScreen(
    imnViewModel: ImnViewModel = viewModel(),
    navController: NavController
) {
    val detailsUiState by imnViewModel.detailsUiState.collectAsState()

    val mesinStatus = detailsUiState.mesinStatus
    val detailText = detailsUiState.details
    val isCallSuccessful = detailsUiState.isCallSuccessful

    val context = LocalContext.current
    val dataStore = AppDataStore(context)

    val tooling = dataStore.getSelectedTooling.collectAsState(initial = "")
    val mesin = dataStore.getSelectedMesin.collectAsState(initial = "")
    val operator = dataStore.getSelectedOperator.collectAsState(initial = "")
    LaunchedEffect(tooling, mesin, operator) {
        API.checkOperatorStatus(
            toolingId = tooling.value?:"",
            mesinId = mesin.value?:"",
            operatorId = operator.value?:"",
            ResponseListener = { response ->
                val isOk: Boolean = response.getBoolean("isSuccess")
                if (isOk) {
                    API.getMesinStatus(
                        mesin.value?:"NONE",
                        { response -> imnViewModel.setMesinStatus(response.getString("status")) },
                        { error ->
                            imnViewModel.updateDetails(
                                detailText = "Mesin status invalid",
                                isSuccessful = false
                            )
                            error.message?.let { Log.e("API", it) }
                        }
                    )
                } else {
                    imnViewModel.updateDetails(
                        detailText = response.getString("errorMessage"),
                        isSuccessful = false
                    )
                }
            }, { error ->
                error.message?.let { Log.e("API", it) }
            })

    }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text="Tooling: ${tooling.value}\n " +
                "Mesin: ${mesin.value}\n " +
                "Operator: ${operator.value}"
        )
        Spacer(Modifier.height(30.dp))
        Text(text=detailText, color= Color.Red)
        Spacer(Modifier.height(30.dp))
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isCallSuccessful) {
                Button(onClick = {
                    when (mesinStatus) {
                        MesinStatus.IDLE -> navController.navigate(Screen.StartScreen.withArgs(mesinStatus.name))
                        MesinStatus.RUNNING -> navController.navigate(Screen.StopScreen.route)
                        MesinStatus.SETUP -> navController.navigate(Screen.StartScreen.withArgs(mesinStatus.name))
                    }
                }
                ) {
                    Text(text = "Yes")
                }
            }

            Button(onClick = {
                navController.navigate(Screen.MainScreen.route)
            }
            ) {
                Text(text = "Kembali ke Halaman Utama")
            }
        }
    }
}

@Composable
fun CameraPreview (
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
            Spacer (modifier = Modifier.height(8.dp))
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
                                    Toast.makeText(context, lastScanned, Toast.LENGTH_SHORT).show()
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

fun getCategoryNextState(currState: Category) : Category {
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
    val userInputState by imnViewModel.userInputState.collectAsState()
    val rejectQty = userInputState.rejectQty
    val reworkQty = userInputState.reworkQty

    val onRejectQtyUpdate: (String) -> Unit = { imnViewModel.updateRejectQty(it) }
    val onReworkQtyUpdate: (String) -> Unit = { imnViewModel.updateReworkQty(it) }
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
            Spacer (modifier = Modifier.height(8.dp))
        }
        Button(onClick = {
            navController.navigate(Screen.PostActivityScreen.withArgs("start", "-", "0", rejectQty, reworkQty, "-", "-", "-"))
        }
        ) {
            Text(text = "Start")
        }
        Spacer (modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.CategorySubmitScreen.withArgs(rejectQty, reworkQty))
        }
        ) {
            Text(text = "Ganti Kategori Downtime")
        }
        Spacer (modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
        ) {
            Text(text = "Kembali ke Halaman Utama")
        }
    }
}

@Composable
fun StopScreen(navController: NavController) {Column(
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
        Spacer (modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
        ) {
            Text(text = "Kembali ke Halaman Utama")
        }
    }
}

@Composable
fun StopSubmitScreen (
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

    val onOutputQtyUpdate: (String) -> Unit = { imnViewModel.updateOutputQty(it) }
    val onRejectQtyUpdate: (String) -> Unit = { imnViewModel.updateRejectQty(it) }
    val onReworkQtyUpdate: (String) -> Unit = { imnViewModel.updateReworkQty(it) }
    val onCoilNoUpdate: (String) -> Unit = { imnViewModel.updateCoilNo(it) }
    val onLotNoUpdate: (String) -> Unit = { imnViewModel.updateLotNo(it) }
    val onPackNoUpdate: (String) -> Unit = { imnViewModel.updatePackNo(it) }

    val (mSelectedState, setSelectedState) = remember { mutableStateOf("") }

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

        Spacer (modifier = Modifier.height(8.dp))
        DowntimeCategoryDropdown(mSelectedState, setSelectedState)
        Button(
            onClick = {
                if (mSelectedState in mCities) {
                    if (coilNo.isBlank()) {
                        coilNo = "-"
                    }
                    if (lotNo.isBlank()) {
                        lotNo = "-"
                    }
                    if (packNo.isBlank()) {
                        packNo = "-"
                    }
                    navController.navigate(Screen.PostActivityScreen.withArgs(
                        "first_stop",
                        mSelectedState,
                        outputQty,
                        rejectQty,
                        reworkQty,
                        coilNo,
                        lotNo,
                        packNo,)
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
                if (mSelectedText in mCities) {
                    Text("Kategori Tooling")
                } else {
                    Text("Kategori Tooling Invalid")
                }
            },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            },
            isError = mSelectedText !in mCities,
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
        ) {
            mCities.forEach { label ->
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
    activityType: String?,
    categoryDowntime: String?,
    outputQty: String?,
    rejectQty: String?,
    reworkQty: String?,
    coilNo: String?,
    lotNo: String?,
    packNo: String?,
    imnViewModel: ImnViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = AppDataStore(context)

    val tooling = dataStore.getSelectedTooling.collectAsState(initial = "")
    val mesin = dataStore.getSelectedMesin.collectAsState(initial = "")
    val operator = dataStore.getSelectedOperator.collectAsState(initial = "")
    LaunchedEffect(key1 = tooling, key2 = mesin, key3 = operator) {
        imnViewModel.submitActivity(
            activityType = activityType?:"",
            tooling = tooling.value?:"",
            mesin = mesin.value?:"",
            operator = operator.value?:"",
            output = outputQty?:"",
            rejectQty = rejectQty?:"",
            reworkQty = reworkQty?:"",
            coilNo = coilNo?:"",
            lotNo = lotNo?:"",
            packNo = packNo?:"",
            categoryDowntime = categoryDowntime?:"",
            navController = navController
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp)
    ) {
        Text("Submitting")
    }
}

@Composable
fun CategorySubmitScreen(
    rejectQty: String?,
    reworkQty: String?,
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
                if (mSelectedState in mCities) {
                    navController.navigate(Screen.PostActivityScreen.withArgs("continue_stop", mSelectedState, "0", rejectQty?:"0", reworkQty?:"0", "-", "-","-"))
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
        Spacer (modifier = Modifier.height(8.dp))
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
        API.getListMesinStatus (
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

        Spacer (modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.MainScreen.route)
        }, modifier = Modifier.weight(1f)
        ) {
            Text(text = "Kembali ke halaman utama")
        }
    }
}

fun prettifyJson(originalText: String) : String {
    var text = originalText.filterNot {
        it == '\"' || it == '{' || it == '}' || it == '\\' || it == ','
    }

    text = text.replace("_", " ")

    var pos: Int = text.indexOf("\n")
    while (pos != -1) {
        if (pos + 2 >= text.length) {
            break
        }
        text = capitalizeAtIndex(text, pos+2)
        pos = text.indexOf("\n", pos + 1)
    }
    return text
}

fun capitalizeAtIndex(word: String, pos: Int) : String {
    return word.substring(0, pos) + word[pos+1].uppercase() + word.substring(pos+2)
}