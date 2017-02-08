package info.smart_tools.smartactors.feature.scatter_gather_feature.iscatter_gather_strategy;

import info.smart_tools.smartactors.feature.scatter_gather_feature.iscatter_gather_strategy.exception.IScatterGatherStrategyException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 * Created by sevenbits on 07.02.17.
 */
public interface IScatterGatherStrategy {

    /**
     * Strategy method that calls before scatter section
     *
     * @param messageProcessor  {@link IMessageProcessor} of the scatter thread
     * @param scatterGatherInfo {@link IObject} with information about current state of scatter-gather
     */
    void beforeScatter(IMessageProcessor messageProcessor, IObject scatterGatherInfo) throws IScatterGatherStrategyException;

    /**
     * Strategy method for choosing chain on which object will be send
     *
     * @param object {@link IObject} that will send on chain
     * @throws IScatterGatherStrategyException
     */
    Object chainChoose(IObject object) throws IScatterGatherStrategyException;

    /**
     * Strategy method that calls on message sent failed
     *
     * @param messageProcessor  {@link IMessageProcessor} of the scatter thread
     * @param scatterGatherInfo {@link IObject} with information about current state of scatter-gather
     */
    void onMessageSendFail(IMessageProcessor messageProcessor, IObject scatterGatherInfo) throws IScatterGatherStrategyException;

    ;

    /**
     * Strategy method that calls after every thread starts
     *
     * @param messageProcessor  {@link IMessageProcessor} of the scatter thread
     * @param scatterGatherInfo {@link IObject} with information about current state of scatter-gather
     */
    void onMessageSent(IMessageProcessor messageProcessor, IObject scatterGatherInfo) throws IScatterGatherStrategyException;

    /**
     * Strategy method, that calls after scatter section
     *
     * @param messageProcessor  {@link IMessageProcessor} of the current thread
     * @param scatterGatherInfo {@link IObject} with information about current state of scatter-gather
     */
    void afterScatter(IMessageProcessor messageProcessor, IObject scatterGatherInfo) throws IScatterGatherStrategyException;

    /**
     * Strategy method, that calls on gather section
     *
     * @param messageProcessor  {@link IMessageProcessor} of the scatter thread
     * @param scatterGatherInfo {@link IObject} with information about current state of scatter-gather
     */
    void onGather(IMessageProcessor messageProcessor, IObject result, IObject scatterGatherInfo) throws IScatterGatherStrategyException;
}
