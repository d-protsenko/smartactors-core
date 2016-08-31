package info.smart_tools.smartactors.core.examples.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Sample actor,
 * replies with "hello" message.
 */
public class HelloActor {

    /**
     * Sends greeting to the person with specified name.
     * @param message a message where to get name and put greeting message
     * @throws HelloActorException when something goes wrong
     */
    public void hello(final GreetingMessage message) throws HelloActorException {
        try {
            String name = message.getName();
            message.setGreeting(String.format("Hello, %s!", name));
        } catch (ReadValueException | ChangeValueException e) {
            throw new HelloActorException(e);
        }
    }

}
