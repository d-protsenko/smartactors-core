package info.smart_tools.smartactors.plugin.get_first_not_null_rule;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.transformation_rules.get_first_not_null.GetFirstNotNullRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@PrepareForTest({IOC.class, Keys.class, IPoorAction.class, ApplyFunctionToArgumentsStrategy.class, GetFirstNotNullRulePlugin.class})
@RunWith(PowerMockRunner.class)
public class GetFirstNotNullRulePluginTest {
    private GetFirstNotNullRulePlugin targetPlugin;
    private IBootstrap bootstrap;

    @Before
    public void before() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock((IBootstrap.class));

        targetPlugin = new GetFirstNotNullRulePlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoadPlugin() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("GetFirstNotNullRulePlugin").thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        targetPlugin.load();

        verifyNew(BootstrapItem.class).withArguments("GetFirstNotNullRulePlugin");

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).before("starter");

        verify(bootstrap).add(bootstrapItem);

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        IKey actorKey = mock(IKey.class);
        when(Keys.getOrAdd(GetFirstNotNullRule.class.getCanonicalName())).thenReturn(actorKey);

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);
        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        IOC.register(eq(actorKey), createNewInstanceStrategyArgumentCaptor.capture());

        GetFirstNotNullRule actor = mock(GetFirstNotNullRule.class);
        whenNew(GetFirstNotNullRule.class).withAnyArguments().thenReturn(actor);
    }


    @Test(expected = PluginException.class)
    public void ShouldThrowPluginException_When_BootstrapItemThrowsException() throws Exception {
        whenNew(BootstrapItem.class).withArguments("GetFirstNotNullRulePlugin").thenThrow(new InvalidArgumentException(""));
        targetPlugin.load();
    }
}