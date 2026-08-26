package com.jetpack.barcodescanner.ui

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.jetpack.barcodescanner.*
import com.jetpack.barcodescanner.ui.theme.DetailsUiState
import com.jetpack.barcodescanner.ui.theme.SubmissionUiState
import com.jetpack.barcodescanner.ui.theme.UserInputState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

class ImnViewModel : ViewModel() {
    // Details UI state
    private val _detailsUiState = MutableStateFlow(DetailsUiState())
    // Backing property to avoid state updates from other classes
    val detailsUiState: StateFlow<DetailsUiState> = _detailsUiState.asStateFlow()

    fun updateDetails(detailText: String, isSuccessful: Boolean) {
        _detailsUiState.update { currentState ->
            currentState.copy(
                details = detailText,
                isCallSuccessful = isSuccessful
            )
        }
    }

    fun setMesinStatus(status: String) {
        val statuses = arrayOf("running", "idle", "setup")

        if (status.lowercase() in statuses) {
            _detailsUiState.update { currentState ->
                currentState.copy(
                    mesinStatus = MesinStatus.valueOf(status.uppercase()),
                    isCallSuccessful = true
                )
            }
        } else {
            _detailsUiState.update { currentState ->
                currentState.copy(
                    isCallSuccessful = false
                )
            }

        }
    }

    private val _userInputState = MutableStateFlow(UserInputState())
    val userInputState: StateFlow<UserInputState> = _userInputState.asStateFlow()

    fun updateOutputQty(enteredNumber: String){
        _userInputState.update { currentState ->
            currentState.copy(
                outputQty = enteredNumber
            )
        }
    }

    fun updateReworkQty(enteredNumber: String){
        _userInputState.update { currentState ->
            currentState.copy(
                reworkQty = enteredNumber
            )
        }
    }

    fun updateRejectQty(enteredNumber: String){
        _userInputState.update { currentState ->
            currentState.copy(
                rejectQty = enteredNumber
            )
        }
    }

    fun updateCoilNo(enteredText: String){
        _userInputState.update { currentState ->
            currentState.copy(
                coilNo = enteredText
            )
        }
    }

    fun updateLotNo(enteredText: String){
        _userInputState.update { currentState ->
            currentState.copy(
                lotNo = enteredText
            )
        }
    }

    fun updatePackNo(enteredText: String){
        _userInputState.update { currentState ->
            currentState.copy(
                packNo = enteredText
            )
        }
    }

    fun updateKeterangan(enteredText: String){
        _userInputState.update { currentState ->
            currentState.copy(
                keterangan = enteredText
            )
        }
    }

    var mSelectedText by mutableStateOf("")
        private set
    var mExpanded by mutableStateOf(false)
        private set
    var mTextFieldSize by mutableStateOf(Size.Zero)
        private set

    fun updateCategorySelection(categoryPicked: String) {
        mSelectedText = categoryPicked
    }

    fun updateCoordinate(coordinates: LayoutCoordinates) {
        mTextFieldSize = coordinates.size.toSize()
    }

    fun updateDropdownExpanded(expanded: Boolean) {
        mExpanded = expanded
    }

    private val _submissionState = MutableStateFlow(SubmissionUiState())
    val submissionState: StateFlow<SubmissionUiState> = _submissionState.asStateFlow()

    fun resetSubmissionState() {
        _submissionState.value = SubmissionUiState()
    }

    private fun failSubmission(message: String) {
        _submissionState.value = SubmissionUiState(
            isError = true,
            errorMessage = message,
        )
    }

    private fun parseQuantity(value: String, label: String): Int? {
        val normalized = value.trim().ifEmpty { "0" }
        val quantity = normalized.toIntOrNull()
        if (quantity == null || quantity < 0) {
            failSubmission("$label must be a whole number of 0 or greater.")
            return null
        }
        return quantity
    }

    private fun errorMessage(error: com.android.volley.VolleyError): String {
        val statusCode = error.networkResponse?.statusCode
        val responseBody = error.networkResponse?.data
            ?.toString(Charsets.UTF_8)
            .orEmpty()
        val backendDetail = runCatching {
            JSONObject(responseBody).optString("detail").takeIf { it.isNotBlank() }
        }.getOrNull()

        return backendDetail
            ?: error.message?.takeIf { it.isNotBlank() }
            ?: statusCode?.let { "Server rejected the activity (HTTP $it)." }
            ?: "Could not reach the server. Check the connection and try again."
    }

    fun submitActivity(tooling: String,
                       mesin: String,
                       operator: String,
                       currCategory: String,
                       categoryDowntime: String,
                       output: String,
                       rejectQty: String,
                       reworkQty: String,
                       coilNo: String,
                       lotNo: String,
                       packNo: String,
                       keterangan: String,
                       navController: NavController,
    ) {
        val normalizedOperator = normalizedStoredValue(operator)
        val normalizedTooling = normalizedStoredValue(tooling)
        val normalizedMesin = normalizedStoredValue(mesin)
        val normalizedCurrCategory = normalizedStoredValue(currCategory).orEmpty()

        if (normalizedOperator == null) {
            failSubmission("Operator is missing. Please scan the operator again.")
            return
        }
        if (categoryCode(categoryDowntime).isEmpty()) {
            failSubmission("The next activity category is missing.")
            return
        }
        if ((categoryRequiresMachine(normalizedCurrCategory) || categoryRequiresMachine(categoryDowntime)) &&
            (normalizedMesin == null || normalizedTooling == null)
        ) {
            failSubmission("Machine and tooling are required for this activity. Please scan them again.")
            return
        }

        val parsedOutput = parseQuantity(output, "Output") ?: return
        val parsedReject = parseQuantity(rejectQty, "Reject quantity") ?: return
        val parsedRework = parseQuantity(reworkQty, "Rework quantity") ?: return

        _submissionState.value = SubmissionUiState(isLoading = true)
        API.postActivity(
            toolingId = normalizedTooling.orEmpty(),
            mesinId = normalizedMesin.orEmpty(),
            operatorId = normalizedOperator,
            currCategory = normalizedCurrCategory,
            categoryDowntime = categoryDowntime,
            output = parsedOutput,
            reject = parsedReject,
            rework = parsedRework,
            coilNo = coilNo,
            lotNo = lotNo,
            packNo = packNo,
            keterangan = keterangan,
            {
                _submissionState.value = SubmissionUiState()
                navController.navigate(Screen.SubmissionSuccessfulScreen.route)
            },
            { error ->
                val message = errorMessage(error)
                Log.e("API", message, error)
                failSubmission(message)
            }
        )
    }

}
