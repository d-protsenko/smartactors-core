package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionTwoArgs;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Queue;

/**
 * Support class for initialization writer of {@link PrintToFileEventHandler}
 */
class PrintToFileWriterParameters {

    private Queue<IEvent> queue;
    private Map<String, IActionTwoArgs<IEvent, PrintWriter>> executors;
    private IActionTwoArgs<IEvent, PrintWriter> defaultExecutor;
    private String fileName;

    /**
     * Default constructor
     */
    private PrintToFileWriterParameters() {
    }

    /**
     * The constructor
     * @param queue the queue
     * @param executors the list of executors
     * @param defaultExecutor the default executor
     * @param fileName the name of file
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    PrintToFileWriterParameters(
            final Queue<IEvent> queue,
            final Map<String, IActionTwoArgs<IEvent, PrintWriter>> executors,
            final IActionTwoArgs<IEvent, PrintWriter> defaultExecutor,
            final String fileName
    ) throws InvalidArgumentException {
        if (null == queue || null == executors || null == defaultExecutor || null == fileName) {
            throw new InvalidArgumentException("All arguments should be not null.");
        }
        this.queue = queue;
        this.executors = executors;
        this.defaultExecutor = defaultExecutor;
        this.fileName = fileName;
    }

    Queue<IEvent> getQueue() {
        return queue;
    }

    Map<String, IActionTwoArgs<IEvent, PrintWriter>> getExecutors() {
        return executors;
    }

    IActionTwoArgs<IEvent, PrintWriter> getDefaultExecutor() {
        return defaultExecutor;
    }

    String getFileName() {
        return fileName;
    }
}
