package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces;

import info.smart_tools.smartactors.base.exception.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.INodeId;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Collection;

/**
 * Interface for object representing an abstract responsibility that may be claimed by one or more nodes of a cluster.
 *
 * A responsibility is any kind of activity that may be performed by nodes of a cluster e.g. "have a message receiver with {some properties}
 * and {some identifier}" or "execute scheduled tasks from {some database collection}".
 */
public interface IResponsibility {
    /**
     * Set the object that _may_ claim this responsibility on this node.
     *
     * Call of this method permanently attaches the responsible object to this responsibility but does not make the object claim it.
     *
     * @param responsible
     * @throws InvalidStateException
     */
    void setResponsible(IResponsible responsible) throws InvalidStateException;

    /**
     * Make the given object claim this responsibility.
     *
     * @param responsible
     * @throws InvalidStateException
     */
    void claim(IResponsible responsible) throws InvalidStateException;

    /**
     * Make the given object stop claiming this responsibility.
     *
     * @param responsible
     * @throws InvalidStateException
     */
    void disclaim(IResponsible responsible) throws InvalidStateException;

    /**
     * Get list of nodes where are objects claiming this responsibility.
     *
     * @return
     */
    Collection<INodeId> getResponsibleNodes();

    /**
     * Start migration of this responsibility from one node to another one.
     *
     * @param from
     * @param to
     * @param migrationArgs
     */
    // TODO:: Exceptions
    void migrate(INodeId from, INodeId to, IObject migrationArgs);
}
