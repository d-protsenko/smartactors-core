package info.smart_tools.smartactors.plugin.base.bootstrap_plugin;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iplugin.IPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Bootstrap item created from a plugin method.
 */
class MethodBootstrapItem extends BootstrapItem {

    /**
     * The constructor.
     *
     * @param plugin    the plugin instance
     * @param method    the method annotated with {@link BootstrapPlugin.Item}
     * @throws InvalidArgumentException if item name is {@code null}
     */
    MethodBootstrapItem(final IPlugin plugin, final Method method)
            throws InvalidArgumentException {
        super(method.getAnnotation(BootstrapPlugin.Item.class).value());

        if (method.getParameterCount() != 0) {
            throw new InvalidArgumentException(
                    MessageFormat.format(
                            "Bootstrap item body method should have no parameters but method {0} of {1} (item ''{2}'') has some.",
                            method.getName(),
                            method.getDeclaringClass().getName(),
                            method.getAnnotation(BootstrapPlugin.Item.class).value()));
        }

        BootstrapPlugin.After after = method.getAnnotation(BootstrapPlugin.After.class);

        if (null != after) {
            for (String name : after.value()) {
                this.after(name);
            }
        }

        BootstrapPlugin.Before before = method.getAnnotation(BootstrapPlugin.Before.class);

        if (null != before) {
            for (String name : before.value()) {
                this.before(name);
            }
        }

        this.process(() -> {
            try {
                method.invoke(plugin);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ActionExecuteException(e);
            }
        });
    }
}
