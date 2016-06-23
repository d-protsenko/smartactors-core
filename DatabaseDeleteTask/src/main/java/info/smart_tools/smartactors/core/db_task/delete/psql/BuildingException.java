package info.smart_tools.smartactors.core.db_task.delete.psql;

class BuildingException extends Exception {
    BuildingException(String message) {
        super(message);
    }

    BuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
