package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link IEventHandler} which output data of {@link IEvent} to the file.
 */
public class PrintToFileEventHandler implements IEventHandler, IExtendedEventHandler {

    private String eventHandlerKey;
    private Queue<IEvent> queue = new ConcurrentLinkedQueue<>();
    private ReentrantLock writeLock = new ReentrantLock();
    private static final String FILENAME = "server.log";
    private IActionTwoArgs<IEvent, PrintWriter> defaultExecutor;
    private Map<String, IActionTwoArgs<IEvent, PrintWriter>> executors = new HashMap<>();

    // Default writer
    private IAction<PrintToFileWriterParameters>  writer = (params) -> {
        IEvent event = null;
        try (PrintWriter writer = new PrintWriter(new FileWriter(params.getFileName(), true))) {
            while (!params.getQueue().isEmpty()) {
                event = params.getQueue().poll();
                String eventType = event.getBody().getClass().getCanonicalName();
                IActionTwoArgs<IEvent, PrintWriter> exec = params
                        .getExecutors()
                        .getOrDefault(eventType, params.getDefaultExecutor());
                exec.execute(event, writer);
            }
        } catch (Exception e) {
            throw new EventHandlerException(
                    "PrintToFileEventHandler: Could not execute action of file writer .", e, event
            );
        }
    };

     /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToFileEventHandler}
     * @param writer the action for writing event data
     * @param defaultExecutor the default executor for event processing
     */
    public PrintToFileEventHandler(
            final String eventHandlerKey,
            final IAction<PrintToFileWriterParameters> writer,
            final IActionTwoArgs<IEvent, PrintWriter> defaultExecutor
    ) {
        this.eventHandlerKey = eventHandlerKey;
        if (null != writer) {
            this.writer = writer;
        }
        this.defaultExecutor = defaultExecutor;
    }

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToFileEventHandler}
     * @param writer the action for writing event data
     * @param defaultExecutor the default executor for event processing
     * @param executors initialization map of executors
     */
    public PrintToFileEventHandler(
            final String eventHandlerKey,
            final IAction<PrintToFileWriterParameters> writer,
            final IActionTwoArgs<IEvent, PrintWriter> defaultExecutor,
            final Map<Object, Object> executors
    ) {
        this.eventHandlerKey = eventHandlerKey;
        if (null != writer) {
            this.writer = writer;
        }
        this.defaultExecutor = defaultExecutor;

        executors.forEach((type, executor) -> {
            try {
                this.addExecutor(type, executor);
            } catch (Exception e) {
                throw new RuntimeException(
                        "PrintToFileEventHandler: One of the executors cannot be casted to a specified type", e
                );
            }
        });
    }

    @Override
    public void handle(final IEvent event)
             throws EventHandlerException {
        if (event != null) {
            this.queue.offer(event);
        }
        if (writeLock.tryLock()) {
            if (writeLock.isHeldByCurrentThread()) {
                try {
                    writeToFile();
                } catch (EventHandlerException e) {
                    throw e;
                } catch (Exception e) {
                    throw new EventHandlerException(
                            "PrintToFileEventHandler: Error on saving data into a log file.", e
                    );
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }

    @Override
    public String getEventHandlerKey() {

        return this.eventHandlerKey;
    }

    @Override
    public void addExecutor(final Object eventType, final Object executor)
            throws ExtendedEventHandlerException {
        String castedEventType = castEventType(eventType);
        IActionTwoArgs<IEvent, PrintWriter> castedExecutor = castExecutor(executor);

        executors.put(castedEventType, castedExecutor);
    }

    @Override
    public Object removeExecutor(final Object eventType)
            throws ExtendedEventHandlerException {
        String castedEventType = castEventType(eventType);

        return executors.remove(castedEventType);
    }

    private void writeToFile()
            throws ActionExecutionException, InvalidArgumentException {
        this.writer.execute(
                new PrintToFileWriterParameters(this.queue, this.executors, this.defaultExecutor, FILENAME)
        );
    }

    private String castEventType(final Object eventType)
            throws ExtendedEventHandlerException {
        try {
            return  (String) eventType;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException(
                    "PrintToFileEventHandler: Could not cast event type to String.", e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private IActionTwoArgs<IEvent, PrintWriter> castExecutor(final Object executor)
            throws ExtendedEventHandlerException {
        try {
            return  (IActionTwoArgs<IEvent, PrintWriter>) executor;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException(
                    "PrintToFileEventHandler: Could not cast executor to IActionTwoArgs<IEvent, PrintWriter>.", e
            );
        }
    }
}
