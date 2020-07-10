package info.smart_tools.smartactors.database_service_starter.database_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class DatabaseSectionStrategyTest {
    private Object configMock;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        configMock = mock(Object.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy()
        );

        IKey iFieldNameKey = Keys.getKeyByName(IFieldName.class.getCanonicalName());
        IOC.register(iFieldNameKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
        );

        IOC.register(Keys.getKeyByName("TestStrategy"),
                new CreateNewInstanceStrategy(
                        (args) -> configMock
                )
        );
    }

    @Test
    public void testLoadingRevertingConfig() throws InvalidArgumentException, ResolutionException, ConfigurationProcessingException {
        DSObject config = new DSObject("\n" +
                "     {\n" +
                "         \"database\": [\n" +
                "             {\n" +
                "                 \"key\": \"databaseKey\"," +
                "                 \"type\": \"TestStrategy\",\n" +
                "                 \"config\": {}\n" +
                "             }\n" +
                "         ]\n" +
                "     }");
        DatabaseSectionStrategy strategy = new DatabaseSectionStrategy();
        strategy.onLoadConfig(config);
        assertSame(configMock, IOC.resolve(Keys.getKeyByName("databaseKey")));
        strategy.onRevertConfig(config);
        try {
            IOC.resolve(Keys.getKeyByName("databaseKey"));
            fail();
        } catch(ResolutionException e) {
        }
    }

    @Test
    public void testExceptionsOnProcessingConfig()
            throws InvalidArgumentException, ResolutionException, ConfigurationProcessingException, ReadValueException {
        DSObject config = new DSObject("\n" +
                "     {\n" +
                "         \"database\": [\n" +
                "             {\n" +
//                "                 \"key\": \"databaseKey\"," +
//                "                 \"type\": \"TestStrategy\",\n" +
//                "                 \"config\": {}\n" +
                "             }\n" +
                "         ]\n" +
                "     }");
        DatabaseSectionStrategy strategy = new DatabaseSectionStrategy();

        strategy.getSectionName();

        try {
            strategy.onLoadConfig(config);
            fail();
        } catch (ConfigurationProcessingException e) {
        }
        try {
            strategy.onRevertConfig(config);
            fail();
        } catch (ConfigurationProcessingException e) {
        }

        IObject configMock1 = mock(IObject.class);
        doThrow(ReadValueException.class).when(configMock1).getValue(any());
        try {
            strategy.onLoadConfig(configMock1);
            fail();
        } catch (ConfigurationProcessingException e) {
        }
    }
}
