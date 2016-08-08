package info.smart_tools.smartactors.actor.repeater;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;

/**
 * Actor repeating current chain in message processing sequence.
 */
public class RepeaterActor {
    /**
     * Repeat current chain if required.
     *
     * @param request the wrapper
     * @throws ReadValueException if error occurs reading values from wrapper
     * @throws InvalidArgumentException if current level returned by sequence is not active level of sequence. This is only possible when a
     *              message processing sequence is used for processing of multiple messages
     */
    public void handle(final IRepeatRequest request)
            throws ReadValueException, InvalidArgumentException {
        if (request.repeat()) {
            IMessageProcessingSequence sequence = request.getSequence();

            sequence.goTo(sequence.getCurrentLevel(), 0);
        }
    }
}
