package info.smart_tools.smartactors.core.proof_of_assumption;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.proof_of_assumption.old_generator.IObjectWrapper;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by sevenbits on 6/6/16.
 */
public class WrapperGeneratorTest {


    @Before
    public void scopeInit()
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

    @Test
    public void generateClass()
            throws Exception {

        B bb = new B();

        List<B> list = new ArrayList<>();
        list.add(bb);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (arg) -> {
                            try {
                                return new Key((String) arg[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        IOC.register(
                Keys.getOrAdd(B.class.toString()),
                new CreateNewInstanceStrategy(
                        (arg) -> {
                            if (((Class)arg[0]).equals(Integer.class)) {
                                return ((B)(arg[1])).c();
                            }
                            if (((Class)arg[0]).equals(String.class)) {
                                return ((B)(arg[1])).c().toString();
                            }

                            return ((B)(arg[1])).c();
                        }
                )
        );

        Map<IFieldName, Object> map = new HashMap<IFieldName, Object>(){
            {
                put(new FieldName("a"), 1);
                put(new FieldName("b"), "string");
                put(new FieldName("c"), 'c');
                put(new FieldName("bB"), bb);
            }
        };
        IObject obj = new DSObject(map);

        Class<? extends TestInterface> a = WrapperGenerator.generate(TestInterface.class);
        TestInterface ins = a.newInstance();
        ins.init(obj);

        int a1 = ins.getA();
        String b1 = ins.getB();
        char c1 = ins.getC();
        ins.setD(1);
        int bb1 = ins.getBB();
        ins.setE(cc);
    }
}

interface TestInterface extends IObjectWrapper {
    Integer getA();
    String getB();
    char getC();
    void setD(Integer a);
    int getBB();
    void setE(B b);
}


class B {
    private int _c = 1;

    public Integer c() {
        return _c;
    }
}

