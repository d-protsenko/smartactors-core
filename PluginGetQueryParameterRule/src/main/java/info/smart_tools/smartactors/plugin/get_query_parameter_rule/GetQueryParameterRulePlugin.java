package info.smart_tools.smartactors.plugin.get_query_parameter_rule;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.transformation_rules.get_query_parameter.GetQueryParameterRule;

/**
 * Plugin for register {@link info.smart_tools.smartactors.transformation_rules.get_query_parameter.GetQueryParameterRule}
 */
public class GetQueryParameterRulePlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public GetQueryParameterRulePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("GetQueryParameterFromRequestRulePlugin");

            item
                    .after("IOC")
                    .after("wds_object")
                    .before("configure")
                    .process(() -> {
                        try {
                            //call IOC.resolve for put GetQueryParameterRule into cache of ResolveByNameDependency strategy
                            IOC.resolve(
                                    Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()),
                                    "getQueryParameterFromRequestRule",
                                    new GetQueryParameterRule()
                            );
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(
                                    "GetQueryParameterFromRequestRule plugin can't load: can't get GetQueryParameterFromRequestRule key", e
                            );
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load GetQueryParameterFromRequestRule plugin", e);
        }
    }
}
