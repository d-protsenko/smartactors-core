package info.smart_tools.smartactors.statistics.sensors.embedded_sensor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.statistics.sensors.embedded_sensor.interfaces.IEmbeddedSensorStrategy;
import info.smart_tools.smartactors.statistics.sensors.embedded_sensor.interfaces.exceptions.EmbeddedSensorStrategyException;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITime;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimer;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class EmbeddedSensorReceiver implements IMessageReceiver {
    private static final long DEFAULT_COMMIT_DELAY = 1000;

    private long observationStart;
    private long observationPeriod;
    private long maxPeriodItems;
    private IEmbeddedSensorStrategy<?> strategy;
    private final AtomicReference<ObservationPeriod<?>> currentPeriod = new AtomicReference<>();
    private Object statisticsChainId;

    private final ITimer timer;
    private final ITime systemTime;
    private long commitDelay = DEFAULT_COMMIT_DELAY;

    private final IFieldName periodFieldName;
    private final IFieldName startFieldName;
    private final IFieldName limitFieldName;
    private final IFieldName strategyFieldName;
    private final IFieldName statisticsChainFieldName;
    private final IFieldName periodStartFieldName;
    private final IFieldName periodEndFieldName;
    private final IFieldName dataFieldName;

    /**
     * Class that stores data of a single observation period.
     *
     * @param <TState>    type of observation period state specific for sensor strategy.
     */
    private final class ObservationPeriod<TState> {
        private final long periodStart;
        private final long periodEnd;
        private final long maxPeriodItems;
        private final AtomicLong curItems;
        private final TState state;
        private final IEmbeddedSensorStrategy<TState> strategy;

        ObservationPeriod(final long start, final long end, final long nItems, final IEmbeddedSensorStrategy<TState> strategy)
                throws EmbeddedSensorStrategyException {
            this.periodStart = start;
            this.periodEnd = end;
            this.maxPeriodItems = nItems;
            this.curItems = new AtomicLong(0);
            this.state = strategy.initPeriod();
            this.strategy = strategy;
        }

        ObservationPeriod<TState> nextPeriod(final long inclTime)
                throws EmbeddedSensorStrategyException {
            long duration = periodEnd - periodStart;
            // Skip some periods where we had no messages
            long skip = (inclTime - periodEnd) / duration;
            skip = (skip > 0) ? skip : 0;
            return new ObservationPeriod<>(periodEnd + skip * duration, periodEnd + (skip + 1) * duration, maxPeriodItems, strategy);
        }

        boolean isCountLimited() {
            return maxPeriodItems > 0;
        }

        boolean isStartedAt(final long time) {
            return periodStart >= time;
        }

        boolean isTimeCompleted(final long time) {
            return time > periodEnd;
        }

        private Collection<? extends Number> extractData()
                throws EmbeddedSensorStrategyException, InvalidArgumentException {
            return strategy.extractPeriod(state);
        }

        /**
         * @return {@code true} if items limit exceeded
         */
        private boolean incrementCount() {
            return maxPeriodItems > 0 && curItems.incrementAndGet() > maxPeriodItems;
        }

        void recordProcessor(final IMessageProcessor mp, final long time)
                throws TaskScheduleException, EmbeddedSensorStrategyException, InvalidArgumentException {
            if (isCountLimited() && !isStartedAt(time)) {
                return;
            }

            if (!incrementCount()) {
                strategy.updatePeriod(state, mp, time);
            } else {
                commitPeriod(this, nextPeriod(time));
            }
        }

        IObject fillInMessage()
                throws ResolutionException, ChangeValueException, InvalidArgumentException, EmbeddedSensorStrategyException {
            IObject message = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            message.setValue(periodStartFieldName, periodStart);
            message.setValue(periodEndFieldName, periodEnd);
            message.setValue(dataFieldName, extractData());
            return message;
        }
    }

    /**
     * The constructor.
     *
     * @param args    description of the sensor to create
     * @throws ResolutionException if error occurs resolving any dependencies
     * @throws ReadValueException if error occurs reading arguments object
     * @throws InvalidArgumentException if something unexpected occurs suddenly
     * @throws EmbeddedSensorStrategyException if error occurs in sensor strategy while initializing first observation period
     */
    public EmbeddedSensorReceiver(final IObject args)
            throws ResolutionException, ReadValueException, InvalidArgumentException, EmbeddedSensorStrategyException {
        periodFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "period");
        startFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "start");
        limitFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "limit");
        strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");
        statisticsChainFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "statisticsChain");
        periodStartFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "periodStart");
        periodEndFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "periodEnd");
        dataFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "data");

        timer = IOC.resolve(Keys.getOrAdd("timer"));
        systemTime = IOC.resolve(Keys.getOrAdd("time"));

        observationPeriod = Duration.parse((String) args.getValue(periodFieldName)).toMillis();

        String start = (String) args.getValue(startFieldName);
        observationStart = (start == null)
                ? systemTime.currentTimeMillis()
                : LocalDateTime.parse(start).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

        Number maxItems = (Number) args.getValue(limitFieldName);
        maxPeriodItems = (maxItems == null) ? -1 : maxItems.longValue();

        strategy = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), args.getValue(strategyFieldName)), args);
        statisticsChainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), args.getValue(statisticsChainFieldName));

        currentPeriod.set(new ObservationPeriod<>(observationStart, observationStart + observationPeriod, maxPeriodItems, strategy));
    }

    private void commitPeriod(final ObservationPeriod<?> period, final ObservationPeriod<?> nextPeriod)
            throws TaskScheduleException {
        if (currentPeriod.compareAndSet(period, nextPeriod)) {
            // Message is created and sent with some delay to let all threads write data to the old period
            timer.schedule(() -> {
                try {
                    MessageBus.send(period.fillInMessage(), statisticsChainId);
                } catch (SendingMessageException | ResolutionException | InvalidArgumentException | ChangeValueException
                        | EmbeddedSensorStrategyException e) {
                    throw new TaskExecutionException(e);
                }
            }, systemTime.currentTimeMillis() + commitDelay);
        }
    }

    private ObservationPeriod<?> getCurrentPeriod(final long time)
            throws TaskScheduleException, EmbeddedSensorStrategyException {
        while (true) {
            ObservationPeriod<?> period = currentPeriod.get();

            if (period.isTimeCompleted(time)) {
                commitPeriod(period, period.nextPeriod(time));
                continue;
            }

            return period;
        }
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException, AsynchronousOperationException {
        try {
            final long cTime = systemTime.currentTimeMillis();
            getCurrentPeriod(cTime).recordProcessor(processor, cTime);
        } catch (TaskScheduleException | EmbeddedSensorStrategyException | InvalidArgumentException e) {
            throw new MessageReceiveException(e);
        }
    }
}
