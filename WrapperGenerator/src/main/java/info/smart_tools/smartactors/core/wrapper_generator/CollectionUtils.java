package info.smart_tools.smartactors.core.wrapper_generator;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Facade class for methods which works with collections
 */
public class CollectionUtils {

    /**
     * Create new array list by given elements
     * @param elements
     * @param <E>
     * @return
     */
    public static <E> ArrayList<E> newArrayList(final Iterable<? extends E> elements) {
        return Lists.newArrayList(elements);
    }

    /**
     *
     * @param elements
     * @param <E>
     * @return
     */
    public static <E> ArrayList<E> newArrayList(final E ... elements) {
        return Lists.newArrayList(elements);
    }
}
