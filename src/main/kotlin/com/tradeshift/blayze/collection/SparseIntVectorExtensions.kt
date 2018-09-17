package com.tradeshift.blayze.collection

import com.google.protobuf.ByteString
import com.tradeshift.blayze.Protos.SparseIntVector
import java.nio.ByteBuffer
import java.util.TreeMap


fun SparseIntVector.add(other: SparseIntVector): SparseIntVector {
    val m = TreeMap<Int, Int>()
    m.putAll(this.items())
    other.items().forEach { (idx, value) -> m[idx] = (m[idx] ?: 0) + value }
    return sparseIntVectorFromMap(m)
}

fun SparseIntVector.items(): Iterable<Pair<Int, Int>> = intIndices().zip(intValues())
fun SparseIntVector.intIndices(): Iterable<Int> = asInts(indices)
fun SparseIntVector.intValues(): Iterable<Int> = asInts(values)

/**
 * The SparseIntVector proto stores the int arrays in a bytes field to prevent unnecessary overhead when
 * deserializing a model. This function parses ints directly from that byte field.
 */
private fun asInts(b: ByteString): Iterable<Int> {
    val ib = b.asReadOnlyByteBuffer().asIntBuffer()
    return object: Iterable<Int> {
        override fun iterator(): Iterator<Int> {
            return object: Iterator<Int> {
                override fun hasNext(): Boolean = ib.hasRemaining()
                override fun next(): Int = ib.get()
            }
        }

    }
}

fun sparseIntVectorFromMap(map: Map<Int, Int>): SparseIntVector {
    fun toByteString(ints: Collection<Int>): ByteString {
        val bb = ByteBuffer.allocate(ints.size * Integer.BYTES)
        ints.forEach { bb.putInt(it) }
        bb.position(0)
        return ByteString.copyFrom(bb)
    }

    val sortedNonZeros = map.filter { it.value != 0 }.toSortedMap()
    return SparseIntVector.newBuilder()
            .setIndices(toByteString(sortedNonZeros.keys))
            .setValues(toByteString(sortedNonZeros.values))
            .build()
}
