package info.smart_tools.smartactors.core.actor_receiver_creator;

public class ConstructorWrapperImpl implements IConstructorWrapper {

    private Boolean wrapperState = false;

    @Override
    public Boolean getA() {
        return this.wrapperState;
    }

    @Override
    public void setA(Boolean visited) {
        this.wrapperState = visited;
    }

    public Boolean getWrapperState() {
        return wrapperState;
    }
}
