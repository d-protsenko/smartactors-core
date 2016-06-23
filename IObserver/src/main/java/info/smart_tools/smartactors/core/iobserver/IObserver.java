package info.smart_tools.smartactors.core.iobserver;

import info.smart_tools.smartactors.core.iobserver.exception.ObserverExecuteException;

/**
 * Interface IObserver
 * @param <T> type of observable object
 */
public interface IObserver<T> {

    /**
     * Action for observable object
     * @param observable observable object
     * @throws ObserverExecuteException if any errors occurred
     */
    void execute(final T observable)
            throws ObserverExecuteException;
}
