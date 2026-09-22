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

import java.time.DateTimeException
import java.time.Year
import java.time.YearMonth

@Unroll
class YearMonthRangeSetTest extends Specification {

    // Maps a plain month offset (as used by the other range set test tables) onto an actual YearMonth, starting
    // Jan 2024. This naturally exercises year rollover for any offset past month 12 without the where-tables
    // needing to know about calendars at all.
    private static YearMonth month(int offset) {
        return YearMonth.of(2024, 1).plusMonths(offset - 1L)
    }

    private static kotlin.ranges.ClosedRange<YearMonth> monthRange(groovy.lang.Range range) {
        return kotlin.ranges.RangesKt.rangeTo(month(range.from as int), month(range.to as int))
    }

    private static List<kotlin.ranges.ClosedRange<YearMonth>> monthRanges(List<groovy.lang.Range> ranges) {
        return ranges.collect { range -> monthRange(range) }
    }


    def 'containsValue: #value'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([3..5, 7..9, 13..16]))
        def emptySet = new YearMonthRangeSet()
        set.containsValue(month(value)) == contained
        emptySet.containsValue(month(value)) == false

        where:
        value | contained
        0     | false
        1     | false
        2     | false
        3     | true
        4     | true
        5     | true
        6     | false
        7     | true
        8     | true
        9     | true
        10    | false
        11    | false
        12    | false
        13    | true
        14    | true
        15    | true
        16    | true
        17    | false
    }

    def 'containsAll: #contains'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([3..5, 7..9, 13..16]))
        def emptySet = new YearMonthRangeSet()
        def ranges = monthRanges(contains)
        set.containsAll(ranges) == contained
        emptySet.containsAll(ranges) == false

        where:
        contains             | contained
        [0..2]               | false
        [3..3]               | true
        [3..5]               | true
        [3..6]               | false
        [3..7]               | false
        [12..14]             | false
        [14..15]             | true
        [13..16]             | true
        [13..17]             | false
        [12..16]             | false
        [3..5, 8..8, 14..15] | true
        [3..6, 8..8, 14..15] | false
        [2..17]              | false
        [3..3, 8..8, 14..14] | true
    }

    def 'addAll: #adds'() {
        expect:
        def set = new YearMonthRangeSet()
        set.addAll(monthRanges(adds)) == true
        set as List == monthRanges(expected)

        where:
        adds                                | expected
        [3..5, 3..5]                        | [3..5]
        [3..5, 1..5]                        | [1..5]
        [3..5, 3..7]                        | [3..7]
        [3..5, 1..4]                        | [1..5]
        [3..5, 4..7]                        | [3..7]
        [3..5, 2..7]                        | [2..7]
        [2..7, 3..5]                        | [2..7]
        [3..5, 7..9, 4..8]                  | [3..9]
        [3..5, 8..9, 13..16, 10..11, 6..6]  | [3..6, 8..11, 13..16]
        [3..5, 7..9, 13..16, 20..25, 6..21] | [3..25]
        [2..4, 0..1, 1..1, 0..0, 2..3]      | [0..4]
        [3..5, 1..2]                        | [1..5]
        // crosses the Dec 2024/Jan 2025 boundary
        [10..12, 13..15]                    | [10..15]
    }

    def 'removeAll: #removes'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([3..5, 7..9, 13..16]))
        def emptySet = new YearMonthRangeSet()
        def ranges = monthRanges(removes)
        set.removeAll(ranges) == removed
        emptySet.removeAll(ranges) == false
        set as List == monthRanges(expected)
        emptySet as List == []

        where:
        removes         | removed | expected
        [0..2]          | false   | [3..5, 7..9, 13..16]
        [6..6]          | false   | [3..5, 7..9, 13..16]
        [10..12]        | false   | [3..5, 7..9, 13..16]
        [1..3]          | true    | [4..5, 7..9, 13..16]
        [1..4]          | true    | [5..5, 7..9, 13..16]
        [1..5]          | true    | [7..9, 13..16]
        [1..6]          | true    | [7..9, 13..16]
        [1..7]          | true    | [8..9, 13..16]
        [5..7]          | true    | [3..4, 8..9, 13..16]
        [4..7]          | true    | [3..3, 8..9, 13..16]
        [3..7]          | true    | [8..9, 13..16]
        [14..15]        | true    | [3..5, 7..9, 13..13, 16..16]
        [0..2, 11..12]  | false   | [3..5, 7..9, 13..16]
        [2..7, 14..15]  | true    | [8..9, 13..13, 16..16]
        [2..10, 12..18] | true    | []
    }

    def 'retainAll: #retains'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([3..7, 12..16, 22..27]))
        def emptySet = new YearMonthRangeSet()
        def ranges = monthRanges(retains)
        set.retainAll(ranges) == changed
        emptySet.retainAll(ranges) == false
        set as List == monthRanges(expected)
        emptySet as List == []

        where:
        retains                 | changed | expected
        [3..7]                  | true    | [3..7]
        [2..7]                  | true    | [3..7]
        [3..8]                  | true    | [3..7]
        [2..8]                  | true    | [3..7]
        [3..27]                 | false   | [3..7, 12..16, 22..27]
        [2..28]                 | false   | [3..7, 12..16, 22..27]
        [4..26]                 | true    | [4..7, 12..16, 22..26]
        [14..23]                | true    | [14..16, 22..23]
        [3..7, 12..16, 22..27]  | false   | [3..7, 12..16, 22..27]
        [4..13, 15..24]         | true    | [4..7, 12..13, 15..16, 22..24]
        [2..15, 13..26, 22..28] | false   | [3..7, 12..16, 22..27]
        [4..15, 13..26, 22..26] | true    | [4..7, 12..16, 22..26]
    }

    def 'differenceAll: #set'() {
        expect:
        new YearMonthRangeSet(monthRanges(set)).differenceAll(monthRanges(difference)).toList() == monthRanges(expected)

        where:
        set                             | difference       | expected
        []                              | []               | []
        []                              | [1..2]           | [1..2]
        []                              | [1..2, 5..7]     | [1..2, 5..7]
        [3..7]                          | []               | []
        [3..7]                          | [4..6]           | []
        [3..7]                          | [3..7]           | []
        [3..7]                          | [2..8]           | [2..2, 8..8]
        [3..7]                          | [1..4, 6..10]    | [1..2, 8..10]
        [3..7, 12..16, 22..27, 29..32]  | []               | []
        [3..7, 12..16, 22..27, 29..32]  | [5..14, 26..30]  | [8..11, 28..28]
        // spans across the Dec 2024/Jan 2025 boundary
        [3..7, 12..16, 22..27, 29..32]  | [1..35]          | [1..2, 8..11, 17..21, 28..28, 33..35]
    }

    def 'gaps: #set'() {
        expect:
        new YearMonthRangeSet(monthRanges(set)).gaps().toList() == monthRanges(expected)

        where:
        set                             | expected
        []                              | []
        [3..7]                          | []
        [3..7, 12..16, 22..27, 29..32]  | [8..11, 17..21, 28..28]
    }

    def 'hashCode/equals: #ranges'() {
        expect:
        def setA = new YearMonthRangeSet(monthRanges([1..3, 7..10, 15..15]))
        def setB = new YearMonthRangeSet(monthRanges(ranges))
        (setA.hashCode() == setB.hashCode()) == equal
        (setA == setB) == equal

        where:
        ranges                | equal
        [1..3, 7..10, 15..15] | true
        [2..3, 7..10, 15..15] | false
        [7..10, 1..3, 15..15] | true
    }

    def 'coalesces ranges across a year boundary'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([]))
        set.add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2023, 10), YearMonth.of(2023, 12)))
        set.add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2024, 1), YearMonth.of(2024, 2)))
        set as List == [kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2023, 10), YearMonth.of(2024, 2))]
    }

    def 'does not treat months in different years as adjacent unless truly consecutive'() {
        expect:
        def set = new YearMonthRangeSet(monthRanges([]))
        set.add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2023, 10), YearMonth.of(2023, 11)))
        set.add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2024, 1), YearMonth.of(2024, 2)))
        set as List == [
                kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2023, 10), YearMonth.of(2023, 11)),
                kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2024, 1), YearMonth.of(2024, 2))
        ]

        // Dec 2023 is the one-month gap between them
        set.gaps().toList() == [kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2023, 12), YearMonth.of(2023, 12))]
    }

    def 'throws when adding a range that would decrement past the minimum year-month'() {
        when:
        new YearMonthRangeSet().add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(Year.MIN_VALUE, 1), YearMonth.of(2024, 5)))

        then:
        thrown(DateTimeException)
    }

    def 'throws when adding a range that would increment past the maximum year-month'() {
        when:
        new YearMonthRangeSet().add(kotlin.ranges.RangesKt.rangeTo(YearMonth.of(2024, 5), YearMonth.of(Year.MAX_VALUE, 12)))

        then:
        thrown(DateTimeException)
    }
}
