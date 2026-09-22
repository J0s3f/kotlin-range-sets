/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jon Peterson
 * Copyright (c) 2024 Josef H.B. Schneider
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.j0s3f.kotlin.rangesets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * UInt is an inline value class, so its ranges can't be built from Groovy the way the other range
 * set tests are (Groovy has no way to construct a boxed UInt directly). This suite is plain Kotlin instead.
 */
class UIntRangeSetTest {

    @Test
    fun containsValue() {
        val set = UIntRangeSet(3u..5u, 7u..9u, 13u..16u)
        val emptySet = UIntRangeSet()

        for (value in 0u..17u) {
            val expected = value in 3u..5u || value in 7u..9u || value in 13u..16u
            assertEquals(expected, set.containsValue(value), "value=$value")
            assertFalse(emptySet.containsValue(value))
        }
    }

    @Test
    fun containsAll() {
        val set = UIntRangeSet(3u..5u, 7u..9u, 13u..16u)
        val emptySet = UIntRangeSet()

        assertFalse(set.containsAll(listOf(0u..2u)))
        assertTrue(set.containsAll(listOf(3u..3u)))
        assertTrue(set.containsAll(listOf(3u..5u)))
        assertFalse(set.containsAll(listOf(3u..6u)))
        assertFalse(set.containsAll(listOf(12u..14u)))
        assertTrue(set.containsAll(listOf(13u..16u)))
        assertTrue(set.containsAll(listOf(3u..5u, 8u..8u, 14u..15u)))
        assertFalse(set.containsAll(listOf(3u..6u, 8u..8u, 14u..15u)))
        assertFalse(emptySet.containsAll(listOf(3u..5u)))
    }

    @Test
    fun addAll() {
        assertEquals(listOf(3u..5u), UIntRangeSet().apply { addAll(listOf(3u..5u, 3u..5u)) }.toList())
        assertEquals(listOf(1u..5u), UIntRangeSet().apply { addAll(listOf(3u..5u, 1u..5u)) }.toList())
        assertEquals(listOf(3u..7u), UIntRangeSet().apply { addAll(listOf(3u..5u, 4u..7u)) }.toList())
        assertEquals(listOf(3u..9u), UIntRangeSet().apply { addAll(listOf(3u..5u, 7u..9u, 4u..8u)) }.toList())
        assertEquals(
            listOf(3u..6u, 8u..11u, 13u..16u),
            UIntRangeSet().apply { addAll(listOf(3u..5u, 8u..9u, 13u..16u, 10u..11u, 6u..6u)) }.toList()
        )
        assertTrue(UIntRangeSet().addAll(listOf(3u..5u)))
        assertFalse(UIntRangeSet(3u..5u).addAll(listOf(3u..5u)))
    }

    @Test
    fun removeAll() {
        val set = UIntRangeSet(3u..5u, 7u..9u, 13u..16u)
        val emptySet = UIntRangeSet()

        assertFalse(set.removeAll(listOf(0u..2u)))
        assertFalse(emptySet.removeAll(listOf(0u..2u)))
        assertTrue(set.removeAll(listOf(1u..4u)))
        assertEquals(listOf(5u..5u, 7u..9u, 13u..16u), set.toList())
        assertTrue(set.removeAll(listOf(2u..10u, 12u..18u)))
        assertTrue(set.isEmpty())
    }

    @Test
    fun retainAll() {
        val set = UIntRangeSet(3u..7u, 12u..16u, 22u..27u)

        assertFalse(set.retainAll(listOf(3u..27u)))
        assertEquals(listOf(3u..7u, 12u..16u, 22u..27u), set.toList())

        assertTrue(set.retainAll(listOf(4u..26u)))
        assertEquals(listOf(4u..7u, 12u..16u, 22u..26u), set.toList())
    }

    @Test
    fun differenceAll() {
        val set = UIntRangeSet(3u..7u, 12u..16u, 22u..27u, 29u..32u)
        assertEquals(
            listOf(1u..2u, 8u..11u, 17u..21u, 28u..28u, 33u..35u),
            set.differenceAll(listOf(1u..35u)).toList()
        )
    }

    @Test
    fun gaps() {
        val set = UIntRangeSet(3u..7u, 12u..16u, 22u..27u, 29u..32u)
        assertEquals(listOf(8u..11u, 17u..21u, 28u..28u), set.gaps().toList())
        assertTrue(UIntRangeSet(3u..7u).gaps().isEmpty())
    }

    @Test
    fun hashCodeAndEquals() {
        val setA = UIntRangeSet(1u..3u, 7u..10u, 15u..15u)
        val setB = UIntRangeSet(7u..10u, 1u..3u, 15u..15u)
        val setC = UIntRangeSet(2u..3u, 7u..10u, 15u..15u)

        assertEquals(setA, setB)
        assertEquals(setA.hashCode(), setB.hashCode())
        assertFalse(setA == setC)
    }

    @Test
    fun supportsValuesBeyondIntRange() {
        // UInt can represent values above Int.MAX_VALUE, which would be negative as a signed Int.
        val aboveIntMax = Int.MAX_VALUE.toUInt() + 100u
        assertTrue(aboveIntMax > Int.MAX_VALUE.toUInt())

        val set = UIntRangeSet(aboveIntMax..(aboveIntMax + 5u))
        assertTrue(set.containsValue(aboveIntMax))
        assertFalse(set.containsValue(aboveIntMax - 1u))

        // adjacent huge ranges are still coalesced
        set.add((aboveIntMax + 6u)..(UInt.MAX_VALUE - 1u))
        assertEquals(listOf(aboveIntMax..(UInt.MAX_VALUE - 1u)), set.toList())
    }

    @Test
    fun canAddARangeStartingAtTheMinimumValue() {
        val set = UIntRangeSet()
        set.add(UInt.MIN_VALUE..5u)
        assertEquals(listOf(UInt.MIN_VALUE..5u), set.toList())

        // and still coalesces with an adjacent range
        set.add(6u..10u)
        assertEquals(listOf(UInt.MIN_VALUE..10u), set.toList())
    }

    @Test
    fun canAddARangeEndingAtTheMaximumValue() {
        val set = UIntRangeSet()
        set.add(5u..UInt.MAX_VALUE)
        assertEquals(listOf(5u..UInt.MAX_VALUE), set.toList())

        // and still coalesces with an adjacent range
        set.add(1u..4u)
        assertEquals(listOf(1u..UInt.MAX_VALUE), set.toList())
    }
}
