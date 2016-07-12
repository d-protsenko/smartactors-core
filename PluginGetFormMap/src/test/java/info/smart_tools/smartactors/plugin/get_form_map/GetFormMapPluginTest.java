package info.smart_tools.smartactors.plugin.get_form_map;

import info.smart_tools.smartactors.actors.get_form.GetFormActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, GetFormMapPlugin.class})
@RunWith(PowerMockRunner.class)
public class GetFormMapPluginTest {
    private GetFormMapPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws ResolutionException {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey key1 = mock(IKey.class);
        IKey keyQuery = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key1);
        when(IOC.resolve(eq(key1), eq(CompiledQuery.class.toString()))).thenReturn(keyQuery);

        bootstrap = mock(IBootstrap.class);
        plugin = new GetFormMapPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {

        IObject arg = mock(IObject.class);

        GetFormActor actor = mock(GetFormActor.class);

        whenNew(GetFormActor.class).withArguments(arg).thenReturn(actor);

        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(GetFormActor.class.toString())).thenReturn(cachedCollectionKey);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateGetFormMapActorsPlugin").thenReturn(bootstrapItem);
        plugin.load();
        verifyNew(BootstrapItem.class).withArguments("CreateGetFormMapActorsPlugin");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(iPoorActionArgumentCaptor.capture());

        ArgumentCaptor<CreateNewInstanceStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(CreateNewInstanceStrategy.class);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        IOC.register(eq(cachedCollectionKey), createNewInstanceStrategyArgumentCaptor.capture());

        createNewInstanceStrategyArgumentCaptor.getValue().resolve(arg);

        verifyNew(GetFormActor.class).withArguments(arg);

        verify(bootstrap).add(eq(bootstrapItem));
    }
}
