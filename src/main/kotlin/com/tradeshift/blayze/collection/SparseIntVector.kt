package com.tradeshift.blayze.collection

import com.google.protobuf.ByteString
import com.tradeshift.blayze.Protos
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.TreeMap


/**
 * A sparse vector that stores non-zero indices and values in primitive arrays.
 */
class SparseIntVector private constructor(private val indices: IntArray, private val values: IntArray): Iterable<Pair<Int, Int>> {

    fun add(other: SparseIntVector): SparseIntVector {
        val m = TreeMap<Int, Int>()
        m.putAll(this)
        other.forEach { (idx, value) -> m[idx] = (m[idx] ?: 0) + value }
        return SparseIntVector(m.keys.toIntArray(), m.values.toIntArray())
    }

    /**
     * Iterator over non-zero elements of the vector.
     * @return An iterator of <index, value> pairs of non-zero elements
     */
    override fun iterator(): Iterator<Pair<Int, Int>> = indices.zip(values).iterator()

    fun toProto(): Protos.SparseIntVector {
        fun bytes(i: IntArray): ByteString {
            val out = ByteString.newOutput()
            i.forEach { out.write(it) }
            return out.toByteString()
        }

        //ByteBuffer.wrap(array).asIntBuffer().array()
        //ByteBuffer.allocate(1).asIntBuffer().array()///.put(indices[0])

        return Protos.SparseIntVector.newBuilder()
                .setIndices(bytes(indices))
                .setValues(bytes(values))
                .build()
    }

    companion object {
        fun fromMap(map: Map<Int, Int>): SparseIntVector {
            val sortedNonZeros = map.filter { it.value != 0 }.toSortedMap()
            return SparseIntVector(sortedNonZeros.keys.toIntArray(), sortedNonZeros.values.toIntArray())
        }

        fun fromProto(proto: Protos.SparseIntVector): SparseIntVector {
            fun backingArray(i: ByteString) = i.asReadOnlyByteBuffer().asIntBuffer().array()

            proto.indices.asReadOnlyByteBuffer().asIntBuffer()
            //val s: Sequence<Byte> = proto.indices.asSequence().map { it.toInt() }
            val indices = proto.indices.map { it.toInt() }.toIntArray()
            val values = proto.values.map { it.toInt() }.toIntArray()
            return SparseIntVector(indices, values)
        }
    }

    class BackedIntSequence(val b: ByteString): Iterable<Int> {
        override fun iterator(): Iterator<Int> {
            val intBuffer = b.asReadOnlyByteBuffer().asIntBuffer()
            return object: Iterator<Int> {
                override fun hasNext(): Boolean = intBuffer.hasRemaining()
                override fun next(): Int = intBuffer.get()
            }
        }

    }
}
