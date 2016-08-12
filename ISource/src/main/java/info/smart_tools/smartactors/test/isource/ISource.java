package info.smart_tools.smartactors.test.isource;

import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;

/**
 * Interface {@link ISource}.
 *
 * @param <T> the type of source data
 */
public interface ISource<T, I> extends Iterable<I> {

    /**
     * Set source of test data
     * @param t the source of test data
     * @throws SourceExtractionException if any error occurs when the source is extracting
     */
    void setSource(T t) throws SourceExtractionException;
}
