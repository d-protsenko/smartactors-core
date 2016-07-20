/**
 * Package contains implementation of {@link info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator}.
 * Create chain of receivers:
 * Generates {@code HandlerReceivers} foreach method and puts its to new
 * instance of {@link info.smart_tools.smartactors.core.handler_routing_receiver.HandlerRoutingReceiver}.
 * After that puts instance
 * of {@link info.smart_tools.smartactors.core.handler_routing_receiver.HandlerRoutingReceiver}
 * to the new instance of {@link info.smart_tools.smartactors.core.actor_receiver.ActorReceiver}.
 */
package info.smart_tools.smartactors.core.actor_receiver_creator;