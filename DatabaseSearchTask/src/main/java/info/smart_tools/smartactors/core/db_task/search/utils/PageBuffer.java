package info.smart_tools.smartactors.core.db_task.search.utils;

import info.smart_tools.smartactors.core.iobject.IObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PageBuffer implements IPageBuffer {
    /**  */
    private int maxSize;
    /**  */
    private NavigableMap<Integer, List<IObject>> buffer;

    /**
     *
     * @param bufferMaxSize
     */
    private PageBuffer(final int bufferMaxSize) {
        maxSize = bufferMaxSize;
        buffer = new TreeMap<>();
    }

    /**
     *
     * @param bufferSize
     * @return
     */
    public static PageBuffer create(final int bufferSize) {
        if (bufferSize == 0)
            throw new IllegalArgumentException("Buffer size should more than a zero!");

        return new PageBuffer(bufferSize);
    }

    /**
     *
     * @param pageNumber
     * @param objects
     */
    @Override
    public void save(final int pageNumber, @Nonnull final List<IObject> objects) {
        if (buffer.size() == maxSize)
            buffer.remove(buffer.firstKey());

        buffer.put(pageNumber, objects);
    }

    /**
     *
     * @param pageNumber
     * @return
     */
    @Override
    public List<IObject> get(final int pageNumber) {
        return buffer.get(pageNumber);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     *
     * @return
     */
    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
