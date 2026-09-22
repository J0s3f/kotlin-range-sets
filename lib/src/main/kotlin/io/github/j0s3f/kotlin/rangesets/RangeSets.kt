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

import java.math.BigInteger
import java.time.LocalDate

class IntRangeSet : RangeSet<Int> {

    constructor() : super()

    constructor(ranges: List<IntRange>) : super(ranges)

    constructor(vararg ranges: IntRange) : this(ranges.asList())

    private constructor(rangeSet: IntRangeSet) : super(rangeSet)

    override fun createRange(start: Int, endInclusive: Int): IntRange = IntRange(start, endInclusive)

    override fun incrementValue(value: Int): Int = value + 1

    override fun decrementValue(value: Int): Int = value - 1

    override fun clone(): RangeSet<Int> = IntRangeSet(this)

    override fun empty(): IntRangeSet = IntRangeSet()
}

class LongRangeSet : RangeSet<Long> {

    constructor() : super()

    constructor(ranges: List<LongRange>) : super(ranges)

    constructor(vararg ranges: LongRange) : this(ranges.asList())

    private constructor(rangeSet: LongRangeSet) : super(rangeSet)

    override fun createRange(start: Long, endInclusive: Long): LongRange = LongRange(start, endInclusive)

    override fun incrementValue(value: Long): Long = value + 1

    override fun decrementValue(value: Long): Long = value - 1

    override fun clone(): RangeSet<Long> = LongRangeSet(this)

    override fun empty(): LongRangeSet = LongRangeSet()
}

class CharRangeSet : RangeSet<Char> {

    constructor() : super()

    constructor(ranges: List<CharRange>) : super(ranges)

    constructor(vararg ranges: CharRange) : this(ranges.asList())

    private constructor(rangeSet: CharRangeSet) : super(rangeSet)

    override fun createRange(start: Char, endInclusive: Char): CharRange = CharRange(start, endInclusive)

    override fun incrementValue(value: Char): Char = value + 1

    override fun decrementValue(value: Char): Char = value - 1

    override fun clone(): RangeSet<Char> = CharRangeSet(this)

    override fun empty(): CharRangeSet = CharRangeSet()
}

class BigIntegerRangeSet : RangeSet<BigInteger> {

    constructor() : super()

    constructor(ranges: List<ClosedRange<BigInteger>>) : super(ranges)

    constructor(vararg ranges: ClosedRange<BigInteger>) : this(ranges.asList())

    private constructor(rangeSet: BigIntegerRangeSet) : super(rangeSet)

    override fun createRange(start: BigInteger, endInclusive: BigInteger): ClosedRange<BigInteger> = start..endInclusive

    override fun incrementValue(value: BigInteger): BigInteger = value + BigInteger.ONE

    override fun decrementValue(value: BigInteger): BigInteger = value - BigInteger.ONE

    override fun clone(): RangeSet<BigInteger> = BigIntegerRangeSet(this)

    override fun empty(): BigIntegerRangeSet = BigIntegerRangeSet()
}

class LocalDateRangeSet : RangeSet<LocalDate> {

    constructor() : super()

    constructor(ranges: List<ClosedRange<LocalDate>>) : super(ranges)

    constructor(vararg ranges: ClosedRange<LocalDate>) : this(ranges.asList())

    private constructor(rangeSet: LocalDateRangeSet) : super(rangeSet)

    override fun createRange(start: LocalDate, endInclusive: LocalDate): ClosedRange<LocalDate> = start..endInclusive

    override fun incrementValue(value: LocalDate): LocalDate = value.plusDays(1)

    override fun decrementValue(value: LocalDate): LocalDate = value.minusDays(1)

    override fun clone(): RangeSet<LocalDate> = LocalDateRangeSet(this)

    override fun empty(): LocalDateRangeSet = LocalDateRangeSet()
}
