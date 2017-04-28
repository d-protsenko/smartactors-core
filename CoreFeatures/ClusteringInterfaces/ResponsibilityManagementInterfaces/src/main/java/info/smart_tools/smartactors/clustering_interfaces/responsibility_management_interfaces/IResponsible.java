package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces;

import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.INodeId;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface for a object that may claim one or more responsibilities on this node.
 *
 * Object implementing this interface may be associated with a message receiver supporting migrations or a service such as scheduler or
 * checkpoint.
 */
public interface IResponsible {
    /**
     * Called when the responsibility state (list of nodes claiming the responsibility) is updated.
     *
     * @param responsibility
     */
    // TODO:: Exceptions
    void onUpdate(IResponsibility responsibility);

    /**
     * Called when migration of the responsibility to this object is required.
     *
     * @param responsibility
     * @param fromNode
     * @param migrationArgs
     * @return
     */
    // TODO:: Exceptions
    IIncomingMigrationListener onIncommingMigrationRequest(
            IResponsibility responsibility, INodeId fromNode, IObject migrationArgs);

    /**
     * Called when migration of the responsibility from this object is required.
     *
     * @param responsibility
     * @param toNode
     * @param migrationArgs
     * @param listener
     */
    // TODO:: Exceptions
    void onOutgoingMigrationRequest(
            IResponsibility responsibility, INodeId toNode, IObject  migrationArgs, IOutgoingMigrationListener listener);
}
