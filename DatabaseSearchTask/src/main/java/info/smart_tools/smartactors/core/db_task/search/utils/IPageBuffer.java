package info.smart_tools.smartactors.core.db_task.search.utils;

import info.smart_tools.smartactors.core.iobject.IObject;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 */
public interface IPageBuffer {
    /**
     *
     * @param pageNumber
     * @param objects
     */
    void save(final int pageNumber, @Nonnull final List<IObject> objects);

    /**
     *
     * @param pageNumber
     * @return
     */
    List<IObject> get(final int pageNumber);

    /**
     *
     * @return
     */
    int size();

    /**
     *
     * @return
     */
    int maxSize();

    /**
     *
     * @return
     */
    boolean isEmpty();
}
