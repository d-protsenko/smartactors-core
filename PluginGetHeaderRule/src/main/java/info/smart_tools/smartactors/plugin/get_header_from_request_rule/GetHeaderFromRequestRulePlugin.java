package info.smart_tools.smartactors.plugin.get_header_from_request_rule;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.transformation_rules.get_header_from_request.GetHeaderFromRequestRule;

/**
 * Plugin for register GetHeaderFromRequestRule in IOC
 */
public class GetHeaderFromRequestRulePlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public GetHeaderFromRequestRulePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("GetHeaderFromRequestRulePlugin");

            item
                .after("IOC")
                .after("wds_object")
                .before("starter")
                    .process(() -> {
                        try {
                            //call IOC.resolve for put GetHeaderFromRequestRule into cache of ResolveByNameDependency strategy
                            IOC.resolve(
                                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()),
                                "getHeaderFromRequestRule",
                                new GetHeaderFromRequestRule()
                            );
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(
                                "GetHeaderFromRequestRule plugin can't load: can't get GetHeaderFromRequestRule key", e
                            );
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load GetHeaderFromRequestRule plugin", e);
        }
    }
}
