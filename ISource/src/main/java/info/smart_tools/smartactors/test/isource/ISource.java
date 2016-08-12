package info.smart_tools.smartactors.test.isource;

import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;

/**
 * Interface {@link ISource}.
 */
public interface ISource extends Iterable {

    /**
     * Set source of test data
     * @param t the source of test data
     * @param <T> the type of source data
     */
    <T> void setSource(T t) throws SourceExtractionException;
}
