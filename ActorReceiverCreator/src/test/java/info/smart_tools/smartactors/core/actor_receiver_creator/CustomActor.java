package info.smart_tools.smartactors.core.actor_receiver_creator;

public class CustomActor {

    public Boolean constructorVisited = false;
    public Boolean methodVisited = false;

    public CustomActor(IConstructorWrapper wrapper) {
        this.constructorVisited = wrapper.getA();
        wrapper.setA(true);
    }

    void getSomeValue(IMethodWrapper wrapper) {
        this.methodVisited = true;
    }
}
