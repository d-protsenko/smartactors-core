package info.smart_tools.smartactors.iobject_plugins.fieldname_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, ResolveByNameIocStrategy.class, FieldNamePlugin.class, FieldName.class, Function.class})
@RunWith(PowerMockRunner.class)
public class FieldNamePluginTest {

    private FieldNamePlugin plugin;
    private IBootstrap bootstrap;
    private String bootstrapArgument;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new FieldNamePlugin(bootstrap);
        bootstrapArgument = "FieldNamePlugin";
    }

    @Test
    public void MustCorrectLoadPlugin() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        ArgumentCaptor<ResolveByNameIocStrategy> resolveByNameIocStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ResolveByNameIocStrategy.class);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.resolveByName(FieldName.class.getCanonicalName());

        verifyStatic();
        IOC.register(eq(fieldNameKey), resolveByNameIocStrategyArgumentCaptor.capture());

        String exampleFieldName = "exampleField";
        FieldName newFieldName = mock(FieldName.class);
        whenNew(FieldName.class).withArguments(exampleFieldName).thenReturn(newFieldName);

        assertTrue("Must return correct value",
                resolveByNameIocStrategyArgumentCaptor.getValue().resolve(exampleFieldName) == newFieldName);

        verifyNew(FieldName.class).withArguments(exampleFieldName);

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor2 = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).revertProcess(iPoorActionArgumentCaptor2.capture());

        iPoorActionArgumentCaptor2.getValue().execute();

        verifyStatic();
        IOC.remove(eq(fieldNameKey));
    }

    @Test
    public void MustInCorrectLoadPluginWhenNewBootstrapItemThrowException() throws Exception {


        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenThrow(new InvalidArgumentException(""));

        try {
            plugin.load();
        } catch (PluginException e) {
            verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectExecuteActionWhenKeysThrowException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenThrow(new ResolutionException(""));

        try {
            iPoorActionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {

            verifyStatic();
            Keys.resolveByName(FieldName.class.getCanonicalName());
            return;
        }
        assertTrue("Must throw exception", false);

    }

    @Test
    public void MustInCorrectExecuteActionWhenNewCreateStrategyThrowException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        whenNew(ResolveByNameIocStrategy.class).withArguments(any()).thenThrow(new InvalidArgumentException(""));

        try {
            iPoorActionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {

            verifyStatic();
            Keys.resolveByName(FieldName.class.getCanonicalName());

            verifyNew(ResolveByNameIocStrategy.class).withArguments(any());
            return;
        }
        assertTrue("Must throw Exception", false);

    }

    @Test
    public void MustInCorrectExecuteActionWhenIOCRegisterThrowException () throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        ArgumentCaptor<ResolveByNameIocStrategy> resolveByNameIocStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ResolveByNameIocStrategy.class);

        doThrow(new RegistrationException("")).when(IOC.class);
        IOC.register(eq(fieldNameKey), any());
        try {
            iPoorActionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {

            verifyStatic();
            Keys.resolveByName(FieldName.class.getCanonicalName());

            verifyStatic();
            IOC.register(eq(fieldNameKey), resolveByNameIocStrategyArgumentCaptor.capture());

            String exampleFieldName = "exampleField";
            FieldName newFieldName = mock(FieldName.class);
            whenNew(FieldName.class).withArguments(exampleFieldName).thenReturn(newFieldName);

            assertTrue("Must return correct value",
                    resolveByNameIocStrategyArgumentCaptor.getValue().resolve(exampleFieldName) == newFieldName);

            verifyNew(FieldName.class).withArguments(exampleFieldName);
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test(expected = RuntimeException.class)
    public void MustInCorrectResolveDependencyWhenArgLength0() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        ArgumentCaptor<Function<Object[], Object>> targetFuncArgumentCaptor = ArgumentCaptor.forClass((Class) Function.class);

        ResolveByNameIocStrategy resolveByNameIocStrategy = mock(ResolveByNameIocStrategy.class);
        whenNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.capture()).thenReturn(resolveByNameIocStrategy);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.resolveByName(FieldName.class.getCanonicalName());

        verifyNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.getValue());

        verifyStatic();
        IOC.register(fieldNameKey, resolveByNameIocStrategy);

        targetFuncArgumentCaptor.getValue().apply(new Object[0]);

    }

    @Test(expected = RuntimeException.class)
    public void MustInCorrectResolveDependencyWhenArg0IsNotString() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        ArgumentCaptor<Function<Object[], Object>> targetFuncArgumentCaptor = ArgumentCaptor.forClass((Class) Function.class);

        ResolveByNameIocStrategy createNewInstanceStrategy = mock(ResolveByNameIocStrategy.class);
        whenNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.capture()).thenReturn(createNewInstanceStrategy);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.resolveByName(FieldName.class.getCanonicalName());

        verifyNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.getValue());

        verifyStatic();
        IOC.register(fieldNameKey, createNewInstanceStrategy);

        targetFuncArgumentCaptor.getValue().apply(new Object[]{2});

    }

    @Test
    public void MustInCorrectResolveDependencyWhenNewFieldNameThrowException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey fieldNameKey = mock(IKey.class);
        when(Keys.resolveByName(FieldName.class.getCanonicalName())).thenReturn(fieldNameKey);

        ResolveByNameIocStrategy createNewInstanceStrategy = mock(ResolveByNameIocStrategy.class);

        ArgumentCaptor<Function<Object[], Object>> targetFuncArgumentCaptor = ArgumentCaptor.forClass((Class) Function.class);
        whenNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.capture()).thenReturn(createNewInstanceStrategy);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.resolveByName(FieldName.class.getCanonicalName());

        verifyNew(ResolveByNameIocStrategy.class).withArguments(targetFuncArgumentCaptor.getValue());

        verifyStatic();
        IOC.register(fieldNameKey, createNewInstanceStrategy);

        String fieldNameName = "asd";

        whenNew(FieldName.class).withArguments(fieldNameName).thenThrow(new InvalidArgumentException(""));

        try {
            targetFuncArgumentCaptor.getValue().apply(new Object[]{fieldNameName});
        } catch (RuntimeException e) {
            verifyNew(FieldName.class).withArguments(fieldNameName);
        }

    }

    @Test
    public void MustReportToConsoleWhenKeyRemovingThrowsException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(bootstrapArgument).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
        when(item.process(any())).thenReturn(item);
        when(item.revertProcess(any())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments(bootstrapArgument);

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).revertProcess(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        doThrow(new DeletionException("TestException")).when(IOC.class);
        IOC.remove(any());

        iPoorActionArgumentCaptor.getValue().execute();
    }

}