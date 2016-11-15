package info.smart_tools.smartactors.demo.simple_chain.sample_actor;

import info.smart_tools.smartactors.demo.simple_chain.sample_actor.exception.SampleException;
import info.smart_tools.smartactors.demo.simple_chain.sample_actor.wrapper.SampleWrapper;

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
            if (wrapper.resetState()) {
                this.state = 0;
            }
            wrapper.setCurrentActorState(this.state);
        } catch (Throwable e) {
            throw new SampleException();
        }
    }
}
