package info.smart_tools.smartactors.plugin.get_first_not_null_rule;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionNoArgs;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.transformation_rules.get_first_not_null.GetFirstNotNullRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@PrepareForTest({IOC.class, Keys.class, IActionNoArgs.class, ApplyFunctionToArgumentsStrategy.class, GetFirstNotNullRulePlugin.class})
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

        ArgumentCaptor<IActionNoArgs> actionArgumentCaptor = ArgumentCaptor.forClass(IActionNoArgs.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).after("wds_object");
        verify(bootstrapItem).before("starter");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(bootstrapItem);

        IKey strategyKey = mock(IKey.class);
        when(Keys.resolveByName(IResolutionStrategy.class.getCanonicalName())).thenReturn(strategyKey);

        GetFirstNotNullRule targetObject = mock(GetFirstNotNullRule.class);
        whenNew(GetFirstNotNullRule.class).withNoArguments().thenReturn(targetObject);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.resolveByName(IResolutionStrategy.class.getCanonicalName());

        verifyNew(GetFirstNotNullRule.class).withNoArguments();

        verifyStatic();
        IOC.resolve(strategyKey, "getFirstNotNullRule", targetObject);
    }


    @Test(expected = PluginException.class)
    public void ShouldThrowPluginException_When_BootstrapItemThrowsException() throws Exception {
        whenNew(BootstrapItem.class).withArguments("GetFirstNotNullRulePlugin").thenThrow(new InvalidArgumentException(""));
        targetPlugin.load();
    }
}