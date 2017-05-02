package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces;

import info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.exceptions.MigrationListenerException;
import info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces.exceptions.MigrationRejectException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Set of callbacks provided by {@link IResponsible responsible} object to handle process of incoming migration.
 */
public interface IIncomingMigrationListener {
    /**
     * Called before the first migration data message.
     *
     * @throws MigrationListenerException if any error occurs
     * @throws MigrationRejectException if listener rejects migration
     */
    void onDataStart() throws MigrationListenerException, MigrationRejectException;

    /**
     * Called when migration data message is received.
     *
     * @param data    data received from object the responsibility is migrating from
     * @throws MigrationListenerException if any error occurs
     */
    void onDataReceived(IObject data) throws MigrationListenerException;

    /**
     * Called when there is no more migration data messages.
     *
     * @throws MigrationListenerException if any error occurs
     */
    void onDataEnd() throws MigrationListenerException;

    /**
     * Called when migration fails on source side.
     *
     * @throws MigrationListenerException if any error occurs
     */
    void onFailure() throws MigrationListenerException;
}
