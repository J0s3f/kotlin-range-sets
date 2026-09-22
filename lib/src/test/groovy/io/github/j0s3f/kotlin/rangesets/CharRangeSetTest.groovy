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
class CharRangeSetTest extends Specification {

    private static kotlin.ranges.CharRange charRange(String from, String to) {
        return new kotlin.ranges.CharRange(from.charAt(0), to.charAt(0))
    }

    private static List<kotlin.ranges.CharRange> charRanges(List<List<String>> ranges) {
        return ranges.collect { range -> charRange(range[0], range[1]) }
    }


    def 'containsValue: #value'() {
        expect:
        def set = new CharRangeSet(charRanges([['c', 'e'], ['g', 'i'], ['m', 'p']]))
        def emptySet = new CharRangeSet()
        set.containsValue(value.charAt(0)) == contained
        emptySet.containsValue(value.charAt(0)) == false

        where:
        value | contained
        'a'   | false
        'b'   | false
        'c'   | true
        'd'   | true
        'e'   | true
        'f'   | false
        'g'   | true
        'h'   | true
        'i'   | true
        'j'   | false
        'k'   | false
        'l'   | false
        'm'   | true
        'n'   | true
        'o'   | true
        'p'   | true
        'q'   | false
    }

    def 'containsAll: #contains'() {
        expect:
        def set = new CharRangeSet(charRanges([['c', 'e'], ['g', 'i'], ['m', 'p']]))
        def emptySet = new CharRangeSet()
        def ranges = charRanges(contains)
        set.containsAll(ranges) == contained
        emptySet.containsAll(ranges) == false

        where:
        contains                                    | contained
        [['a', 'b']]                                | false
        [['c', 'c']]                                 | true
        [['c', 'e']]                                 | true
        [['c', 'f']]                                 | false
        [['l', 'n']]                                 | false
        [['n', 'o']]                                 | true
        [['m', 'p']]                                 | true
        [['m', 'q']]                                 | false
        [['l', 'p']]                                 | false
        [['c', 'e'], ['h', 'h'], ['n', 'o']]         | true
        [['c', 'f'], ['h', 'h'], ['n', 'o']]         | false
        [['b', 'q']]                                 | false
    }

    def 'addAll: #adds'() {
        expect:
        def set = new CharRangeSet()
        set.addAll(charRanges(adds)) == true
        set as List == charRanges(expected)

        where:
        adds                                                          | expected
        [['c', 'e'], ['c', 'e']]                                      | [['c', 'e']]
        [['c', 'e'], ['a', 'e']]                                      | [['a', 'e']]
        [['c', 'e'], ['c', 'g']]                                      | [['c', 'g']]
        [['c', 'e'], ['a', 'd']]                                      | [['a', 'e']]
        [['c', 'e'], ['d', 'g']]                                      | [['c', 'g']]
        [['c', 'e'], ['b', 'g']]                                      | [['b', 'g']]
        [['b', 'g'], ['c', 'e']]                                      | [['b', 'g']]
        [['c', 'e'], ['g', 'i'], ['d', 'h']]                          | [['c', 'i']]
        [['c', 'e'], ['h', 'i'], ['m', 'p'], ['j', 'k'], ['f', 'f']]  | [['c', 'f'], ['h', 'k'], ['m', 'p']]
        [['a', 'b'], ['0', '0'], ['0', '1'], ['0', '0'], ['a', 'b']]  | [['0', '1'], ['a', 'b']]
    }

    def 'removeAll: #removes'() {
        expect:
        def set = new CharRangeSet(charRanges([['c', 'e'], ['g', 'i'], ['m', 'p']]))
        def emptySet = new CharRangeSet()
        def ranges = charRanges(removes)
        set.removeAll(ranges) == removed
        emptySet.removeAll(ranges) == false
        set as List == charRanges(expected)
        emptySet as List == []

        where:
        removes                             | removed | expected
        [['a', 'b']]                        | false   | [['c', 'e'], ['g', 'i'], ['m', 'p']]
        [['f', 'f']]                        | false   | [['c', 'e'], ['g', 'i'], ['m', 'p']]
        [['j', 'l']]                        | false   | [['c', 'e'], ['g', 'i'], ['m', 'p']]
        [['a', 'c']]                        | true    | [['d', 'e'], ['g', 'i'], ['m', 'p']]
        [['a', 'd']]                        | true    | [['e', 'e'], ['g', 'i'], ['m', 'p']]
        [['a', 'e']]                        | true    | [['g', 'i'], ['m', 'p']]
        [['n', 'o']]                        | true    | [['c', 'e'], ['g', 'i'], ['m', 'm'], ['p', 'p']]
        [['a', 'b'], ['j', 'l']]            | false   | [['c', 'e'], ['g', 'i'], ['m', 'p']]
        [['b', 'g'], ['n', 'o']]            | true    | [['h', 'i'], ['m', 'm'], ['p', 'p']]
        [['b', 'j'], ['l', 'r']]            | true    | []
    }

