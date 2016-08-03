package info.smart_tools.smartactors.plugin.standard_object_creators;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 *
 */
public class PluginStandardObjectCreators implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginStandardObjectCreators(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            BootstrapItem creatorsItem = new BootstrapItem("standard_object_creators");

            creatorsItem
                    .after("IOC")
//                    .after("field_name")
                    .after("IFieldNamePlugin")
                    .before("configure")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.getOrAdd(IRoutedObjectCreator.class.getCanonicalName() + "#raw"),
                                    new SingletonStrategy(new RawObjectCreator()));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("StandardObjectCreators plugin can't load: can't get StandardObjectCreators key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("StandardObjectCreators plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("StandardObjectCreators plugin can't load: can't register new strategy", e);
                        }
                    });

            bootstrap.add(creatorsItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
