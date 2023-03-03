package com.jetpack.barcodescanner.ui.theme

import androidx.camera.core.Preview
import com.jetpack.barcodescanner.MesinStatus

data class DetailsUiState (
    val details: String = "",
    val isCallSuccessful: Boolean = false,
    val mesinStatus: MesinStatus = MesinStatus.IDLE,
)

data class UserInputState (
    val outputQty: String = "0",
    val rejectQty: String = "0",
    val reworkQty: String = "0",
    val coilNo: String = "",
    val lotNo: String = "",
    val packNo: String = "",
)