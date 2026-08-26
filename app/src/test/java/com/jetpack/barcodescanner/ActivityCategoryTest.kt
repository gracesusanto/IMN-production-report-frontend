package com.jetpack.barcodescanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityCategoryTest {
    @Test
    fun noPlanIsNonMachineByCode() {
        assertEquals("NP", categoryCode("NP : No Plan"))
        assertFalse(categoryRequiresMachine("NP : No Plan"))
        assertFalse(categoryRequiresMachine("NP : Historical Label"))
    }

    @Test
    fun machineCategoriesStillRequireMachineAndTooling() {
        assertTrue(categoryRequiresMachine("U : Utility"))
        assertTrue(categoryRequiresMachine("MP : Machine Problem"))
    }

    @Test
    fun datastoreSentinelsAreTreatedAsMissing() {
        assertNull(normalizedStoredValue("NONE"))
        assertNull(normalizedStoredValue("null"))
        assertNull(normalizedStoredValue(" "))
        assertEquals("MC-1", normalizedStoredValue(" MC-1 "))
    }
}
