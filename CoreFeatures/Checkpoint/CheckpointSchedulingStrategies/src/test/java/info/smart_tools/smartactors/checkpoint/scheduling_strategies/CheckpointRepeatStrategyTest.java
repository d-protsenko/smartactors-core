package info.smart_tools.smartactors.checkpoint.scheduling_strategies;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.time.Duration;

import static org.mockito.Mockito.*;

/**
 * Test for {@link CheckpointRepeatStrategy}.
 */
public class CheckpointRepeatStrategyTest extends PluginsLoadingTestBase {
    private CheckpointRepeatStrategy strategy;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        strategy = new CheckpointRepeatStrategy() {
            @Override
            protected long calculateNextInterval(ISchedulerEntry entry) throws ReadValueException, InvalidArgumentException, ChangeValueException {
                return 0;
            }

            @Override
            protected Duration defaultPostRestoreDelay(ISchedulerEntry entry) throws ReadValueException, InvalidArgumentException {
                return null;
            }

            @Override
            protected Duration defaultPostCompletionDelay(ISchedulerEntry entry) throws ReadValueException, InvalidArgumentException {
                return null;
            }
        };
    }

    @Test
    public void Should_awakePausedEntryWhenItIsUnpaused()
            throws Exception {
        ISchedulerEntry entry = mock(ISchedulerEntry.class);

        strategy.notifyPaused(entry);
        strategy.processPausedExecution(entry);

        verifyNoMoreInteractions(entry);

        strategy.notifyUnPaused(entry);

        verify(entry).awake();
    }
}

