package info.smart_tools.smartactors.plugin.cached_collection;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, Keys.class, IPoorAction.class, ResolveByCompositeNameIOCStrategy.class, CreateCachedCollectionPlugin.class})
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
    public void MustCorrectLoadPlugin() throws Exception {

        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(ICachedCollection.class.getCanonicalName())).thenReturn(cachedCollectionKey);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin").thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        IField connectionPoolField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "connectionPool")).thenReturn(connectionPoolField);

        IField collectionNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);

        IField keyNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "keyName")).thenReturn(keyNameField);

        ConcurrentHashMap<String, ICachedCollection> collectionMap = mock(ConcurrentHashMap.class);
        whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn(collectionMap);

        plugin.load();


        verifyNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        ArgumentCaptor<ResolveByCompositeNameIOCStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ResolveByCompositeNameIOCStrategy.class);
        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(ICachedCollection.class.getCanonicalName());

        verifyStatic(times(3));
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(iFieldKey, "connectionPool");

        verifyStatic();
        IOC.resolve(iFieldKey, "collectionName");

        verifyStatic();
        IOC.resolve(iFieldKey, "keyName");

        verifyStatic();
        IOC.register(eq(cachedCollectionKey), createNewInstanceStrategyArgumentCaptor.capture());

        String keyName = "asd";
        String collectionNameString = "cn";
        String collectionMapKey = collectionNameString.concat(keyName);

        CachedCollection cachedCollection = mock(CachedCollection.class);

        IKey iobjectKey = mock(IKey.class);
        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iobjectKey);

        IKey connectionPoolKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionPool")).thenReturn(connectionPoolKey);

        IKey collectionNameKey = mock(IKey.class);
        when(Keys.getOrAdd(CollectionName.class.getCanonicalName())).thenReturn(collectionNameKey);

        IObject config = mock(IObject.class);
        IPool connectionPool = mock(IPool.class);


        whenNew(CachedCollection.class).withArguments(config).thenReturn(cachedCollection);

        ConnectionOptions connectionOptionsWrapper = mock(ConnectionOptions.class);
        IKey connectionOptionsKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionOptions")).thenReturn(connectionOptionsKey);
        when(IOC.resolve(connectionOptionsKey)).thenReturn(connectionOptionsWrapper);

        when(IOC.resolve(iobjectKey)).thenReturn(config);
        when(IOC.resolve(connectionPoolKey, connectionOptionsWrapper)).thenReturn(connectionPool);

        assertTrue("Must return correct value", createNewInstanceStrategyArgumentCaptor.getValue().resolve(collectionNameString, keyName) == cachedCollection);
        verify(collectionMap).get(collectionMapKey);

        verifyStatic();
        Keys.getOrAdd(IObject.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(iobjectKey);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionOptions");

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionPool");

        verify(connectionPoolField).out(config, connectionPool);
        verify(collectionNameField).out(config, collectionNameString);
        verify(keyNameField).out(config, keyName);

        verifyNew(CachedCollection.class).withArguments(config);
        verify(collectionMap).putIfAbsent(collectionMapKey, cachedCollection);
        verify(bootstrap).add(bootstrapItem);
    }

    @Test(expected = PluginException.class)
    public void MustIncorrectLoadPluginWhenKeysThrowException() throws Exception {

        whenNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin").thenThrow(new InvalidArgumentException(""));

        plugin.load();
    }

    @Test
    public void MustInCorrectExecuteInIPoorActionWhenThrowRegistrationException() throws Exception {

        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(ICachedCollection.class.getCanonicalName())).thenReturn(cachedCollectionKey);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin").thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        IField connectionPoolField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "connectionPool")).thenReturn(connectionPoolField);

        IField collectionNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);

        IField keyNameField = mock(IField.class);
        when(IOC.resolve(iFieldKey, "keyName")).thenReturn(keyNameField);

        ConcurrentHashMap<String, ICachedCollection> collectionMap = mock(ConcurrentHashMap.class);
        whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn(collectionMap);

        plugin.load();


        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        doThrow(new RegistrationException("")).when(IOC.class);
        IOC.register(eq(cachedCollectionKey), any());

        try {
            actionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {
            verifyStatic();
            Keys.getOrAdd(ICachedCollection.class.getCanonicalName());

            verifyNew(BootstrapItem.class).withArguments("CreateCachedCollectionPlugin");

            verifyStatic(times(3));
            Keys.getOrAdd(IField.class.getCanonicalName());

            verifyStatic();
            IOC.resolve(iFieldKey, "connectionPool");

            verifyStatic();
            IOC.resolve(iFieldKey, "collectionName");

            verifyStatic();
            IOC.resolve(iFieldKey, "keyName");
            verifyStatic();
            IOC.register(eq(cachedCollectionKey), any());

            verify(bootstrap).add(bootstrapItem);
            return;
        }
        assertTrue("Must throw exception", false);
    }
}
