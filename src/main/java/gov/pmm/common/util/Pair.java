/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util;


import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A immutable pair of things.
 *
 * @param <S> Type of the first thing.
 * @param <T> Type of the second thing.
 */
@ToString
@EqualsAndHashCode
public final class Pair<S, T> {

    private final S first;
    private final T second;

    private Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new Pair for the given elements.
     *
     * @param first
     * @param second
     * @return
     */
    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair<S, T>(first, second);
    }

    /**
     * Returns the first element of the Pair.
     *
     * @return
     */
    public S getFirst() {
        return first;
    }

    /**
     * Returns the second element of the Pair.
     *
     * @return
     */
    public T getSecond() {
        return second;
    }
}
