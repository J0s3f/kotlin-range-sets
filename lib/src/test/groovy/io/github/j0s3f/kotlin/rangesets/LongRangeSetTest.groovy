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

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class LongRangeSetTest extends Specification {

    private static kotlin.ranges.LongRange groovyToKotlinRange(groovy.lang.Range range) {
        return new kotlin.ranges.LongRange(range.from as long, range.to as long)
    }

    private static List<kotlin.ranges.LongRange> groovyToKotlinRanges(List<groovy.lang.Range> ranges) {
        return ranges.collect { range -> groovyToKotlinRange(range) }
    }


    def 'containsValue: #value'() {
        expect:
        def set = new LongRangeSet(groovyToKotlinRanges([3L..5L, 7L..9L, 13L..16L]))
        def emptySet = new LongRangeSet()
        set.containsValue(value as long) == contained
        emptySet.containsValue(value as long) == false

        where:
        value | contained
        0L    | false
        1L    | false
        2L    | false
        3L    | true
        4L    | true
        5L    | true
        6L    | false
        7L    | true
        8L    | true
        9L    | true
        10L   | false
        11L   | false
        12L   | false
        13L   | true
        14L   | true
        15L   | true
        16L   | true
        17L   | false
    }

    def 'containsAll: #contains'() {
        expect:
        def set = new LongRangeSet(groovyToKotlinRanges([3L..5L, 7L..9L, 13L..16L]))
        def emptySet = new LongRangeSet()
        def ranges = groovyToKotlinRanges(contains)
        set.containsAll(ranges) == contained
        emptySet.containsAll(ranges) == false

        where:
        contains                | contained
        [0L..2L]                | false
        [3L..3L]                | true
        [3L..5L]                | true
        [3L..6L]                | false
        [3L..7L]                | false
        [12L..14L]              | false
        [14L..15L]              | true
        [13L..16L]              | true
        [13L..17L]              | false
        [12L..16L]              | false
        [3L..5L, 8L..8L, 14L..15L] | true
        [3L..6L, 8L..8L, 14L..15L] | false
        [2L..17L]               | false
        [3L..3L, 8L..8L, 14L..14L] | true
    }

    def 'addAll: #adds'() {
        expect:
        def set = new LongRangeSet()
        set.addAll(groovyToKotlinRanges(adds)) == true
        set as List == groovyToKotlinRanges(expected)

        where:
        adds                                        | expected
        [3L..5L, 3L..5L]                             | [3L..5L]
        [3L..5L, 1L..5L]                             | [1L..5L]
        [3L..5L, 3L..7L]                             | [3L..7L]
        [3L..5L, 1L..4L]                             | [1L..5L]
        [3L..5L, 4L..7L]                             | [3L..7L]
        [3L..5L, 2L..7L]                             | [2L..7L]
        [2L..7L, 3L..5L]                             | [2L..7L]
        [3L..5L, 7L..9L, 4L..8L]                     | [3L..9L]
        [3L..5L, 8L..9L, 13L..16L, 10L..11L, 6L..6L] | [3L..6L, 8L..11L, 13L..16L]
        [3L..5L, 7L..9L, 13L..16L, 20L..25L, 6L..21L]| [3L..25L]
        [2L..4L, 0L..1L, 1L..1L, 0L..0L, 2L..3L]     | [0L..4L]
        [3L..5L, 1L..2L]                             | [1L..5L]
        [(Long.MAX_VALUE - 7)..(Long.MAX_VALUE - 4), (Long.MAX_VALUE - 3)..(Long.MAX_VALUE - 1)] | [(Long.MAX_VALUE - 7)..(Long.MAX_VALUE - 1)]
    }

    def 'removeAll: #removes'() {
        expect:
        def set = new LongRangeSet(groovyToKotlinRanges([3L..5L, 7L..9L, 13L..16L]))
        def emptySet = new LongRangeSet()
        def ranges = groovyToKotlinRanges(removes)
        set.removeAll(ranges) == removed
        emptySet.removeAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        removes             | removed | expected
        [0L..2L]            | false   | [3L..5L, 7L..9L, 13L..16L]
        [6L..6L]            | false   | [3L..5L, 7L..9L, 13L..16L]
        [10L..12L]          | false   | [3L..5L, 7L..9L, 13L..16L]
        [1L..3L]            | true    | [4L..5L, 7L..9L, 13L..16L]
        [1L..4L]            | true    | [5L..5L, 7L..9L, 13L..16L]
        [1L..5L]            | true    | [7L..9L, 13L..16L]
        [1L..6L]            | true    | [7L..9L, 13L..16L]
        [1L..7L]            | true    | [8L..9L, 13L..16L]
        [5L..7L]            | true    | [3L..4L, 8L..9L, 13L..16L]
        [4L..7L]            | true    | [3L..3L, 8L..9L, 13L..16L]
        [3L..7L]            | true    | [8L..9L, 13L..16L]
        [14L..15L]          | true    | [3L..5L, 7L..9L, 13L..13L, 16L..16L]
        [0L..2L, 11L..12L]  | false   | [3L..5L, 7L..9L, 13L..16L]
        [2L..7L, 14L..15L]  | true    | [8L..9L, 13L..13L, 16L..16L]
        [2L..10L, 12L..18L] | true    | []
    }

    def 'retainAll: #retains'() {
        expect:
        def set = new LongRangeSet(groovyToKotlinRanges([3L..7L, 12L..16L, 22L..27L]))
        def emptySet = new LongRangeSet()
        def ranges = groovyToKotlinRanges(retains)
        set.retainAll(ranges) == changed
        emptySet.retainAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        retains                     | changed | expected
        [3L..7L]                    | true    | [3L..7L]
        [2L..7L]                    | true    | [3L..7L]
        [3L..8L]                    | true    | [3L..7L]
        [2L..8L]                    | true    | [3L..7L]
        [3L..27L]                   | false   | [3L..7L, 12L..16L, 22L..27L]
        [2L..28L]                   | false   | [3L..7L, 12L..16L, 22L..27L]
        [4L..26L]                   | true    | [4L..7L, 12L..16L, 22L..26L]
        [14L..23L]                  | true    | [14L..16L, 22L..23L]
        [3L..7L, 12L..16L, 22L..27L]| false   | [3L..7L, 12L..16L, 22L..27L]
        [4L..13L, 15L..24L]         | true    | [4L..7L, 12L..13L, 15L..16L, 22L..24L]
        [2L..15L, 13L..26L, 22L..28L] | false | [3L..7L, 12L..16L, 22L..27L]
        [4L..15L, 13L..26L, 22L..26L] | true  | [4L..7L, 12L..16L, 22L..26L]
    }

    def 'differenceAll: #set'() {
        expect:
        new LongRangeSet(groovyToKotlinRanges(set)).differenceAll(groovyToKotlinRanges(difference)).toList() == groovyToKotlinRanges(expected)

        where:
        set                                | difference           | expected
        []                                 | []                   | []
        []                                 | [1L..2L]             | [1L..2L]
        []                                 | [1L..2L, 5L..7L]     | [1L..2L, 5L..7L]
        [3L..7L]                           | []                   | []
        [3L..7L]                           | [4L..6L]             | []
        [3L..7L]                           | [3L..7L]             | []
        [3L..7L]                           | [2L..8L]             | [2L..2L, 8L..8L]
        [3L..7L]                           | [1L..4L, 6L..10L]    | [1L..2L, 8L..10L]
        [3L..7L, 12L..16L, 22L..27L, 29L..32L] | []               | []
        [3L..7L, 12L..16L, 22L..27L, 29L..32L] | [5L..14L, 26L..30L] | [8L..11L, 28L..28L]
        [3L..7L, 12L..16L, 22L..27L, 29L..32L] | [1L..35L]        | [1L..2L, 8L..11L, 17L..21L, 28L..28L, 33L..35L]
        [(Long.MIN_VALUE + 1)..(Long.MIN_VALUE + 6)] | [(Long.MIN_VALUE + 1)..(Long.MIN_VALUE + 9)] | [(Long.MIN_VALUE + 7)..(Long.MIN_VALUE + 9)]
    }

    def 'gaps: #set'() {
        expect:
        new LongRangeSet(groovyToKotlinRanges(set)).gaps().toList() == groovyToKotlinRanges(expected)

        where:
        set                                     | expected
        []                                      | []
        [3L..7L]                                | []
        [3L..7L, 12L..16L, 22L..27L, 29L..32L]  | [8L..11L, 17L..21L, 28L..28L]
        [(Long.MAX_VALUE - 10)..(Long.MAX_VALUE - 8), (Long.MAX_VALUE - 2)..(Long.MAX_VALUE - 1)] | [(Long.MAX_VALUE - 7)..(Long.MAX_VALUE - 3)]
    }

    def 'hashCode/equals: #ranges'() {
        expect:
        def setA = new LongRangeSet(groovyToKotlinRanges([1L..3L, 7L..10L, 15L..15L]))
        def setB = new LongRangeSet(groovyToKotlinRanges(ranges))
        (setA.hashCode() == setB.hashCode()) == equal
        (setA == setB) == equal

        where:
        ranges                     | equal
        [1L..3L, 7L..10L, 15L..15L] | true
        [2L..3L, 7L..10L, 15L..15L] | false
        [7L..10L, 1L..3L, 15L..15L] | true
    }

    def 'supports values beyond the Int range'() {
        expect:
        def belowInt = Integer.MIN_VALUE - 100L
        def aboveInt = Integer.MAX_VALUE + 100L
        def set = new LongRangeSet(groovyToKotlinRanges([belowInt..(belowInt + 5), aboveInt..(aboveInt + 5)]))
        set.containsValue(belowInt as long) == true
        set.containsValue(aboveInt as long) == true
        set.containsValue(0L) == false

        set.add(new kotlin.ranges.LongRange(belowInt + 6, aboveInt - 1))
        set.toList() == [new kotlin.ranges.LongRange(belowInt, aboveInt + 5)]
    }

    def 'can add a range starting at the minimum value'() {
        expect:
        def set = new LongRangeSet()
        set.add(new kotlin.ranges.LongRange(Long.MIN_VALUE, 5L))
        set as List == [new kotlin.ranges.LongRange(Long.MIN_VALUE, 5L)]

        // and still coalesces with an adjacent range
        set.add(new kotlin.ranges.LongRange(6L, 10L))
        set as List == [new kotlin.ranges.LongRange(Long.MIN_VALUE, 10L)]
    }

    def 'can add a range ending at the maximum value'() {
        expect:
        def set = new LongRangeSet()
        set.add(new kotlin.ranges.LongRange(5L, Long.MAX_VALUE))
        set as List == [new kotlin.ranges.LongRange(5L, Long.MAX_VALUE)]

        // and still coalesces with an adjacent range
        set.add(new kotlin.ranges.LongRange(1L, 4L))
        set as List == [new kotlin.ranges.LongRange(1L, Long.MAX_VALUE)]
    }
}
