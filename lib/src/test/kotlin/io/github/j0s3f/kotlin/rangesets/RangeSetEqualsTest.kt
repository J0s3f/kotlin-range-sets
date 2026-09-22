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
 * RangeSet.ranges is a TreeSet ordered by an overlap-based comparator (needed so overlapping/adjacent
 * ranges collide and get merged). That comparator makes TreeSet's own equals()/contains() unsuitable for
 * comparing two different RangeSets, since those consider two *overlapping but different* ranges "equal"
 * for ordering purposes. This must be tested in plain Kotlin/JUnit rather than Groovy/Spock: Groovy's own
 * `==`/`.equals()` dispatch for Set-like objects goes through its own structural-equality extension methods
 * rather than the JVM's real virtual equals() call, so a Groovy test would pass even if the real equals()
 * implementation were broken (as it was before this fix).
 */
class RangeSetEqualsTest {

    @Test
    fun `does not treat sets with different but overlapping ranges as equal`() {
        val a = IntRangeSet(1..3, 7..10, 15..15)
        val b = IntRangeSet(2..3, 7..10, 15..15)

        assertFalse(a.equals(b))
        assertFalse(b.equals(a))
        assertFalse(a == b)
    }

    @Test
    fun `treats sets with identical ranges as equal regardless of insertion order`() {
        val a = IntRangeSet(1..3, 7..10, 15..15)
        val b = IntRangeSet(7..10, 1..3, 15..15)

        assertTrue(a.equals(b))
        assertTrue(b.equals(a))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `unequal sets do not collide as equal via hashCode contract violations`() {
        val a = IntRangeSet(1..3, 7..10, 15..15)
        val b = IntRangeSet(2..3, 7..10, 15..15)

        // Not a strict requirement that unequal objects have different hashCodes, but if they do differ
        // here it's further confirmation these are genuinely treated as distinct, non-colliding sets.
        assertFalse(a == b)
        assertTrue(a.hashCode() != b.hashCode())
    }
}
