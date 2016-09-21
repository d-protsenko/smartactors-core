package info.smart_tools.smartactors.plugin.user_auth_by_login_actor;

import info.smart_tools.smartactors.actors.authentication.users.UserAuthByLoginActor;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ IOC.class, Keys.class, UserAuthByLoginActorPlugin.class})
@RunWith(PowerMockRunner.class)
public class UserAuthByLoginActorPluginTest {
    private UserAuthByLoginActorPlugin testPlugin;

    private IBootstrap<IBootstrapItem<String>> bootstrap;

    @Before
    public void before() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);

        testPlugin = new UserAuthByLoginActorPlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoad() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("UserAuthByLoginActorPlugin").thenReturn(item);

        when(item.after(any())).thenReturn(item);
        when(item.before(any())).thenReturn(item);

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        when(item.process(actionArgumentCaptor.capture())).thenReturn(item);

        testPlugin.load();

        verifyNew(BootstrapItem.class).withArguments("UserAuthByLoginActorPlugin");
        verify(item).after("IOC");
        verify(item).before("starter");

        verify(item).process(actionArgumentCaptor.getValue());

        verify(bootstrap).add(item);

        //------test lambda
        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);
        IField collectionNameField = mock(IField.class);
        IField charsetField = mock(IField.class);
        IField algorithmField = mock(IField.class);
        IField encoderField = mock(IField.class);

        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);
        when(IOC.resolve(iFieldKey, "charset")).thenReturn(charsetField);
        when(IOC.resolve(iFieldKey, "algorithm")).thenReturn(algorithmField);
        when(IOC.resolve(iFieldKey, "encoder")).thenReturn(encoderField);

        IKey iUserAuthByLoginParamsKey = mock(IKey.class);
        when(Keys.getOrAdd(IUserAuthByLoginParams.class.getCanonicalName())).thenReturn(iUserAuthByLoginParamsKey);

        IKey userAuthByLoginActorKey = mock(IKey.class);
        when(Keys.getOrAdd(UserAuthByLoginActor.class.getCanonicalName())).thenReturn(userAuthByLoginActorKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        IOC.resolve(iFieldKey, "collectionName");

        verifyStatic();
        IOC.resolve(iFieldKey, "charset");

        verifyStatic();
        IOC.resolve(iFieldKey, "algorithm");

        verifyStatic();
        IOC.resolve(iFieldKey, "encoder");

        ArgumentCaptor<IResolveDependencyStrategy> paramsStrategyArgumentCaptor = ArgumentCaptor.forClass(IResolveDependencyStrategy.class);

        verifyStatic();
        IOC.register(eq(iUserAuthByLoginParamsKey), paramsStrategyArgumentCaptor.capture());

        ArgumentCaptor<IResolveDependencyStrategy> actorStrategyArgumentCaptor = ArgumentCaptor.forClass(IResolveDependencyStrategy.class);

        verifyStatic();
        IOC.register(eq(userAuthByLoginActorKey), actorStrategyArgumentCaptor.capture());

        //------test params lambda

        IObject paramsIObject = mock(IObject.class);

        String collectionName = "exampleCN";
        String charset = "exampleCS";
        String algorithm = "exampleA";
        String encoder = "exampleE";

        when(collectionNameField.in(paramsIObject)).thenReturn(collectionName);
        when(charsetField.in(paramsIObject)).thenReturn(charset);
        when(algorithmField.in(paramsIObject)).thenReturn(algorithm);
        when(encoderField.in(paramsIObject)).thenReturn(encoder);

        IUserAuthByLoginParams testParamsInterface = paramsStrategyArgumentCaptor.getValue().resolve(paramsIObject);

        assertTrue(testParamsInterface.getCollection().equals(collectionName));
        verify(collectionNameField).in(paramsIObject);

        assertTrue(testParamsInterface.getCharset().equals(charset));
        verify(charsetField).in(paramsIObject);

        assertTrue(testParamsInterface.getAlgorithm().equals(algorithm));
        verify(algorithmField).in(paramsIObject);

        assertTrue(testParamsInterface.getEncoder().equals(encoder));
        verify(encoderField).in(paramsIObject);

        //---------test getting connection pool
        ConnectionOptions connectionOptions = mock(ConnectionOptions.class);
        IKey connectionOptionsKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionOptions")).thenReturn(connectionOptionsKey);
        when(IOC.resolve(connectionOptionsKey)).thenReturn(connectionOptions);

        IKey connectionPoolKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionPool")).thenReturn(connectionPoolKey);

        IPool pool = mock(IPool.class);
        when(IOC.resolve(connectionPoolKey, connectionOptions)).thenReturn(pool);

        assertTrue(testParamsInterface.getConnectionPool() == pool);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionOptions");

        verifyStatic();
        IOC.resolve(connectionOptionsKey);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionPool");

        verifyStatic();
        IOC.resolve(connectionPoolKey, connectionOptions);

        //------test actor lambda
        UserAuthByLoginActor userAuthByLoginActor = mock(UserAuthByLoginActor.class);
        whenNew(UserAuthByLoginActor.class).withArguments(testParamsInterface).thenReturn(userAuthByLoginActor);

        when(IOC.resolve(iUserAuthByLoginParamsKey, paramsIObject)).thenReturn(testParamsInterface);

        assertTrue(actorStrategyArgumentCaptor.getValue().resolve(paramsIObject) == userAuthByLoginActor);

        verifyStatic(times(2));
        Keys.getOrAdd(IUserAuthByLoginParams.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(iUserAuthByLoginParamsKey, paramsIObject);

        verifyNew(UserAuthByLoginActor.class).withArguments(testParamsInterface);
    }
}