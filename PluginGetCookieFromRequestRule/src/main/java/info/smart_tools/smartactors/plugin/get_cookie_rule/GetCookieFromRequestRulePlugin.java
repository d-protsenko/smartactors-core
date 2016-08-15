package info.smart_tools.smartactors.plugin.get_cookie_rule;

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
import info.smart_tools.smartactors.transformation_rules.get_cookie_from_request.GetCookieFromRequestRule;

/**
 * Plugin for register GetCookieFromRequestRule in IOC
 */
public class GetCookieFromRequestRulePlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public GetCookieFromRequestRulePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("GetCookieFromRequestRulePlugin");

            item
                    .after("IOC")
                    .after("wds_object")
                    .before("configure")
                    .process(() -> {
                        try {
                            IOC.resolve(
                                    Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()),
                                    "getCookieFromRequestRule",
                                    new GetCookieFromRequestRule()
                            );
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(
                                    "GetCookieFromRequestRule plugin can't load: can't get GetCookieFromRequestRule key", e
                            );
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load GetCookieFromRequestRule plugin", e);
        }
    }
}
