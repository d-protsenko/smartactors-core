package info.smart_tools.smartactors.actors.sample_other_actor;

import info.smart_tools.smartactors.actors.sample_other_actor.exception.SampleOtherException;
import info.smart_tools.smartactors.actors.sample_other_actor.wrapper.SampleOtherWrapper;

/**
 * Created by sevenbits on 7/28/16.
 */
public class SampleOtherActor {

    public void transformAndPutForResponse(final SampleOtherWrapper wrapper)
            throws SampleOtherException {
        try {
            String s = wrapper.getStringValue();
            System.out.println("Received message from other chain: " + s);
        } catch (Exception e) {
            throw new SampleOtherException();
        }
    }
}
