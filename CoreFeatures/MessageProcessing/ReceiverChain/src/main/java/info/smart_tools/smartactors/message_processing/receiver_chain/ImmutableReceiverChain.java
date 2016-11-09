package info.smart_tools.smartactors.message_processing.receiver_chain;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of {@link IReceiverChain} -- immutable sequence of receivers.
 */
public class ImmutableReceiverChain implements IReceiverChain {
    private final String name;
    private final IMessageReceiver[] receivers;
    private final IObject[] arguments;
    private final Map<Class<? extends Throwable>, IObject> exceptionalChains;
    private final Set<IReceiverChain> allExceptionalChains;

    /**
     * The constructor.
     *
     * @param name                        name of the chain
     * @param receivers                   sequence (array) of receivers
     * @param arguments                   array of argument objects for receivers in the chain
     * @param exceptionalChainsAndEnv           mapping from exception class to exceptional chain to use when it occurs
     * @throws InvalidArgumentException if name is {@code null}
     * @throws InvalidArgumentException if receivers is {@code null}
     * @throws ResolutionException if cannot resolve any dependency
     * @throws ReadValueException if cannot read chains from {@code exceptionalChainsAndEnv}
     */
    public ImmutableReceiverChain(final String name, final IMessageReceiver[] receivers, final IObject[] arguments,
                                  final Map<Class<? extends Throwable>, IObject> exceptionalChainsAndEnv)
            throws InvalidArgumentException, ResolutionException, ReadValueException {
        if (null == name) {
            throw new InvalidArgumentException("Chain name should not be null.");
        }

        if (null == receivers) {
            throw new InvalidArgumentException("Chain receivers list should not be null.");
        }

        if (null == arguments) {
            throw new InvalidArgumentException("Chain arguments list should not be null.");
        }

        if (receivers.length != arguments.length) {
            throw new InvalidArgumentException("Length of arguments list  does not match length of receivers list.");
        }

        if (null == exceptionalChainsAndEnv) {
            throw new InvalidArgumentException("Exceptional chains list should not be null");
        }

        this.name = name;
        this.receivers = receivers;
        this.arguments = arguments;
        this.exceptionalChains = exceptionalChainsAndEnv;

        allExceptionalChains = new HashSet<>();

        IFieldName chainFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chain");

        for (IObject exceptionEnv : exceptionalChainsAndEnv.values()) {
            allExceptionalChains.add((IReceiverChain) exceptionEnv.getValue(chainFieldName));
        }
    }

    @Override
    public IMessageReceiver get(final int index) {
        if (index < 0 || index >= receivers.length) {
            return null;
        }

        return receivers[index];
    }

    @Override
    public IObject getArguments(final int index) {
        if (index < 0 || index >= arguments.length) {
            return null;
        }

        return arguments[index];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IObject getExceptionalChainAndEnvironments(final Throwable exception) {
        Throwable e = exception;

        do {
            for (Map.Entry<Class<? extends Throwable>, IObject> entry : this.exceptionalChains.entrySet()) {
                if (entry.getKey().isAssignableFrom(e.getClass())) {
                    return entry.getValue();
                }
            }

            Throwable eNext = e.getCause();

            if (eNext == e) {
                break;
            }

            e = eNext;
        } while (null != e);

        return null;
    }

    @Override
    public Collection<IReceiverChain> getExceptionalChains() {
        return allExceptionalChains;
    }

    @Override
    public IObject dump() {
        throw new RuntimeException("not implemented");
    }
}