    def 'retainAll: #retains'() {
        expect:
        def set = new CharRangeSet(charRanges([['c', 'g'], ['l', 'p'], ['v', 'z']]))
        def emptySet = new CharRangeSet()
        def ranges = charRanges(retains)
        set.retainAll(ranges) == changed
        emptySet.retainAll(ranges) == false
        set as List == charRanges(expected)
        emptySet as List == []

        where:
        retains                                            | changed | expected
        [['c', 'g']]                                       | true    | [['c', 'g']]
        [['b', 'g']]                                       | true    | [['c', 'g']]
        [['c', 'h']]                                       | true    | [['c', 'g']]
        [['b', 'h']]                                       | true    | [['c', 'g']]
        [['c', 'z']]                                       | false   | [['c', 'g'], ['l', 'p'], ['v', 'z']]
        [['b', '{']]                                       | false   | [['c', 'g'], ['l', 'p'], ['v', 'z']]
        [['d', 'y']]                                       | true    | [['d', 'g'], ['l', 'p'], ['v', 'y']]
        [['n', 'w']]                                       | true    | [['n', 'p'], ['v', 'w']]
        [['c', 'g'], ['l', 'p'], ['v', 'z']]                | false   | [['c', 'g'], ['l', 'p'], ['v', 'z']]
    }

    def 'differenceAll: #set'() {
        expect:
        new CharRangeSet(charRanges(set)).differenceAll(charRanges(difference)).toList() == charRanges(expected)

        where:
        set                                        | difference                    | expected
        []                                         | []                            | []
        []                                         | [['a', 'b']]                  | [['a', 'b']]
        [['c', 'g']]                                | []                            | []
        [['c', 'g']]                                | [['d', 'f']]                  | []
        [['c', 'g']]                                | [['c', 'g']]                  | []
        [['c', 'g']]                                | [['b', 'h']]                  | [['b', 'b'], ['h', 'h']]
        [['c', 'g'], ['l', 'p'], ['v', 'z']]        | []                            | []
        [['c', 'g'], ['l', 'p'], ['v', 'z']]        | [['e', 'n']]                  | [['h', 'k']]
    }

    def 'gaps: #set'() {
        expect:
        new CharRangeSet(charRanges(set)).gaps().toList() == charRanges(expected)

        where:
        set                                     | expected
        []                                      | []
        [['c', 'g']]                            | []
        [['c', 'g'], ['l', 'p'], ['v', 'z']]    | [['h', 'k'], ['q', 'u']]
    }

    def 'hashCode/equals: #ranges'() {
        expect:
        def setA = new CharRangeSet(charRanges([['a', 'c'], ['g', 'j'], ['o', 'o']]))
        def setB = new CharRangeSet(charRanges(ranges))
        (setA.hashCode() == setB.hashCode()) == equal
        (setA == setB) == equal

        where:
        ranges                                        | equal
        [['a', 'c'], ['g', 'j'], ['o', 'o']]           | true
        [['b', 'c'], ['g', 'j'], ['o', 'o']]           | false
        [['g', 'j'], ['a', 'c'], ['o', 'o']]           | true
    }

    def 'supports numeric (digit) characters: #value'() {
        expect:
        def set = new CharRangeSet(charRanges([['0', '3'], ['5', '7']]))
        set.containsValue(value.charAt(0)) == contained

        where:
        value | contained
        '0'   | true
        '2'   | true
        '3'   | true
        '4'   | false
        '5'   | true
        '7'   | true
        '8'   | false
    }

    def 'combines and modifies ranges of digit characters'() {
        expect:
        def set = new CharRangeSet(charRanges([['0', '3'], ['5', '7']]))

        // adjacent digit ranges are coalesced
        set.add(charRange('4', '4'))
        set as List == charRanges([['0', '7']])

        // digits can be removed like any other character
        set.remove(charRange('2', '5'))
        set as List == charRanges([['0', '1'], ['6', '7']])

        // and retained
        set.retain(charRange('1', '6'))
        set as List == charRanges([['1', '1'], ['6', '6']])
    }

    def 'throws when adding a range that would decrement past the minimum value'() {
        when:
        new CharRangeSet().add(new kotlin.ranges.CharRange((char) 0, 'e' as char))

        then:
        thrown(IllegalArgumentException)
    }

    def 'throws when adding a range that would increment past the maximum value'() {
        when:
        new CharRangeSet().add(new kotlin.ranges.CharRange('a' as char, Character.MAX_VALUE))

        then:
        thrown(IllegalArgumentException)
    }
}
