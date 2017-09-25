package info.smart_tools.smartactors.system_actors_pack.repeater_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.system_actors_pack.repeater_actor.RepeaterActor;

/**
 * Message wrapper for {@link RepeaterActor}.
 *
 * The {@link #getRepeatCondition()} method should be configured to apply rules that determine if the repeater should repeat the chain.
 * The {@link #getSequence()} method should be configured to return the message processing sequence stored in message environment.
 *
 * @see IMessageProcessor#getEnvironment()
 */
public interface IRepeatRequest {
    /**
     * The message processing sequence to repeat actions in.
     *
     * @return the message processing sequence
     * @throws ReadValueException if any error occurs
     */
    IMessageProcessingSequence getSequence() throws ReadValueException;

    /**
     * True if repeater should repeat chain at current level of sequence.
     *
     * @return true if repeater should repeat a chain
     * @throws ReadValueException if any error occurs
     */
    boolean getRepeatCondition() throws ReadValueException;
}
