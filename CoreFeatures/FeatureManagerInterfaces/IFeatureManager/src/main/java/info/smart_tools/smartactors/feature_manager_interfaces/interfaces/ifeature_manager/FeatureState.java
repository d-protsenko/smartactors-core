package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

/**
 * Created by sevenbits on 11/15/16.
 */
public class FeatureState implements IFeatureState<String> {

    private String[] stateChain;
    private int currentState;
    private boolean lastSuccess;
    private boolean isExecuting;
    private boolean completed;

    public FeatureState(String[] stateChain) {
        this.stateChain = stateChain;
        this.isExecuting = false;
        this.lastSuccess = true;
        this.completed = false;
    }

    public FeatureState(String[] stateChain, int currentState) {
        this.stateChain = stateChain;
        this.currentState = currentState;
    }

    @Override
    public String getCurrent() {
        return this.stateChain[this.currentState];
    }

    @Override
    public void next()
            throws Exception {
        if (this.stateChain.length > this.currentState + 1) {
            ++this.currentState;
        } else {
            this.completed = true;
        }
    }

    @Override
    public boolean getLastSuccess() {
        return this.lastSuccess;
    }

    @Override
    public void setLastSuccess(boolean lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    @Override
    public boolean isExecuting() {
        return this.isExecuting;
    }

    @Override
    public void setExecuting(boolean executing) {
        this.isExecuting = executing;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }
}
