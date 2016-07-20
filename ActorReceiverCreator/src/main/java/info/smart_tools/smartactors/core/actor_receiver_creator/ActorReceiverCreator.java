package info.smart_tools.smartactors.core.actor_receiver_creator;

import info.smart_tools.smartactors.core.actor_receiver.ActorReceiver;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.handler_routing_receiver.HandlerRoutingReceiver;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ActorReceiverCreator implements IRoutedObjectCreator {
    @Override
    public void createObject(IRouter router, IObject description)
            throws ObjectCreationException, InvalidArgumentException {

        try {
            Map<Object, IMessageReceiver> handlerReceiversMap = new HashMap<>();

            String objectId = (String) description.getValue(new FieldName("name"));
            String objectDependency = (String) description.getValue(new FieldName("dependency"));
            IObject wrapperConfig = (IObject) description.getValue(new FieldName("wrapper"));
            IWrapperGenerator wg = IOC.resolve(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()));
            IReceiverGenerator rg = IOC.resolve(Keys.getOrAdd(IReceiverGenerator.class.getCanonicalName()));

            Object object = IOC.resolve(Keys.getOrAdd(objectDependency), wrapperConfig);
            for (Method m : object.getClass().getDeclaredMethods()) {
                Class wrapperInterface = m.getParameterTypes()[0];
                Object wrapper = wg.generate(wrapperInterface);
                IResolveDependencyStrategy strategy = new SingletonStrategy(wrapper);
                IMessageReceiver handlerReceiver = rg.generate(object, strategy, m.getName());
                handlerReceiversMap.put(m.getName(), handlerReceiver);
            }
            IMessageReceiver handlerRoutingReceiver = new HandlerRoutingReceiver(handlerReceiversMap);
            IMessageReceiver actorReceiver = new ActorReceiver(handlerRoutingReceiver);
            router.register(objectId, actorReceiver);
        } catch (Throwable e) {
            throw new ObjectCreationException("Could not create receiver chain.", e);
        }
    }

}
