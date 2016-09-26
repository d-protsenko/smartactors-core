package info.smart_tools.smartactors.core.examples.wrapper;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Example of plugin to register transformation strategies for Wrapper.
 */
public class ConcatSplitRulesPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Creates the plugin.
     * @param bootstrap bootstrap where this plugin puts {@link IBootstrapItem}.
     */
    public ConcatSplitRulesPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {   // this constructor signature is required
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ConcalSplitRulesPlugin");
            item.after("IOC");
            item.after("ioc_keys");
            item.process(() -> {
                try {
                    registerConcatRule();
                    registerSplitRule();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to register strategies in ConcatSplitRulesPlugin", e);
                }
            });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Failed to load ConcatSplitRulesPlugin", e);
        }
    }

    private void registerConcatRule() throws ResolutionException, RegistrationException {
        //call IOC.resolve for put "concat_strategy" into cache of ResolveByNameDependency strategy
        IKey key = Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName());
        String name = "concat_strategy";
        IResolveDependencyStrategy strategy = new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
                String result = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining());
                return (T) result;
            }
        };
        IOC.resolve(key, name, strategy);
    }

    private void registerSplitRule() throws ResolutionException, RegistrationException {
        //call IOC.resolve for put "concat_strategy" into cache of ResolveByNameDependency strategy
        IKey key = Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName());
        String name = "split_strategy";
        IResolveDependencyStrategy strategy = new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
                String value = (String) args[0];
                String delimiters = (String) args[1];
                List<String> result = new ArrayList<>();
                StringTokenizer tokenizer = new StringTokenizer(value, delimiters, false);
                while (tokenizer.hasMoreTokens()) {
                    result.add(tokenizer.nextToken());
                }
                return (T) result;
            }
        };
        IOC.resolve(key, name, strategy);
    }

}
