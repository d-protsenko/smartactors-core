package info.smart_tools.smartactors.plugin.base.bootstrap_plugin;

import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Base class for {@link IPlugin plugins} registering {@link IBootstrapItem bootstrap items} in a {@link IBootstrap bootstrap} passed to
 * constructor.
 *
 * <p>
 *     Example of a plugin:
 * </p>
 *
 * <pre>
 *     public class MyPlugin extends BootstrapPlugin {
 *         public MyPlugin(final IBootstrap bs) {
 *             super(bs);
 *         }
 *
 *         {@literal @}Item("my_item_1")                        // Name of the item
 *         {@literal @}After({"other_item_1", "other_item_2")   // Names of items to execute after which
 *         {@literal @}Before({"one_more_item"})                // Names of the items to execute before which
 *         public void myItem1()
 *                 throws SomeException {
 *             // . . . Body of the item
 *         }
 *
 *         {@literal @}Item("my_item_2")                        // {@literal @}After and {@literal @}Before annotations are optional
 *         public void myItem2() {
 *             // . . .
 *         }
 *     }
 * </pre>
 */
public abstract class BootstrapPlugin implements IPlugin {
    /**
     * Annotation used to mark the methods of plugins extending {@link BootstrapPlugin} for which the bootstrap items should e created.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Item {
        /**
         * Name of the bootstrap item to create.
         *
         * @return name of bootstrap item
         */
        String value();
    }

    /**
     * Annotation storing array of names of bootstrap items that should be executed before the item created for annotated method.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface After {
        /**
         * Array of names of items after which the item should be executed.
         *
         * @return array of item names
         */
        String[] value();
    }

    /**
     * Annotation storing array of names of bootstrap items that should be executed after the item created for annotated method.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Before {
        /**
         * Array of names of items before which the item should be executed.
         *
         * @return array of item names
         */
        String[] value();
    }

    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * <p>
     *     Constructor of a plugin extending this class should look like the following:
     * </p>
     *
     * <pre>
     *     class MyPlugin extends BootstrapPlugin {
     *         public MyPlugin(final IBootstrap bs) {
     *             super(bs);
     *         }
     *     }
     * </pre>
     *
     * @param bootstrap    the bootstrap
     */
    protected BootstrapPlugin(final IBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public final void load() throws PluginException {
        try {
            for (Method method : this.getClass().getMethods()) {
                if (null == method.getAnnotation(Item.class)) {
                    continue;
                }

                bootstrap.add(new MethodBootstrapItem(this, method));
            }
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
