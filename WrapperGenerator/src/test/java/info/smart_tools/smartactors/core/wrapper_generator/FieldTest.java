package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

/**
 * Tests for Field
 */
public class FieldTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IResolveDependencyStrategy toInteger = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd(int.class.toString()), toInteger);
        when(toInteger.resolve(any())).thenReturn(1);
    }

    @Test
    public void checkFieldCreation()
            throws Exception {
        Field field = new Field<>(new FieldName("test"));
        assertNotNull(field);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkFirstMethodFromOnWrongArgument()
            throws Exception {
        Field<Integer> field = new Field<>(new FieldName("intValue"));
        field.from(null, Integer.class);
        fail();
    }

    @Test
    public void checkFirstMethodFromNullValue()
            throws Exception {
        IObject iObject = mock(IObject.class);
        Field<Integer> field = new Field<>(new FieldName("intValue"));
        Integer result = field.from(iObject, Integer.class);
        assertNull(result);
    }

    @Test
    public void checkFirstMethodFromSameClass()
            throws Exception {
        IObject iObject = mock(IObject.class);
        when(iObject.getValue(new FieldName("intValue"))).thenReturn(1);
        Field<Integer> field = new Field<>(new FieldName("intValue"));
        Integer result = field.from(iObject, Integer.class);
        assertEquals((long)result, 1L);
    }

    @Test
    public void checkFirstMethodFromImplementedClass()
            throws Exception {
        IObject iObject = mock(IObject.class);
        IObject innerIObject = mock(IObject.class);
        when(iObject.getValue(new FieldName("IObjectValue"))).thenReturn(innerIObject);
        Field<IObject> field = new Field<>(new FieldName("IObjectValue"));
        IObject result = field.from(iObject, IObject.class);
        assertSame(result, innerIObject);
    }

    @Test
    public void checkFirstMethodFromWithIOC()
            throws Exception {
        IObject iObject = mock(IObject.class);
        when(iObject.getValue(new FieldName("intValue"))).thenReturn(1);
        Field<Integer> field = new Field<>(new FieldName("intValue"));
        Integer result = field.from(iObject, int.class);
        assertSame(result, 1);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkFirstMethodFromWithIOCResolutionException()
            throws Exception {
        IObject iObject = mock(IObject.class);
        when(iObject.getValue(new FieldName("boolValue"))).thenReturn(true);
        Field<Boolean> field = new Field<>(new FieldName("boolValue"));
        Boolean result = field.from(iObject, boolean.class);
        fail();
    }

}
