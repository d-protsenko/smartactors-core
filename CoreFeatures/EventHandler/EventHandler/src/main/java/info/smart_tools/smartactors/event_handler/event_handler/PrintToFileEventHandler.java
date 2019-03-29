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

public class PrintToFileEventHandler implements IEventHandler, IExtendedEventHandler {

    private String name;
    private Queue<IEvent> queue = new ConcurrentLinkedQueue<>();
    private ReentrantLock writeLock = new ReentrantLock();
    private static final String FILENAME = "server.log";

    private Map<String, IActionTwoArgs<IEvent, PrintWriter>> executors = new HashMap<String, IActionTwoArgs<IEvent, PrintWriter>>() {{
        put(
                Exception.class.getCanonicalName(), (event, writer) -> {
                    ((Exception) event.getBody()).printStackTrace(writer);
                }
        );
    }};

    public PrintToFileEventHandler(final String name) {
        this.name = name;
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
    public String getName() {
        return this.name;
    }

    @Override
    public void addExecutor(final Object key, final Object executor)
            throws ExtendedEventHandlerException {
        String castedKey = castKey(key);
        IActionTwoArgs<IEvent, PrintWriter> castedExecutor = castExecutor(executor);

        executors.put(castedKey, castedExecutor);
    }

    @Override
    public Object removeExecutor(final Object key)
            throws ExtendedEventHandlerException {
        String castedKey = castKey(key);

        return executors.remove(castedKey);
    }

    private void writeToFile()
            throws IOException, EventHandlerException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILENAME, true))) {
            while (!this.queue.isEmpty()) {
                IEvent event = queue.poll();
                String eventType = event.getType();
                IActionTwoArgs<IEvent, PrintWriter> exec = executors.getOrDefault(eventType, (et, pw) -> {
                    pw.println(et.toString());
                });
                try {
                    exec.execute(event, writer);
                } catch (Exception e) {
                    throw new EventHandlerException(
                            String.format("Event handler action '%s' throws exception.", eventType),
                            e
                    );
                }
            }
        }
    }

    private String castKey(final Object key)
            throws ExtendedEventHandlerException {
        try {
            return  (String) key;
        } catch (Exception e) {
            throw new ExtendedEventHandlerException("Could not cast key to String.");
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
