package com.github.jonpeterson.kotlin.ranges

import java.util.*

/**
 * Base class for implementing a [MutableSet] of [ClosedRange]s.
 *
 * Ranges in the set will always be in a normalized state. This means that ranges are kept in order and overlapping or
 * adjacent ranges will be joined.
 *
 * Some operations on this are not designed to be thread-safe.
 *
 * @param T the type of ranges in this set
 */
abstract class RangeSet<T: Comparable<T>> : MutableSet<ClosedRange<T>>, Cloneable {
    private val ranges = LinkedList<ClosedRange<T>>()

    /**
     * Creates a set with no ranges.
     */
    constructor()

    /**
     * Creates a set with ranges added.
     *
     * @property ranges ranges to normalize and add
     */
    constructor(ranges: List<ClosedRange<T>>) {
        addAll(ranges)
    }

    /**
     * Creates a set with ranges shallow-copied from another [RangeSet].
     *
     * @property rangeSet set to copy ranges from
     */
    protected constructor(rangeSet: RangeSet<T>) {
        ranges.addAll(rangeSet)
    }

    /**
     * @return number of ranges
     */
    // TODO: test
    override val size: Int
        get() = ranges.size

    /**
     * @param rangeValue value to determine presence of
     * @return whether a value is contained by any of the ranges in the set
     */
    // TODO: test
    fun contains(rangeValue: T): Boolean = ranges.any { it.contains(rangeValue) }

    /**
     * @param element range to determine presence of
     * @return whether a range is contained within any of the ranges in the set
     */
    // TODO: test
    override fun contains(element: ClosedRange<T>): Boolean {
        ranges.forEach { range ->
            if(element.start.compareTo(range.start) >= 0 && element.endInclusive.compareTo(range.endInclusive) <= 0)
                return true
        }

        return false
    }

    /**
     * @param elements ranges to determine presence of
     * @return whether ranges are all contained within any of the ranges in the set
     */
    // TODO: test
    override fun containsAll(elements: Collection<ClosedRange<T>>): Boolean = elements.all { contains(it) }

    /**
     * @return whether the set contains any elements
     */
    override fun isEmpty(): Boolean = ranges.isEmpty()

    /**
     * Adds a range to the set.
     *
     * The set is normalized (see class-level documentation for the definition of normalization
     * used here) during insertion.
     *
     * @param element range to add
     * @return whether any values were added; false if every value of the new range were already in a range in the set
     */
    override fun add(element: ClosedRange<T>): Boolean {
        var new = element
        var addIndex = -1

        val iterator = ranges.iterator()
        while(iterator.hasNext()) {
            val existing = iterator.next()
            addIndex++

            // existing:       |---|
            //      new: |---|
            //   result: |---| |---|
            //
            // add before existing
            if(new.endInclusive.compareTo(decrementValue(existing.start)) < 0) {
                ranges.add(addIndex, new)
                return true
            }

            // existing:   |--?
            //      new: |----?
            if(new.start.compareTo(existing.start) < 0) {

                // existing:   |---|
                //      new: |---|
                //   result: |-----|
                //
                // redefine new
                if(new.endInclusive.compareTo(existing.endInclusive) < 0)
                    new = createRange(new.start, existing.endInclusive)

                // existing:   |---|
                //      new: |-------|
                //   result: |-------|

                // remove existing, update position of where to insert new, and continue
                iterator.remove()
                addIndex--
                continue
            }

            // existing: |-------|
            //      new:   |---|
            //   result: |-------|
            //
            // range already contained in set, no need to modify anything
            if(new.endInclusive.compareTo(existing.endInclusive) <= 0)
                return false

            // existing: |---|
            //      new:   |---|
            //   result: |-----|
            //
            // redefine new, remove existing, and update position of where to insert new
            if(new.start.compareTo(incrementValue(existing.endInclusive)) <= 0) {
                new = createRange(existing.start, new.endInclusive)
                iterator.remove()
                addIndex--
            }
        }

        // existing: |---|
        //      new:       |---|
        //   result: |---| |---|
        //
        // add to the end
        ranges.add(new)
        return true
    }

    /**
     * Adds multiple ranges to the set.
     *
     * The set is normalized (see class-level documentation for the definition of normalization
     * used here) during insertion.
     *
     * @param elements ranges to add
     * @return whether any values were added; false if every value of every new range were already in a range in the set
     */
    override fun addAll(elements: Collection<ClosedRange<T>>): Boolean {
        return elements.map { add(it) }.any()
    }

