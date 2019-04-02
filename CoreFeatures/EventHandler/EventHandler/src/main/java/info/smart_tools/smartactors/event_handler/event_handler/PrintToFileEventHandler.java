package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IActionTwoArgs;
import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;

import java.io.FileWriter;
import java.io.IOException;
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

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToFileEventHandler}
     * @param defaultExecutor the default executor for event processing
     */
    public PrintToFileEventHandler(
            final String eventHandlerKey,
            final IActionTwoArgs<IEvent, PrintWriter> defaultExecutor
    ) {
        this.eventHandlerKey = eventHandlerKey;
        this.defaultExecutor = defaultExecutor;
    }

    /**
     * The constructor
     * @param eventHandlerKey the key of created instance of {@link PrintToFileEventHandler}
     * @param defaultExecutor the default executor for event processing
     * @param executors initialization map of executors
     */
    public PrintToFileEventHandler(
            final String eventHandlerKey,
            final IActionTwoArgs<IEvent, PrintWriter> defaultExecutor,
            final Map<Object, Object> executors
    ) {
        this.eventHandlerKey = eventHandlerKey;
        this.defaultExecutor = defaultExecutor;

        executors.forEach((type, executor) -> {
            try {
                this.addExecutor(type, executor);
            } catch (Exception e) {
                throw new RuntimeException("One of the executors cannot be casted to a specified type");
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
                } catch (Exception e) {
                    throw new EventHandlerException("PrintToFileEventHandler: Error on saving data into a log file.");
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
            throws IOException, EventHandlerException {
        try (
                PrintWriter writer = new PrintWriter(
                        new FileWriter(FILENAME, true)
                )
        ) {
            while (!this.queue.isEmpty()) {
                IEvent event = queue.poll();
                String eventType = event.getType();
                IActionTwoArgs<IEvent, PrintWriter> exec = executors.getOrDefault(eventType, this.defaultExecutor);
                try {
                    exec.execute(event, writer);
                } catch (Exception e) {
                    throw new EventHandlerException(
                            String.format("Event handler executor '%s' throws exception.", eventType),
                            e
                    );
                }
            }
        }
    }

    private String castEventType(final Object eventType)
            throws ExtendedEventHandlerException {
        try {
            return  (String) eventType;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast event type to String.");
        }
    }

    @SuppressWarnings("unchecked")
    private IActionTwoArgs<IEvent, PrintWriter> castExecutor(final Object executor)
            throws ExtendedEventHandlerException {
        try {
            return  (IActionTwoArgs<IEvent, PrintWriter>) executor;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast executor to IActionTwoArgs<IEvent, PrintWriter>.");
        }
    }
}
