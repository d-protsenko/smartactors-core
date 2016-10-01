package info.smart_tools.smartactors.configuration_manager.configuration_manager;

import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

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

    @Test
    public void Should_applyStrategyToConfigurationIfSectionIsPresent()
            throws Exception {
        IFieldName sectionNameMock1 = mock(IFieldName.class);
        IFieldName sectionNameMock2 = mock(IFieldName.class);
        ISectionStrategy sectionStrategy1 = mock(ISectionStrategy.class);
        ISectionStrategy sectionStrategy2 = mock(ISectionStrategy.class);

        IObject configMock = mock(IObject.class);

        when(sectionStrategy1.getSectionName()).thenReturn(sectionNameMock1);
        when(sectionStrategy2.getSectionName()).thenReturn(sectionNameMock2);

        when(configMock.getValue(sectionNameMock1)).thenReturn(null);
        when(configMock.getValue(sectionNameMock2)).thenReturn(new Object());

        IConfigurationManager configurationManager = new ConfigurationManager();

        configurationManager.addSectionStrategy(sectionStrategy1);
        configurationManager.addSectionStrategy(sectionStrategy2);

        configurationManager.applyConfig(configMock);

        verify(sectionStrategy2).onLoadConfig(same(configMock));
        verify(sectionStrategy1, times(2)).getSectionName();
        verify(sectionStrategy2, times(2)).getSectionName();
        verifyNoMoreInteractions(sectionStrategy1, sectionStrategy2);
    }

    @Test(expected = ConfigurationProcessingException.class)
    public void Should_wrapExceptionWhenCannotReadSectionValue()
            throws Exception {
        IFieldName sectionNameMock1 = mock(IFieldName.class);
        ISectionStrategy sectionStrategy1 = mock(ISectionStrategy.class);

        IObject configMock = mock(IObject.class);

        when(sectionStrategy1.getSectionName()).thenReturn(sectionNameMock1);

        when(configMock.getValue(sectionNameMock1)).thenThrow(ReadValueException.class);

        IConfigurationManager configurationManager = new ConfigurationManager();

        configurationManager.addSectionStrategy(sectionStrategy1);
        configurationManager.applyConfig(configMock);
    }
}