    // TODO: document
    override fun clear() {
        ranges.clear()
    }

    // TODO: document
    override fun remove(element: ClosedRange<T>): Boolean {
        var changed = false

        val iterator = ranges.listIterator()
        while(iterator.hasNext()) {
            val existing = iterator.next()

            // existing:       |---|
            //   remove: |---|
            //   result:       |---|
            //
            // current existing is past the remove; stop iterating
            if(element.endInclusive.compareTo(existing.start) < 0)
                break

            // existing: |---|
            //   remove:       |---|
            //   result: |---|
            //
            // existing not effected; move to next existing
            if(element.start.compareTo(existing.endInclusive) > 0)
                continue

            val removeFromStart = element.start.compareTo(existing.start) <= 0
            val removeFromEnd = element.endInclusive.compareTo(existing.endInclusive) >= 0
            iterator.remove()
            changed = true

            // existing: |-----|
            //   remove:     |---|
            //   result: |--|
            //
            // existing: |---------|
            //   remove:    |---|
            //   result: |-|
            //
            // not removing start, so add that back
            if(!removeFromStart)
                iterator.add(createRange(existing.start, decrementValue(element.start)))

            // existing:   |-----|
            //   remove: |---|
            //   result:      |--|
            //
            // existing: |---------|
            //   remove:    |---|
            //   result:         |-|
            //
            // not removing end, so add that back
            if(!removeFromEnd)
                iterator.add(createRange(incrementValue(element.endInclusive), existing.endInclusive))
        }

        return changed
    }

    // TODO: document, test
    override fun removeAll(elements: Collection<ClosedRange<T>>): Boolean {
        return elements.map { remove(it) }.any()
    }

    // TODO: document
    fun retain(element: ClosedRange<T>): Boolean {
        var changed = false

        val iterator = ranges.listIterator()
        while(iterator.hasNext()) {
            val existing = iterator.next()

            val removeFromStart = element.start.compareTo(existing.start) > 0
            val removeFromEnd = element.endInclusive.compareTo(existing.endInclusive) < 0

            // existing:   |---|
            //   retain: |-------|
            //   result:   |---|
            //
            // existing not effected; move to next existing
            if(!removeFromStart && !removeFromEnd)
                continue

            iterator.remove()
            changed = true

            // existing:       |---|
            //   retain: |---|
            //   result:
            //
            // current existing is past the retain; remove the rest
            if(element.endInclusive.compareTo(existing.start) < 0) {
                iterator.forEach { iterator.remove() }
                break
            }

            // existing: |---|
            //   retain:       |---|
            //   result:
            //
            // nothing to add back in; continue to next existing
            if(element.start.compareTo(existing.endInclusive) > 0)
                continue

            // existing: |-------|
            //   retain:   |---|
            //   result:   |---|
            //
            // existing: |-----|
            //   retain:   |-----|
            //   result:   |---|
            //
            // existing:   |-----|
            //   retain: |-----|
            //   result:   |---|
            //
            // overlap; add a range back in
            iterator.add(createRange(
                if(removeFromStart) element.start else existing.start,
                if(removeFromEnd) element.endInclusive else existing.endInclusive)
            )
        }

        return changed
    }

    // TODO: document
    override fun retainAll(elements: Collection<ClosedRange<T>>): Boolean {
        val unnormalizedRanges = elements.map { element ->
            val clone = clone()
            clone.retain(element)
            clone.ranges
        }

        val shallowRangesCopy = LinkedList(ranges)

        clear()
        unnormalizedRanges.forEach { addAll(it) }

        return shallowRangesCopy != ranges
    }

    // TODO: document
    override fun iterator(): MutableIterator<ClosedRange<T>> = ranges.iterator()

    override fun hashCode(): Int {
        return ranges.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is RangeSet<*> && ranges == other.ranges)
    }

    // TODO: document
    protected abstract fun createRange(start: T, endInclusive: T): ClosedRange<T>

    // TODO: document
    protected abstract fun incrementValue(value: T): T

    // TODO: document
    protected abstract fun decrementValue(value: T): T

    // TODO: document
    override abstract fun clone(): RangeSet<T>
}