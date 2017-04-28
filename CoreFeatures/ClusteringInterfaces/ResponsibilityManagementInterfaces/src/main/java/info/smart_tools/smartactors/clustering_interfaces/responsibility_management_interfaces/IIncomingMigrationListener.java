package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces;

import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Set of callbacks provided by {@link IResponsible responsible} object to handle process of incoming migration.
 */
public interface IIncomingMigrationListener {
    /**
     * Called before the first migration data message.
     */
    // TODO:: Exceptions
    void onDataStart();

    /**
     * Called when migration data message is received.
     *
     * @param data
     */
    // TODO:: Exceptions
    void onDataReceived(IObject data);

    /**
     * Called when there is no more migration data messages.
     */
    // TODO:: Exceptions
    void onDataEnd();

    /**
     * Called when migration fails on source side.
     */
    // TODO:: Exceptions
    void onFailure();
}
