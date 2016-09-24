package info.smart_tools.smartactors.core.configuration_manager;

import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.exception.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.iobject.IObject;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ConfigurationManager}.
 */
public class ConfigurationManagerTest {
    @Test(expected = InvalidArgumentException.class)
    public void Should_notAddStrategiesWithDuplicateSectionName()
            throws Exception {
        IFieldName sectionNameMock = mock(IFieldName.class);
        ISectionStrategy sectionStrategy1 = mock(ISectionStrategy.class);
        ISectionStrategy sectionStrategy2 = mock(ISectionStrategy.class);

        when(sectionStrategy1.getSectionName()).thenReturn(sectionNameMock);
        when(sectionStrategy2.getSectionName()).thenReturn(sectionNameMock);

        IConfigurationManager configurationManager = new ConfigurationManager();

        try {
            configurationManager.addSectionStrategy(sectionStrategy1);
        } catch (Exception e) {
            fail();
        }

        configurationManager.addSectionStrategy(sectionStrategy2);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenStrategyIsNull()
            throws Exception {
        new ConfigurationManager().addSectionStrategy(null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenConfigIsNull()
            throws Exception {
        new ConfigurationManager().setInitialConfig(null);
    }

    @Test
    public void Should_storeInitialConfigurationObject()
            throws Exception {
        IObject configMock = mock(IObject.class);

        IConfigurationManager configurationManager = new ConfigurationManager();

        configurationManager.setInitialConfig(configMock);

        assertSame(configMock, configurationManager.getConfig());
    }

    @Test
    public void Should_configure_callAllStrategies()
            throws Exception {
        ISectionStrategy sectionStrategy1 = mock(ISectionStrategy.class);
        ISectionStrategy sectionStrategy2 = mock(ISectionStrategy.class);
        IObject configMock = mock(IObject.class);

        when(sectionStrategy1.getSectionName()).thenReturn(mock(IFieldName.class));
        when(sectionStrategy2.getSectionName()).thenReturn(mock(IFieldName.class));

        IConfigurationManager configurationManager = new ConfigurationManager();

        configurationManager.addSectionStrategy(sectionStrategy1);
        configurationManager.addSectionStrategy(sectionStrategy2);
        configurationManager.setInitialConfig(configMock);

        configurationManager.configure();

        verify(sectionStrategy1).onLoadConfig(same(configMock));
        verify(sectionStrategy2).onLoadConfig(same(configMock));
    }

    @Test(expected = InvalidStateException.class)
    public void Should_configure_throw_When_initialConfigIsNotSet()
            throws Exception {
        new ConfigurationManager().configure();
    }

    @Test(expected = InvalidStateException.class)
    public void Should_configure_throw_When_calledTwice()
            throws Exception {
        IObject configMock = mock(IObject.class);

        IConfigurationManager configurationManager = new ConfigurationManager();

        configurationManager.setInitialConfig(configMock);

        try {
            configurationManager.configure();
        } catch (Exception e) {
            fail();
        }

        configurationManager.configure();
    }

    @Test(expected = InvalidStateException.class)
    public void Should_throw_whenTryingToSetInitialConfigAfterConfigureCall()
            throws Exception {
        IObject configMock = mock(IObject.class);

        IConfigurationManager configurationManager = new ConfigurationManager();

        try {
            configurationManager.setInitialConfig(configMock);
            configurationManager.configure();
        } catch (Exception e) {
            fail();
        }

        configurationManager.setInitialConfig(configMock);
    }
}
