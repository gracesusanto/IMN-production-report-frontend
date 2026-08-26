package com.jetpack.barcodescanner

internal val NON_MACHINE_CATEGORY_CODES = setOf("BR", "BT", "NP")

internal fun categoryCode(category: String?): String =
    category.orEmpty().substringBefore(":").trim().uppercase()

internal fun categoryRequiresMachine(category: String?): Boolean {
    val code = categoryCode(category)
    return code.isNotEmpty() && code !in NON_MACHINE_CATEGORY_CODES
}

internal fun normalizedStoredValue(value: String?): String? {
    val normalized = value?.trim().orEmpty()
    return normalized.takeUnless {
        it.isEmpty() || it.equals("null", ignoreCase = true) || it.equals("none", ignoreCase = true)
    }
}

internal data class ActivitySubmissionPayload(
    val tooling: String,
    val mesin: String,
    val operator: String,
    val currCategory: String,
    val nextCategory: String,
    val output: String,
    val reject: String,
    val rework: String,
    val coilNo: String,
    val lotNo: String,
    val packNo: String,
    val keterangan: String,
)
