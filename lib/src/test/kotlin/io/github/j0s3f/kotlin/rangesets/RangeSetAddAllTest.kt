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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * RangeSet.addAll() used to compute its result with `elements.map { add(it) }.any()` - `any()` with no
 * predicate reports whether the list is non-empty, not whether it contains a `true`, so addAll() returned
 * true for any non-empty input even when every individual add() call reported no change. Every existing
 * addAll test happened to start from an empty set, where the first add() is always a real change, so this
 * never surfaced until a redundant addAll() on an already-populated set was tested.
 */
class RangeSetAddAllTest {

    @Test
    fun `reports no change when every element being added is already fully present`() {
        val set = IntRangeSet(3..5)
        assertFalse(set.addAll(listOf(3..5)))
        assertFalse(set.addAll(listOf(3..4, 4..5)))
    }

    @Test
    fun `reports a change when at least one element being added is new`() {
        val set = IntRangeSet(3..5)
        assertTrue(set.addAll(listOf(3..5, 10..12)))
        assertTrue(IntRangeSet().addAll(listOf(3..5)))
    }
}
