package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for {@link MapsSectionProcessingStrategy}.
 */
@PrepareForTest({IOC.class, Keys.class})
@RunWith(PowerMockRunner.class)
public class MapsSectionProcessingStrategyTest {
    private IKey fieldNameKey = mock(IKey.class);
    private IKey chainStorageKey = mock(IKey.class);
    private IKey chainIdKey = mock(IKey.class);

    private IFieldName mapsFieldName;
    private IFieldName idFieldName;
    private IChainStorage chainStorageMock;
    private List<IObject> section;
    private IObject configMock;

    private String map1Name = "map1";
    private String map2Name = "map2";

    private Object chain1id = mock(Object.class);
    private Object chain2id = mock(Object.class);

    @Before
    public void setUp()
            throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        mapsFieldName = mock(IFieldName.class);
        idFieldName = mock(IFieldName.class);
        chainStorageMock = mock(IChainStorage.class);

        when(Keys.getOrAdd(eq(IFieldName.class.getCanonicalName()))).thenReturn(fieldNameKey);
        when(Keys.getOrAdd(eq(IChainStorage.class.getCanonicalName()))).thenReturn(chainStorageKey);
        when(Keys.getOrAdd(eq("chain_id_from_map_name"))).thenReturn(chainIdKey);

        when(IOC.resolve(same(fieldNameKey), eq("maps"))).thenReturn(mapsFieldName);
        when(IOC.resolve(same(fieldNameKey), eq("id"))).thenReturn(idFieldName);

        when(IOC.resolve(chainStorageKey)).thenReturn(chainStorageMock);

        when(IOC.resolve(chainIdKey, map1Name)).thenReturn(chain1id);
        when(IOC.resolve(chainIdKey, map2Name)).thenReturn(chain2id);

        section = Arrays.asList(mock(IObject.class), mock(IObject.class));

        when(section.get(0).getValue(same(idFieldName))).thenReturn(map1Name);
        when(section.get(1).getValue(same(idFieldName))).thenReturn(map2Name);

        configMock = mock(IObject.class);

        when(configMock.getValue(same(mapsFieldName))).thenReturn(section);
    }

    @Test
    public void Should_createMaps()
            throws Exception {
        ISectionStrategy strategy = new MapsSectionProcessingStrategy();

        assertSame(mapsFieldName, strategy.getSectionName());

        strategy.onLoadConfig(configMock);

        verify(chainStorageMock).register(chain1id, section.get(0));
        verify(chainStorageMock).register(chain2id, section.get(1));
    }

    @Test
    public void Should_wrapExceptionThrownByChainStorage()
            throws Exception {
        doThrow(ChainCreationException.class).when(chainStorageMock).register(chain2id, section.get(1));

        try {
            ISectionStrategy strategy = new MapsSectionProcessingStrategy();

            strategy.onLoadConfig(configMock);

            fail();
        } catch (ConfigurationProcessingException e) {
            assertSame(ChainCreationException.class, e.getCause().getClass());

            verify(chainStorageMock).register(chain1id, section.get(0));
        }
    }

    @Test(expected = ConfigurationProcessingException.class)
    public void Should_wrapExceptionsThrownByIOC()
            throws Exception {
        when(IOC.resolve(chainStorageKey)).thenThrow(ResolutionException.class);

        new MapsSectionProcessingStrategy().onLoadConfig(configMock);
    }
}
