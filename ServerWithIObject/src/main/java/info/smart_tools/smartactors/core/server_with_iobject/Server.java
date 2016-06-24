package info.smart_tools.smartactors.core.server_with_iobject;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import info.smart_tools.smartactors.core.wrapper_generator.IObjectWrapper;
import info.smart_tools.smartactors.core.wrapper_generator.WrapperGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IServer} with
 */
public class Server implements IServer {

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            scopeInit();
            registryKeysStorageStrategy();
            registerWrapperGenerator();
            registerCustomClass();
        } catch (Throwable e) {
            throw new ServerInitializeException("Could not initialize server.");
        }
    }

    @Override
    public void start()
            throws ServerExecutionException {
        try {
            /** Create instance of CustomClass */
            CustomClass cc = new CustomClass();
            /** Initialize map for IObject initialization */
            Map<IFieldName, Object> map = new HashMap<IFieldName, Object>() {
                {
                    put(new FieldName("a"), 1);
                    put(new FieldName("b"), "string");
                    put(new FieldName("c"), 'c');
                    put(new FieldName("bB"), cc);
                }
            };
            /** Initialize instance of IObject - DSObject */
            IObject message = new DSObject();
            IObject context = new DSObject();
            IObject response = new DSObject();
            IObject binding = new DSObject();

            /** Get wrapper generator by IOC.resolve */
            IWrapperGenerator wg = IOC.resolve(Keys.getOrAdd(IWrapperGenerator.class.toString()));
            /** Generate wrapper class by given interface and create instance of generated class */
            DemonstrationInterface ins = wg.generate(DemonstrationInterface.class, binding);
            /** Initialize instance of generated class by IObject */
            ((IObjectWrapper) (ins)).init(message, context, response);

            /** make some operations on this instance */
            int a1 = ins.getA();
            String b1 = ins.getB();
            char c1 = ins.getC();
            ins.setD(1);
            int bb1 = ins.getBB();

        } catch (Throwable e) {
            throw new ServerExecutionException(e);
        }
    }

    private void scopeInit()
            throws Exception {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );


        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
    }

    private void registryKeysStorageStrategy()
            throws Exception {
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (arg) -> {
                            try {
                                return new Key((String) arg[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    private void registerWrapperGenerator()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        IOC.register(Keys.getOrAdd(IWrapperGenerator.class.toString()), new SingletonStrategy(wg));
    }

    private void registerCustomClass()
            throws Exception {
        IOC.register(
                Keys.getOrAdd(CustomClass.class.toString()),
                new CreateNewInstanceStrategy(
                        (arg) -> {
                            /** It's demonstration realization of type convert strategy.
                             * Current realization has bad pattern (switch). Don't repeat that.
                             * Batter, realize separated implementation of IResolveDependencyStrategy
                             * with HashMap of (TargetType, TransformRule) pairs
                             **/
                            if (((Class) arg[0]).equals(Integer.class)) {
                                return ((CustomClass) (arg[1])).customMethod();
                            }
                            if (((Class) arg[0]).equals(String.class)) {
                                return ((CustomClass) (arg[1])).customMethod().toString();
                            }

                            return ((CustomClass) (arg[1])).customMethod();
                        }
                )
        );
    }
}

/**
 * Demonstration interface for creation IObject wrapper
 */
interface DemonstrationInterface {
    /**
     * Getter
     * @return integer
     */
    Integer getA();

    /**
     * Getter
     * @return string
     */
    String getB();

    /**
     * Getter
     * @return char
     */
    char getC();

    /**
     * Setter
     * @param a integer
     */
    void setD(Integer a);

    /**
     * Getter
     * @return integer
     */
    int getBB();

    /**
     * Setter
     * @param b instance of {@link CustomClass}
     */
    void setE(CustomClass b);
}

/**
 * Demonstration custom class
 */
class CustomClass {
    private int c = 1;

    /**
     * Custom method
     * @return integer
     */
    public Integer customMethod() {
        return c;
    }
}