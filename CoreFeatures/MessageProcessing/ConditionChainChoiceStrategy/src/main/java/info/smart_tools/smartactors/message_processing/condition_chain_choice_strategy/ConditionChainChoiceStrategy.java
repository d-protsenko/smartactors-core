package info.smart_tools.smartactors.message_processing.condition_chain_choice_strategy;

import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 * {@link IChainChoiceStrategy Chain choice strategy} that returns chain id depending the chainCondition flag.
 */
public class ConditionChainChoiceStrategy implements IChainChoiceStrategy {
    private final IFieldName chainConditionFN;
    private final IFieldName trueChainFN;
    private final IFieldName falseChainFN;

    /**
     * Constructor
     * @throws ResolutionException sometimes
     */
    public ConditionChainChoiceStrategy() throws ResolutionException {
        chainConditionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chainCondition");
        trueChainFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "trueChain");
        falseChainFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "falseChain");
    }

    @Override
    public Object chooseChain(final IMessageProcessor messageProcessor) throws ChainChoiceException {
        try {
            if ((Boolean) messageProcessor.getMessage().getValue(chainConditionFN)) {
                Object name = messageProcessor.getSequence().getCurrentReceiverArguments().getValue(trueChainFN);
                return IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), name);
            }

            Object name = messageProcessor.getSequence().getCurrentReceiverArguments().getValue(falseChainFN);
            return IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), name);
        } catch (Exception e) {
            throw new ChainChoiceException("Could not execute condition chain choice strategy.");
        }
    }
}
