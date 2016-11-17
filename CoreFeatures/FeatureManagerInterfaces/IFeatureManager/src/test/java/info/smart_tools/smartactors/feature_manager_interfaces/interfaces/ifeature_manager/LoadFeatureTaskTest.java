package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by sevenbits on 11/16/16.
 */
public class LoadFeatureTaskTest {

    @Test
    public void check() throws Exception {
        IFeature feature = mock(IFeature.class);
        LoadFeatureTask lft = new LoadFeatureTask(null, feature);
        when(feature.getFeatureLocation()).thenReturn(new Path("/home/sevenbits/Projects/1derlink-server/corefeatures/http-endpoint-0.2.0-SNAPSHOT"));

        lft.execute();
    }
}
