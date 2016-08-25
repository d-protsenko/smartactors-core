package info.smart_tools.smartactors.plugin.get_first_not_null_rule;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.transformation_rules.get_first_not_null.GetFirstNotNullRule;

public class GetFirstNotNullRulePlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public GetFirstNotNullRulePlugin(IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("GetFirstNotNullRulePlugin");

            item
                    .after("IOC")
                    .before("starter")
                    .process(()-> {
                        try {
                            IKey ruleKey = Keys.getOrAdd(GetFirstNotNullRule.class.getCanonicalName());
                            IOC.register(ruleKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> new GetFirstNotNullRule()
                            ));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("Can't get key for load", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("Can't apply lambda", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("Can't register the strategy", e);
                        }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't create BootstrapItem", e);
        }
    }
}
