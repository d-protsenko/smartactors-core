/**
 * Implementation of {@link info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer}
 *
 * <p>
 * This implementation has these specific features:
 * <ul>
 *     <li>key-{@link info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy} storage</li>
 *     <li>uses {@link info.smart_tools.smartactors.scope.iscope.IScope}</li>
 *     <li>stores the link to the parent container to make the recursive resolving
 *          when the strategy doesn't exist in the current container</li>
 * </ul>
 * </p>
 */
package info.smart_tools.smartactors.version_management.versioned_strategy_container;