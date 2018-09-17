package com.tradeshift.blayze.collection

import org.junit.Assert.*
import org.junit.Test

class SparseIntVectorTest {
    private val vector = sparseIntVectorFromMap(mapOf(
            3 to 30,
            2 to 20,
            1 to 1000,
            7 to 70
    ))

    private val expected = listOf(1 to 1000, 2 to 20, 3 to 30, 7 to 70)

    @Test
    fun iterates_in_order() {
        assertEquals(expected, vector.items().toList())
    }

    @Test
    fun add() {
        val other = sparseIntVectorFromMap(mapOf(1 to 10, 4 to 40, 7 to 70))

        val expected = listOf(1 to 1010, 2 to 20, 3 to 30, 4 to 40, 7 to 140)
        assertEquals(expected, vector.add(other).items().toList())
    }

    @Test
    fun test_serialization() {
        val v = vector.add(sparseIntVectorFromMap(mapOf()))
        assertEquals(vector, v)
    }
}