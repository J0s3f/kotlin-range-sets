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
class BigIntegerRangeSetTest extends Specification {

    private static kotlin.ranges.ClosedRange<BigInteger> groovyToKotlinRange(groovy.lang.Range range) {
        return kotlin.ranges.RangesKt.rangeTo((BigInteger) range.from, (BigInteger) range.to)
    }

    private static List<kotlin.ranges.ClosedRange<BigInteger>> groovyToKotlinRanges(List<groovy.lang.Range> ranges) {
        return ranges.collect { range -> groovyToKotlinRange(range) }
    }


    def 'containsValue: #value'() {
        expect:
        def set = new BigIntegerRangeSet(groovyToKotlinRanges([3g..5g, 7g..9g, 13g..16g]))
        def emptySet = new BigIntegerRangeSet()
        set.containsValue(value as BigInteger) == contained
        emptySet.containsValue(value as BigInteger) == false

        where:
        value | contained
        0g    | false
        1g    | false
        2g    | false
        3g    | true
        4g    | true
        5g    | true
        6g    | false
        7g    | true
        8g    | true
        9g    | true
        10g   | false
        11g   | false
        12g   | false
        13g   | true
        14g   | true
        15g   | true
        16g   | true
        17g   | false
    }

    def 'containsAll: #contains'() {
        expect:
        def set = new BigIntegerRangeSet(groovyToKotlinRanges([3g..5g, 7g..9g, 13g..16g]))
        def emptySet = new BigIntegerRangeSet()
        def ranges = groovyToKotlinRanges(contains)
        set.containsAll(ranges) == contained
        emptySet.containsAll(ranges) == false

        where:
        contains                | contained
        [0g..2g]                | false
        [3g..3g]                 | true
        [3g..5g]                 | true
        [3g..6g]                 | false
        [3g..7g]                 | false
        [12g..14g]               | false
        [14g..15g]               | true
        [13g..16g]               | true
        [13g..17g]               | false
        [12g..16g]               | false
        [3g..5g, 8g..8g, 14g..15g] | true
        [3g..6g, 8g..8g, 14g..15g] | false
        [2g..17g]                | false
        [3g..3g, 8g..8g, 14g..14g] | true
    }

    def 'addAll: #adds'() {
        expect:
        def set = new BigIntegerRangeSet()
        set.addAll(groovyToKotlinRanges(adds)) == true
        set as List == groovyToKotlinRanges(expected)

        where:
        adds                                          | expected
        [3g..5g, 3g..5g]                               | [3g..5g]
        [3g..5g, 1g..5g]                               | [1g..5g]
        [3g..5g, 3g..7g]                               | [3g..7g]
        [3g..5g, 1g..4g]                               | [1g..5g]
        [3g..5g, 4g..7g]                               | [3g..7g]
        [3g..5g, 2g..7g]                               | [2g..7g]
        [2g..7g, 3g..5g]                               | [2g..7g]
        [3g..5g, 7g..9g, 4g..8g]                       | [3g..9g]
        [3g..5g, 8g..9g, 13g..16g, 10g..11g, 6g..6g]   | [3g..6g, 8g..11g, 13g..16g]
        [3g..5g, 7g..9g, 13g..16g, 20g..25g, 6g..21g]  | [3g..25g]
        [2g..4g, 0g..1g, 1g..1g, 0g..0g, 2g..3g]       | [0g..4g]
        [3g..5g, 1g..2g]                               | [1g..5g]
        [(-5g)..(-3g), (-2g)..0g]                      | [(-5g)..0g]
    }

    def 'removeAll: #removes'() {
        expect:
        def set = new BigIntegerRangeSet(groovyToKotlinRanges([3g..5g, 7g..9g, 13g..16g]))
        def emptySet = new BigIntegerRangeSet()
        def ranges = groovyToKotlinRanges(removes)
        set.removeAll(ranges) == removed
        emptySet.removeAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        removes             | removed | expected
        [0g..2g]            | false   | [3g..5g, 7g..9g, 13g..16g]
        [6g..6g]            | false   | [3g..5g, 7g..9g, 13g..16g]
        [10g..12g]          | false   | [3g..5g, 7g..9g, 13g..16g]
        [1g..3g]            | true    | [4g..5g, 7g..9g, 13g..16g]
        [1g..4g]            | true    | [5g..5g, 7g..9g, 13g..16g]
        [1g..5g]            | true    | [7g..9g, 13g..16g]
        [1g..6g]            | true    | [7g..9g, 13g..16g]
        [1g..7g]            | true    | [8g..9g, 13g..16g]
        [5g..7g]            | true    | [3g..4g, 8g..9g, 13g..16g]
        [4g..7g]            | true    | [3g..3g, 8g..9g, 13g..16g]
        [3g..7g]            | true    | [8g..9g, 13g..16g]
        [14g..15g]          | true    | [3g..5g, 7g..9g, 13g..13g, 16g..16g]
        [0g..2g, 11g..12g]  | false   | [3g..5g, 7g..9g, 13g..16g]
        [2g..7g, 14g..15g]  | true    | [8g..9g, 13g..13g, 16g..16g]
        [2g..10g, 12g..18g] | true    | []
    }

