package info.smart_tools.smartactors.core.proof_of_assumption.old_generator;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Facade class for methods which works with collections
 */
public class CollectionUtils {

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        return Lists.newArrayList(elements);
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        return Lists.newArrayList(elements);
    }
}
