package info.smart_tools.smartactors.core.wrapper_generator;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Facade class for methods which works with collections
 */
public final class CollectionUtils {

    /**
     * Utility class should not have a public default constructor
     */
    private CollectionUtils() {
    }

    /**
     * Create new array list by given iterated elements
     * @param elements given elements
     * @param <E> type of elements
     * @return new instance of {@link ArrayList}
     */
    public static <E> ArrayList<E> newArrayList(final Iterable<? extends E> elements) {
        return Lists.newArrayList(elements);
    }

    /**
     * Create new array list by given list of elements
     * @param elements given elements
     * @param <E> type of given elements
     * @return new instance of {@link ArrayList}
     */
    public static <E> ArrayList<E> newArrayList(final E ... elements) {
        return Lists.newArrayList(elements);
    }
}
