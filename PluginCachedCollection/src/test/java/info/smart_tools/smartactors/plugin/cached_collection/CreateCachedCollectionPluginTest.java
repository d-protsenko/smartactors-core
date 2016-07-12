package info.smart_tools.smartactors.plugin.cached_collection;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, Keys.class, IPoorAction.class, CreateNewInstanceStrategy.class, CreateCachedCollectionPlugin.class})
@RunWith(PowerMockRunner.class)
public class CreateCachedCollectionPluginTest {

    private CreateCachedCollectionPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey key1 = mock(IKey.class);
        IKey keyPlugin = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key1);
        when(IOC.resolve(eq(key1), eq("CreateCachedCollectionPlugin"))).thenReturn(keyPlugin);

        bootstrap = mock(IBootstrap.class);
        plugin = new CreateCachedCollectionPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {

        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(ICachedCollection.class.toString())).thenReturn(cachedCollectionKey);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin").thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.toString())).thenReturn(iFieldKey);

        IField connectionPoolField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "connectionPool")).thenReturn(connectionPoolField);

        IField collectionNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);

        IField keyNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "keyName")).thenReturn(keyNameField);

        HashMap<String, ICachedCollection> collectionMap = mock(HashMap.class);
        whenNew(HashMap.class).withNoArguments().thenReturn(collectionMap);

        plugin.load();

        verifyStatic();
        Keys.getOrAdd(ICachedCollection.class.toString());

        verifyNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin");

        verifyStatic(times(3));
        Keys.getOrAdd(IField.class.toString());

        verifyStatic();
        IOC.resolve(iFieldKey, "connectionPool");

        verifyStatic();
        IOC.resolve(iFieldKey, "collectionName");

        verifyStatic();
        IOC.resolve(iFieldKey, "keyName");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        ArgumentCaptor<CreateNewInstanceStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(CreateNewInstanceStrategy.class);
        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        IOC.register(eq(cachedCollectionKey), createNewInstanceStrategyArgumentCaptor.capture());

        CollectionName collectionName = mock(CollectionName.class);
        String keyName = "asd";
        String collectionNameString = "cn";
        when(collectionName.toString()).thenReturn(collectionNameString);
        String collectionMapKey = collectionNameString.concat(keyName);

        CachedCollection cachedCollection = mock(CachedCollection.class);

        IKey iobjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.toString())).thenReturn(iobjectKey);

        IKey connectionPoolKey = mock(IKey.class);
        when(Keys.getOrAdd(IPool.class.toString() + "PostgresConnection")).thenReturn(connectionPoolKey);

        IObject config = mock(IObject.class);
        IPool connectionPool = mock(IPool.class);

        when(IOC.resolve(iobjectKey)).thenReturn(config);
        when(IOC.resolve(connectionPoolKey)).thenReturn(connectionPool);

        whenNew(CachedCollection.class).withArguments(config).thenReturn(cachedCollection);

        assertTrue("Must return correct value", createNewInstanceStrategyArgumentCaptor.getValue().resolve(collectionName, keyName) == cachedCollection);

        verify(collectionMap).get(collectionMapKey);

        verifyStatic();
        Keys.getOrAdd(IObject.class.toString());

        verifyStatic();
        IOC.resolve(iobjectKey);

        verifyStatic();
        Keys.getOrAdd(IPool.class.toString() + "PostgresConnection");

        verifyStatic();
        IOC.resolve(connectionPoolKey);

        verify(connectionPoolField).out(config, connectionPool);
        verify(collectionNameField).out(config, collectionName);
        verify(keyNameField).out(config, keyName);

        verifyNew(CachedCollection.class).withArguments(config);

        verify(collectionMap).put(collectionMapKey, cachedCollection);



        verifyNew(HashMap.class).withNoArguments();
        verify(bootstrap).add(bootstrapItem);
    }
}
