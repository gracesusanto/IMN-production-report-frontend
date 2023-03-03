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
import com.jetpack.barcodescanner.ui.theme.UserInputState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun submitActivity(activityType: String,
                       tooling: String,
                       mesin: String,
                       operator: String,
                       categoryDowntime: String,
                       output: String,
                       rejectQty: String,
                       reworkQty: String,
                       coilNo: String,
                       lotNo: String,
                       packNo: String,
                       navController: NavController,
    ) {
        if (tooling.isEmpty() || mesin.isEmpty() || operator.isEmpty() || output.isEmpty()) { return }
        API.postActivity(
            type = activityType,
            toolingId = tooling,
            mesinId = mesin,
            operatorId = operator,
            categoryDowntime = categoryDowntime,
            output = output.toInt(),
            reject = rejectQty.toInt(),
            rework = reworkQty.toInt(),
            coilNo = coilNo,
            lotNo = lotNo,
            packNo = packNo,
            {
                navController.navigate(Screen.SubmissionSuccessfulScreen.route)
            },
            { error ->
                error.message?.let { Log.e("API", it) }
            }
        )
    }

}