    def 'retainAll: #retains'() {
        expect:
        def set = new BigIntegerRangeSet(groovyToKotlinRanges([3g..7g, 12g..16g, 22g..27g]))
        def emptySet = new BigIntegerRangeSet()
        def ranges = groovyToKotlinRanges(retains)
        set.retainAll(ranges) == changed
        emptySet.retainAll(ranges) == false
        set as List == groovyToKotlinRanges(expected)
        emptySet as List == []

        where:
        retains                       | changed | expected
        [3g..7g]                      | true    | [3g..7g]
        [2g..7g]                      | true    | [3g..7g]
        [3g..8g]                      | true    | [3g..7g]
        [2g..8g]                      | true    | [3g..7g]
        [3g..27g]                     | false   | [3g..7g, 12g..16g, 22g..27g]
        [2g..28g]                     | false   | [3g..7g, 12g..16g, 22g..27g]
        [4g..26g]                     | true    | [4g..7g, 12g..16g, 22g..26g]
        [14g..23g]                    | true    | [14g..16g, 22g..23g]
        [3g..7g, 12g..16g, 22g..27g]  | false   | [3g..7g, 12g..16g, 22g..27g]
        [4g..13g, 15g..24g]           | true    | [4g..7g, 12g..13g, 15g..16g, 22g..24g]
        [2g..15g, 13g..26g, 22g..28g] | false   | [3g..7g, 12g..16g, 22g..27g]
        [4g..15g, 13g..26g, 22g..26g] | true    | [4g..7g, 12g..16g, 22g..26g]
    }

    def 'differenceAll: #set'() {
        expect:
        new BigIntegerRangeSet(groovyToKotlinRanges(set)).differenceAll(groovyToKotlinRanges(difference)).toList() == groovyToKotlinRanges(expected)

        where:
        set                                | difference       | expected
        []                                 | []               | []
        []                                 | [1g..2g]         | [1g..2g]
        []                                 | [1g..2g, 5g..7g] | [1g..2g, 5g..7g]
        [3g..7g]                           | []               | []
        [3g..7g]                           | [4g..6g]         | []
        [3g..7g]                           | [3g..7g]         | []
        [3g..7g]                           | [2g..8g]         | [2g..2g, 8g..8g]
        [3g..7g]                           | [1g..4g, 6g..10g] | [1g..2g, 8g..10g]
        [3g..7g, 12g..16g, 22g..27g, 29g..32g] | []           | []
        [3g..7g, 12g..16g, 22g..27g, 29g..32g] | [5g..14g, 26g..30g] | [8g..11g, 28g..28g]
        [3g..7g, 12g..16g, 22g..27g, 29g..32g] | [1g..35g]    | [1g..2g, 8g..11g, 17g..21g, 28g..28g, 33g..35g]
    }

    def 'gaps: #set'() {
        expect:
        new BigIntegerRangeSet(groovyToKotlinRanges(set)).gaps().toList() == groovyToKotlinRanges(expected)

        where:
        set                                     | expected
        []                                      | []
        [3g..7g]                                | []
        [3g..7g, 12g..16g, 22g..27g, 29g..32g]  | [8g..11g, 17g..21g, 28g..28g]
    }

    def 'hashCode/equals: #ranges'() {
        expect:
        def setA = new BigIntegerRangeSet(groovyToKotlinRanges([1g..3g, 7g..10g, 15g..15g]))
        def setB = new BigIntegerRangeSet(groovyToKotlinRanges(ranges))
        (setA.hashCode() == setB.hashCode()) == equal
        (setA == setB) == equal

        where:
        ranges                     | equal
        [1g..3g, 7g..10g, 15g..15g] | true
        [2g..3g, 7g..10g, 15g..15g] | false
        [7g..10g, 1g..3g, 15g..15g] | true
    }

    def 'supports arbitrary precision values far beyond Long range'() {
        expect:
        // 2^100 and neighbouring values, far beyond Long.MAX_VALUE
        def huge = BigInteger.TWO.pow(100)
        def set = new BigIntegerRangeSet(groovyToKotlinRanges([(huge - 10)..(huge - 5), (huge - 4)..(huge + 5)]))

        // adjacent huge ranges are coalesced into one
        set as List == [kotlin.ranges.RangesKt.rangeTo(huge - 10, huge + 5)]

        set.containsValue(huge) == true
        set.containsValue(huge - 100) == false
        set.containsValue(huge + 1_000_000g) == false

        // removing a huge sub-range splits the set again
        set.remove(kotlin.ranges.RangesKt.rangeTo(huge - 3, huge + 1))
        set as List == [
                kotlin.ranges.RangesKt.rangeTo(huge - 10, huge - 4),
                kotlin.ranges.RangesKt.rangeTo(huge + 2, huge + 5)
        ]
    }
}
