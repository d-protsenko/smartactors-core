package info.smart_tools.smartactors.actors;

import info.smart_tools.smartactors.actors.exception.SampleException;
import info.smart_tools.smartactors.actors.wrapper.SampleWrapper;

/**
 * Created by sevenbits on 7/28/16.
 */
public class SampleActor {

    private Integer state = 0;

    public void transformAndPutForResponse(SampleWrapper wrapper)
            throws SampleException {
        try {
            ++this.state;
            String s = wrapper.getSomeField();
            wrapper.setSomeValueForRequest(s + "_transformed");
            wrapper.setCurrentActorState(this.state);
            if (wrapper.resetState()) {
                this.state = 0;
            }
        } catch (Exception e) {
            throw new SampleException();
        }
    }
}
