package info.smart_tools.smartactors.test.isource;

import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;

import java.util.concurrent.BlockingDeque;

/**
 * Interface {@link ISource}.
 *
 * @param <T1> the type of source data
 * @param <T2> the type of queue element
 */
public interface ISource<T1, T2> {

    /**
     * Set source of test data
     * @param source the source of test data
     * @throws SourceExtractionException if any error occurs when the source is extracting
     */
    void setSource(T1 source) throws SourceExtractionException;

    /**
     * Get queue with messages
     * @return the instance of {@link BlockingDeque}
     */
    BlockingDeque<T2> getQueue();
}
