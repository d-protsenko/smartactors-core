package info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.exception.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.exceptions.ClusterMessageSendException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Collection;

/**
 * Interface for object providing methods for communication between cluster nodes.
 */
public interface ICommunicationChannel {
    /**
     * Get identifier of current node.
     *
     * @return
     */
    INodeId getThisNodeId();

    /**
     * Get snapshot of list of all available nodes.
     *
     * @return
     */
    Collection<INodeId> getNodeList();

    /**
     * Add a callback to be called when message of given type is received.
     *
     * @param messageType
     * @param listener
     * @throws InvalidArgumentException if {@code messageType} is not valid message type
     * @throws InvalidStateException if there already is a listener for messages of given type
     */
    void setMessageListener(Object messageType, IBiAction<INodeId, IObject> listener)
            throws InvalidArgumentException, InvalidStateException;

    /**
     * Send a unicast message to specified node.
     *
     * @param dst
     * @param type
     * @param message
     * @throws ClusterMessageSendException if any error occurs sending the message
     */
    void sendUnicast(INodeId dst, Object type, IObject message) throws ClusterMessageSendException;

    /**
     * Send a broadcast message to all available nodes.
     *
     * @param type
     * @param message
     * @throws ClusterMessageSendException if any error occurs sending the message
     */
    void sendBroadcast(Object type, IObject message) throws ClusterMessageSendException;

    /**
     * Add a callback to be called when cluster topology (set of available nodes) changes.
     *
     * @param listener
     * @throws InvalidArgumentException if {@code listener} is {@code null}
     */
    void addTopologyChangeListener(IAction<Void> listener)
            throws InvalidArgumentException;
}
