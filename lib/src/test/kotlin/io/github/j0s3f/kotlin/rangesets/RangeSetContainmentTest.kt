/*
 * The MIT License (MIT)
 *
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

import java.math.BigInteger
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RangeSetContainmentTest {
    @Test
    fun `containment distinguishes enclosure from overlap and gaps`() {
        val set = IntRangeSet(3..5, 9..12, 18..22)
        for (value in 0..25) {
            assertEquals(value in 3..5 || value in 9..12 || value in 18..22, set.containsValue(value))
        }
        for (query in listOf(3..3, 3..5, 4..5, 9..12, 18..22, 22..22)) {
            assertTrue(set.contains(query), "query=$query")
        }
        for (query in listOf(0..25, 4..20, 6..20, 6..10, 3..6, 2..5, 8..12, 12..18, 23..25)) {
            assertFalse(set.contains(query), "query=$query")
        }
        assertTrue(set.containsAll(listOf(3..5, 10..11, 22..22)))
        assertFalse(set.containsAll(listOf(3..5, 6..10)))
        assertFalse(IntRangeSet().containsValue(3))
        assertFalse(IntRangeSet().contains(3..5))
    }

    @Test
    fun `range lookup preserves the previous endpoint rules for reversed queries`() {
        val set = IntRangeSet(3..5, 9..12, 18..22)
        // The existing API checks endpoint enclosure even for reversed ranges; preserve that behavior.
        for (start in 0..25) for (end in 0..25) {
            val expected = set.any { start >= it.start && end <= it.endInclusive }
            assertEquals(expected, set.contains(start..end), "query=$start..$end")
        }
    }

    @Test
    fun `lookups remain correct after randomized merges removals and splits`() {
        val set = IntRangeSet()
        val oracle = mutableSetOf<Int>()
        val random = Random(418)
        repeat(300) {
            val start = random.nextInt(-40, 41)
            val end = random.nextInt(start, 41)
            if (random.nextBoolean()) {
                set.add(start..end)
                oracle.addAll(start..end)
            } else {
                set.remove(start..end)
                oracle.removeAll((start..end).toSet())
            }
            for (value in -41..41) assertEquals(value in oracle, set.containsValue(value))
            repeat(20) {
                val queryStart = random.nextInt(-41, 42)
                val queryEnd = random.nextInt(queryStart, 42)
                assertEquals((queryStart..queryEnd).all { it in oracle }, set.contains(queryStart..queryEnd))
            }
        }
    }

    @Test
    fun `singleton tree probes work at every supported type boundary`() {
        checkExtremes(IntRangeSet(Int.MIN_VALUE..Int.MIN_VALUE, Int.MAX_VALUE..Int.MAX_VALUE),
            Int.MIN_VALUE, Int.MAX_VALUE, 0)
        checkExtremes(LongRangeSet(Long.MIN_VALUE..Long.MIN_VALUE, Long.MAX_VALUE..Long.MAX_VALUE),
            Long.MIN_VALUE, Long.MAX_VALUE, 0L)
        checkExtremes(UIntRangeSet(0u..0u, UInt.MAX_VALUE..UInt.MAX_VALUE), 0u, UInt.MAX_VALUE, 1u)
        checkExtremes(ULongRangeSet(0uL..0uL, ULong.MAX_VALUE..ULong.MAX_VALUE), 0uL, ULong.MAX_VALUE, 1uL)
        checkExtremes(CharRangeSet(Char.MIN_VALUE..Char.MIN_VALUE, Char.MAX_VALUE..Char.MAX_VALUE),
            Char.MIN_VALUE, Char.MAX_VALUE, 'a')
        val huge = BigInteger.ONE.shiftLeft(1024)
        checkExtremes(BigIntegerRangeSet(-huge..-huge, huge..huge), -huge, huge, BigInteger.ZERO)
        checkExtremes(LocalDateRangeSet(LocalDate.MIN..LocalDate.MIN, LocalDate.MAX..LocalDate.MAX),
            LocalDate.MIN, LocalDate.MAX, LocalDate.of(2026, 1, 1))
        val firstMonth = YearMonth.of(Year.MIN_VALUE, 1)
        val lastMonth = YearMonth.of(Year.MAX_VALUE, 12)
        checkExtremes(YearMonthRangeSet(firstMonth..firstMonth, lastMonth..lastMonth),
            firstMonth, lastMonth, YearMonth.of(2026, 1))
    }

    @Test
    fun `point and range queries use logarithmically bounded comparisons`() {
        val counter = ComparisonCounter()
        fun value(number: Int) = CountedValue(number, counter)
        val set = CountedRangeSet()
        repeat(4096) { set.add(value(it * 4)..value(it * 4 + 1)) }
        fun checkComparisons(expected: Boolean, query: () -> Boolean) {
            counter.comparisons = 0
            assertEquals(expected, query())
            assertTrue(counter.comparisons < 100,
                "4096 disjoint ranges should require a tree search, used ${counter.comparisons} comparisons")
        }
        checkComparisons(true) { set.containsValue(value(4095 * 4)) }
        checkComparisons(false) { set.containsValue(value(4095 * 4 + 2)) }
        checkComparisons(false) { set.containsValue(value(-1)) }
        checkComparisons(true) { set.contains(value(4095 * 4)..value(4095 * 4 + 1)) }
        checkComparisons(false) { set.contains(value(4095 * 4)..value(4095 * 4 + 2)) }
        checkComparisons(false) { set.contains(value(-1)..value(4095 * 4)) }
    }

    private fun <T : Comparable<T>> checkExtremes(set: RangeSet<T>, first: T, last: T, missing: T) {
        assertTrue(set.containsValue(first))
        assertTrue(set.containsValue(last))
        assertFalse(set.containsValue(missing))
        assertTrue(set.contains(first..first))
        assertTrue(set.contains(last..last))
        assertFalse(set.contains(first..last))
        assertFalse(set.contains(missing..last))
    }

    private class ComparisonCounter(var comparisons: Int = 0)

    private data class CountedValue(val number: Int, val counter: ComparisonCounter) : Comparable<CountedValue> {
        override fun compareTo(other: CountedValue): Int {
            counter.comparisons++
            return number.compareTo(other.number)
        }
    }

    private class CountedRangeSet : RangeSet<CountedValue>() {
        override fun createRange(start: CountedValue, endInclusive: CountedValue) = start..endInclusive
        override fun incrementValue(value: CountedValue) = value.copy(number = value.number + 1)
        override fun decrementValue(value: CountedValue) = value.copy(number = value.number - 1)
        override fun empty() = CountedRangeSet()
        override fun clone(): RangeSet<CountedValue> = CountedRangeSet().also { it.addAll(this) }
    }
}
