package info.smart_tools.smartactors.feature_management.feature_creator_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper.CreateFeaturesWrapper;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper.CreateMessageWrapper;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link FeaturesCreatorActor}
 */
public class FeaturesCreatorActorTest {
    private IStrategyContainer container = new StrategyContainer();

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException("exception", e);
                            }
                        }
                )
        );
        IOC.register(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    if (args.length == 0) {
                        return new DSObject();
                    } else if (args.length == 1 && args[0] instanceof String) {
                        try {
                            return new DSObject((String) args[0]);
                        } catch (InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("Invalid arguments for IObject creation.");
                    }
                }));
    }

    @Test
    public void checkActorCreation()
            throws Exception {
        FeaturesCreatorActor actor = new FeaturesCreatorActor();
        assertNotNull(actor);
    }

    @Test
    public void checkCreateMessageByZipFileMethod()
            throws Exception {
        FeaturesCreatorActor actor = new FeaturesCreatorActor();
        CreateMessageWrapper wrapper = mock(CreateMessageWrapper.class);
        when(wrapper.getFileName()).thenReturn("test-feature-0.0.1-archive.zip");
        when(wrapper.getObservedDirectory()).thenReturn("target/test-classes/");

        doAnswer(invocationOnMock -> {
            try {
                IObject featureDescription = (IObject) ((List) invocationOnMock.getArguments()[0]).get(0);
                assertEquals(featureDescription.getValue(new FieldName("name")), "test-feature");
                assertEquals(
                        featureDescription.getValue(new FieldName("featureLocation")),
                        new Path("target/test-classes/test-feature-0.0.1-archive.zip")
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(wrapper).setJsonFeaturesDescription(any());

        actor.createMessageByFile(wrapper);
    }

    @Test
    public void checkCreateMessageByJsonFileMethod()
            throws Exception {
        FeaturesCreatorActor actor = new FeaturesCreatorActor();
        CreateMessageWrapper wrapper = mock(CreateMessageWrapper.class);
        when(wrapper.getFileName()).thenReturn("features.json");
        when(wrapper.getObservedDirectory()).thenReturn("target/test-classes/");
        doAnswer(invocationOnMock -> {
            try {
                IObject featureDescription = (IObject) ((List) invocationOnMock.getArguments()[0]).get(0);
                assertEquals(featureDescription.getValue(new FieldName("name")), "feature1");
                assertEquals(featureDescription.getValue(new FieldName("version")), "0.0.0");
                assertEquals(featureDescription.getValue(new FieldName("group")), "info.smart_tools.smartactors");
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(wrapper).setJsonFeaturesDescription(any());
        doAnswer(invocationOnMock -> {
            try {
                IObject featureDescription = (IObject) ((List) invocationOnMock.getArguments()[0]).get(0);
                assertEquals(featureDescription.getValue(new FieldName("repositoryId")), "repository1");
                assertEquals(featureDescription.getValue(new FieldName("type")), "default");
                assertEquals(featureDescription.getValue(new FieldName("url")), "info.smart_tools.smartactors");
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(wrapper).setJsonRepositoriesDescription(any());
        actor.createMessageByFile(wrapper);
        actor.createMessageByFile(wrapper);
    }

    @Test
    public void checkCreationFeatureByMessageMethodBasedOnJsonFile()
            throws Exception {
        List<IObject> repositoryStorage = new ArrayList<>();
        IOC.register(Keys.getOrAdd("feature-repositories"), new SingletonStrategy(repositoryStorage));

        FeaturesCreatorActor actor = new FeaturesCreatorActor();
        CreateFeaturesWrapper wrapper = mock(CreateFeaturesWrapper.class);
        IObject json = new DSObject(
                "{\n" +
                        "  \"repositories\": [\n" +
                        "    {\n" +
                        "      \"repositoryId\": \"repository1\",\n" +
                        "      \"type\": \"default\",\n" +
                        "      \"url\": \"info.smart_tools.smartactors\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"features\": [\n" +
                        "    {\n" +
                        "      \"name\": \"feature1\",\n" +
                        "      \"group\": \"info.smart_tools.smartactors\",\n" +
                        "      \"version\": \"0.0.0\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
        when(wrapper.getRepositoriesDescription()).thenReturn(
                (List<IObject>) json.getValue(new FieldName("repositories"))
        );
        when(wrapper.getFeaturesDescription()).thenReturn(
                (List<IObject>) json.getValue(new FieldName("features"))
        );
        Collection<IFeature> features = new HashSet<>();
        doAnswer(invocationOnMock -> {
            try {
                ((Collection<IFeature>)invocationOnMock.getArguments()[0]).forEach((f) -> {
                    features.add(f);
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(wrapper).setFeatures(any());
        actor.createFeaturesByMessage(wrapper);
        assertEquals(features.size(), 1);
        assertEquals(((IFeature) features.toArray()[0]).getName(), "feature1");
        assertEquals(((IFeature) features.toArray()[0]).getGroupId(), "info.smart_tools.smartactors");
        assertEquals(((IFeature) features.toArray()[0]).getVersion(), "0.0.0");
    }

    @Test
    public void checkCreationFeatureByMessageMethodBasedOnZipFile()
            throws Exception {
        List<IObject> repositoryStorage = new ArrayList<>();
        IOC.register(Keys.getOrAdd("feature-repositories"), new SingletonStrategy(repositoryStorage));

        FeaturesCreatorActor actor = new FeaturesCreatorActor();
        CreateFeaturesWrapper wrapper = mock(CreateFeaturesWrapper.class);
        IObject json = new DSObject(
                "{\"features\": [{\"name\":\"test-feature\",\"version\":null,\"group\":null}]}"
        );
        ((List<IObject>) json.getValue(new FieldName("features"))).get(0).setValue(new FieldName("featureLocation"), new Path("target/test-classes/test-feature-0.0.1-archive.zip"));
        when(wrapper.getFeaturesDescription()).thenReturn(
                (List<IObject>) json.getValue(new FieldName("features"))
        );
        Collection<IFeature> features = new HashSet<>();
        doAnswer(invocationOnMock -> {
            try {
                ((Collection<IFeature>)invocationOnMock.getArguments()[0]).forEach((f) -> {
                    features.add(f);
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(wrapper).setFeatures(any());
        actor.createFeaturesByMessage(wrapper);
        assertEquals(features.size(), 1);
        assertEquals(((IFeature) features.toArray()[0]).getName(), "test-feature");
        assertEquals(((IFeature) features.toArray()[0]).getGroupId(), null);
        assertEquals(((IFeature) features.toArray()[0]).getVersion(), null);
        assertEquals(((IFeature) features.toArray()[0]).getFeatureLocation(), new Path("target/test-classes/test-feature-0.0.1-archive.zip"));
    }
}
