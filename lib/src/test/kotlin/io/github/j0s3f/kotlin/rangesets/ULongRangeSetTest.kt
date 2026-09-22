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
 * ULong is an inline value class, so its ranges can't be built from Groovy the way the other range set
 * tests are (Groovy has no way to construct a boxed ULong directly). This suite is plain Kotlin instead.
 */
class ULongRangeSetTest {

    @Test
    fun containsValue() {
        val set = ULongRangeSet(3uL..5uL, 7uL..9uL, 13uL..16uL)
        val emptySet = ULongRangeSet()

        for (value in 0uL..17uL) {
            val expected = value in 3uL..5uL || value in 7uL..9uL || value in 13uL..16uL
            assertEquals(expected, set.containsValue(value), "value=$value")
            assertFalse(emptySet.containsValue(value))
        }
    }

    @Test
    fun containsAll() {
        val set = ULongRangeSet(3uL..5uL, 7uL..9uL, 13uL..16uL)
        val emptySet = ULongRangeSet()

        assertFalse(set.containsAll(listOf(0uL..2uL)))
        assertTrue(set.containsAll(listOf(3uL..3uL)))
        assertTrue(set.containsAll(listOf(3uL..5uL)))
        assertFalse(set.containsAll(listOf(3uL..6uL)))
        assertFalse(set.containsAll(listOf(12uL..14uL)))
        assertTrue(set.containsAll(listOf(13uL..16uL)))
        assertTrue(set.containsAll(listOf(3uL..5uL, 8uL..8uL, 14uL..15uL)))
        assertFalse(set.containsAll(listOf(3uL..6uL, 8uL..8uL, 14uL..15uL)))
        assertFalse(emptySet.containsAll(listOf(3uL..5uL)))
    }

    @Test
    fun addAll() {
        assertEquals(listOf(3uL..5uL), ULongRangeSet().apply { addAll(listOf(3uL..5uL, 3uL..5uL)) }.toList())
        assertEquals(listOf(1uL..5uL), ULongRangeSet().apply { addAll(listOf(3uL..5uL, 1uL..5uL)) }.toList())
        assertEquals(listOf(3uL..7uL), ULongRangeSet().apply { addAll(listOf(3uL..5uL, 4uL..7uL)) }.toList())
        assertEquals(listOf(3uL..9uL), ULongRangeSet().apply { addAll(listOf(3uL..5uL, 7uL..9uL, 4uL..8uL)) }.toList())
        assertEquals(
            listOf(3uL..6uL, 8uL..11uL, 13uL..16uL),
            ULongRangeSet().apply { addAll(listOf(3uL..5uL, 8uL..9uL, 13uL..16uL, 10uL..11uL, 6uL..6uL)) }.toList()
        )
        assertTrue(ULongRangeSet().addAll(listOf(3uL..5uL)))
        assertFalse(ULongRangeSet(3uL..5uL).addAll(listOf(3uL..5uL)))
    }

    @Test
    fun removeAll() {
        val set = ULongRangeSet(3uL..5uL, 7uL..9uL, 13uL..16uL)
        val emptySet = ULongRangeSet()

        assertFalse(set.removeAll(listOf(0uL..2uL)))
        assertFalse(emptySet.removeAll(listOf(0uL..2uL)))
        assertTrue(set.removeAll(listOf(1uL..4uL)))
        assertEquals(listOf(5uL..5uL, 7uL..9uL, 13uL..16uL), set.toList())
        assertTrue(set.removeAll(listOf(2uL..10uL, 12uL..18uL)))
        assertTrue(set.isEmpty())
    }

    @Test
    fun retainAll() {
        val set = ULongRangeSet(3uL..7uL, 12uL..16uL, 22uL..27uL)

        assertFalse(set.retainAll(listOf(3uL..27uL)))
        assertEquals(listOf(3uL..7uL, 12uL..16uL, 22uL..27uL), set.toList())

        assertTrue(set.retainAll(listOf(4uL..26uL)))
        assertEquals(listOf(4uL..7uL, 12uL..16uL, 22uL..26uL), set.toList())
    }

    @Test
    fun differenceAll() {
        val set = ULongRangeSet(3uL..7uL, 12uL..16uL, 22uL..27uL, 29uL..32uL)
        assertEquals(
            listOf(1uL..2uL, 8uL..11uL, 17uL..21uL, 28uL..28uL, 33uL..35uL),
            set.differenceAll(listOf(1uL..35uL)).toList()
        )
    }

    @Test
    fun gaps() {
        val set = ULongRangeSet(3uL..7uL, 12uL..16uL, 22uL..27uL, 29uL..32uL)
        assertEquals(listOf(8uL..11uL, 17uL..21uL, 28uL..28uL), set.gaps().toList())
        assertTrue(ULongRangeSet(3uL..7uL).gaps().isEmpty())
    }

    @Test
    fun hashCodeAndEquals() {
        val setA = ULongRangeSet(1uL..3uL, 7uL..10uL, 15uL..15uL)
        val setB = ULongRangeSet(7uL..10uL, 1uL..3uL, 15uL..15uL)
        val setC = ULongRangeSet(2uL..3uL, 7uL..10uL, 15uL..15uL)

        assertEquals(setA, setB)
        assertEquals(setA.hashCode(), setB.hashCode())
        assertFalse(setA == setC)
    }

    @Test
    fun supportsValuesBeyondLongRange() {
        // ULong can represent values above Long.MAX_VALUE, which would be negative as a signed Long.
        val aboveLongMax = Long.MAX_VALUE.toULong() + 100uL
        assertTrue(aboveLongMax > Long.MAX_VALUE.toULong())

        val set = ULongRangeSet(aboveLongMax..(aboveLongMax + 5uL))
        assertTrue(set.containsValue(aboveLongMax))
        assertFalse(set.containsValue(aboveLongMax - 1uL))

        // adjacent huge ranges are still coalesced
        set.add((aboveLongMax + 6uL)..(ULong.MAX_VALUE - 1uL))
        assertEquals(listOf(aboveLongMax..(ULong.MAX_VALUE - 1uL)), set.toList())
    }

    @Test
    fun canAddARangeStartingAtTheMinimumValue() {
        val set = ULongRangeSet()
        set.add(ULong.MIN_VALUE..5uL)
        assertEquals(listOf(ULong.MIN_VALUE..5uL), set.toList())

        // and still coalesces with an adjacent range
        set.add(6uL..10uL)
        assertEquals(listOf(ULong.MIN_VALUE..10uL), set.toList())
    }

    @Test
    fun canAddARangeEndingAtTheMaximumValue() {
        val set = ULongRangeSet()
        set.add(5uL..ULong.MAX_VALUE)
        assertEquals(listOf(5uL..ULong.MAX_VALUE), set.toList())

        // and still coalesces with an adjacent range
        set.add(1uL..4uL)
        assertEquals(listOf(1uL..ULong.MAX_VALUE), set.toList())
    }
}
