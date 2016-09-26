package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for {@link ObjectsSectionProcessingStrategy}
 */
@PrepareForTest({IOC.class, Keys.class})
@RunWith(PowerMockRunner.class)
public class ObjectsSectionProcessingStrategyTest {
    private IKey fieldNameKey = mock(IKey.class);
    private IKey routerKey = mock(IKey.class);
    private IKey kindACreatorKey = mock(IKey.class);
    private IKey kindBCreatorKey = mock(IKey.class);

    private IFieldName objectsFieldName;
    private IFieldName kindFieldName;
    private IRouter routerMock;
    private IRoutedObjectCreator kindACreatorMock;
    private IRoutedObjectCreator kindBCreatorMock;
    private List<IObject> section;
    private IObject configMock;

    @Before
    public void setUp()
            throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        objectsFieldName = mock(IFieldName.class);
        kindFieldName = mock(IFieldName.class);
        routerMock = mock(IRouter.class);
        kindACreatorMock = mock(IRoutedObjectCreator.class);
        kindBCreatorMock = mock(IRoutedObjectCreator.class);

        when(Keys.getOrAdd(eq(IFieldName.class.getCanonicalName()))).thenReturn(fieldNameKey);
        when(Keys.getOrAdd(eq(IRouter.class.getCanonicalName()))).thenReturn(routerKey);
        when(Keys.getOrAdd(eq(IRoutedObjectCreator.class.getCanonicalName()+"#kind_a"))).thenReturn(kindACreatorKey);
        when(Keys.getOrAdd(eq(IRoutedObjectCreator.class.getCanonicalName()+"#kind_b"))).thenReturn(kindBCreatorKey);

        when(IOC.resolve(same(fieldNameKey), eq("objects"))).thenReturn(objectsFieldName);
        when(IOC.resolve(same(fieldNameKey), eq("kind"))).thenReturn(kindFieldName);

        when(IOC.resolve(routerKey)).thenReturn(routerMock);
        when(IOC.resolve(kindACreatorKey)).thenReturn(kindACreatorMock);
        when(IOC.resolve(kindBCreatorKey)).thenReturn(kindBCreatorMock);

        section = Arrays.asList(mock(IObject.class), mock(IObject.class));

        when(section.get(0).getValue(same(kindFieldName))).thenReturn("kind_a");
        when(section.get(1).getValue(same(kindFieldName))).thenReturn("kind_b");

        configMock = mock(IObject.class);

        when(configMock.getValue(same(objectsFieldName))).thenReturn(section);
    }

    @Test
    public void Should_callObjectCreatorsAccordingToConfiguration()
            throws Exception {
        ISectionStrategy strategy = new ObjectsSectionProcessingStrategy();

        assertSame(objectsFieldName, strategy.getSectionName());

        strategy.onLoadConfig(configMock);

        verify(kindACreatorMock).createObject(same(routerMock), same(section.get(0)));
        verify(kindBCreatorMock).createObject(same(routerMock), same(section.get(1)));
    }

    @Test
    public void Should_wrapExceptionsThrownByIOC()
            throws Exception {
        when(IOC.resolve(same(kindBCreatorKey))).thenThrow(ResolutionException.class);

        try {
            ISectionStrategy strategy = new ObjectsSectionProcessingStrategy();

            strategy.onLoadConfig(configMock);

            fail();
        } catch (ConfigurationProcessingException e) {
            assertSame(ResolutionException.class, e.getCause().getClass());
            verify(kindACreatorMock).createObject(same(routerMock), same(section.get(0)));
        }
    }

    @Test
    public void Should_wrapExceptionsThrownCreators()
            throws Exception {
        doThrow(ObjectCreationException.class).when(kindACreatorMock).createObject(same(routerMock), same(section.get(0)));

        try {
            ISectionStrategy strategy = new ObjectsSectionProcessingStrategy();

            strategy.onLoadConfig(configMock);

            fail();
        } catch (ConfigurationProcessingException e) {
            assertSame(ObjectCreationException.class, e.getCause().getClass());
            verify(kindBCreatorMock, never()).createObject(same(routerMock), same(section.get(1)));
        }
    }
}
