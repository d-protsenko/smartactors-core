package info.smart_tools.smartactors.plugin.get_header_from_request_rule;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.transformation_rules.get_header_from_request.GetHeaderFromRequestRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, GetHeaderFromRequestRulePlugin.class, CreateNewInstanceStrategy.class})
@RunWith(PowerMockRunner.class)
public class GetHeaderFromRequestRulePluginTest {
    private GetHeaderFromRequestRulePlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new GetHeaderFromRequestRulePlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoadPlugin() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).after("wds_object");
        verify(bootstrapItem).before("starter");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(bootstrapItem);

        IKey strategyKey = mock(IKey.class);
        when(Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName())).thenReturn(strategyKey);

        GetHeaderFromRequestRule targetObject = mock(GetHeaderFromRequestRule.class);
        whenNew(GetHeaderFromRequestRule.class).withNoArguments().thenReturn(targetObject);


        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName());

        verifyNew(GetHeaderFromRequestRule.class).withNoArguments();

        verifyStatic();
        IOC.resolve(strategyKey, "getHeaderFromRequestRule", targetObject);
        /*IKey ruleKey = mock(IKey.class);
        when(Keys.getOrAdd(GetHeaderFromRequestRule.class.getCanonicalName())).thenReturn(ruleKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(GetHeaderFromRequestRule.class.getCanonicalName());

        ArgumentCaptor<CreateNewInstanceStrategy> createNewInstanceStrategyArgumentCaptor = ArgumentCaptor.forClass(CreateNewInstanceStrategy.class);

        verifyStatic();
        IOC.register(eq(ruleKey), createNewInstanceStrategyArgumentCaptor.capture());

        IObject arg = mock(IObject.class);

        GetHeaderFromRequestRule rule = mock(GetHeaderFromRequestRule.class);
        whenNew(GetHeaderFromRequestRule.class).withNoArguments().thenReturn(rule);

        assertTrue("Objects must return correct object", createNewInstanceStrategyArgumentCaptor.getValue().resolve(arg) == rule);

        verifyNew(GetHeaderFromRequestRule.class).withNoArguments();*/
    }

    @Test
    public void MustInCorrectLoadNewIBootstrapItemThrowException() throws Exception {

        whenNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin").thenThrow(new InvalidArgumentException(""));

        try {
            plugin.load();
        } catch (PluginException e) {

            verifyNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin");
            return;
        }
        assertTrue("Must throw exception, but was not", false);
    }

    @Test
    public void MustInCorrectExecuteActionWhenKeysThrowException() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(bootstrapItem);

        when(Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName())).thenThrow(new ResolutionException(""));

        GetHeaderFromRequestRule targetObject = mock(GetHeaderFromRequestRule.class);
        whenNew(GetHeaderFromRequestRule.class).withNoArguments().thenReturn(targetObject);

        try {
            actionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {
            verifyStatic();
            Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName());
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectExecuteActionWhenIOCRegisterThrowException() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("GetHeaderFromRequestRulePlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(bootstrapItem);

        IKey strategyKey = mock(IKey.class);
        when(Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName())).thenReturn(strategyKey);

        GetHeaderFromRequestRule targetObject = mock(GetHeaderFromRequestRule.class);
        whenNew(GetHeaderFromRequestRule.class).withNoArguments().thenReturn(targetObject);

        when(IOC.resolve(strategyKey, "getHeaderFromRequestRule", targetObject)).thenThrow(new ResolutionException(""));

        try {
            actionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {
            verifyStatic();
            Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName());

            verifyNew(GetHeaderFromRequestRule.class).withNoArguments();

            verifyStatic();
            IOC.resolve(strategyKey, "getHeaderFromRequestRule", targetObject);
            return;
        }
        assertTrue("Must throw exception", false);
    }
}