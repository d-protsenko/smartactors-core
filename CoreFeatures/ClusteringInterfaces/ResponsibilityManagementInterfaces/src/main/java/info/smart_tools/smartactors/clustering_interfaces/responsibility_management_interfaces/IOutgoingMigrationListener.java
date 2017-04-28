package info.smart_tools.smartactors.clustering_interfaces.responsibility_management_interfaces;

import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 *
 */
public interface IOutgoingMigrationListener {
    // TODO:: Exceptions
    void startMigration();

    // TODO:: Exceptions
    void sendMigrationData(IObject data);

    // TODO:: Future?
    // TODO:: Exceptions
    void finishMigration();

    // TODO:: Exceptions
    void failMigration(Throwable err);
}
