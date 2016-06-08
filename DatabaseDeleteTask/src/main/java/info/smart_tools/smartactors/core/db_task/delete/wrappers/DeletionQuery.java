package info.smart_tools.smartactors.core.db_task.delete.wrappers;

public interface DeletionQuery {
    String getCollectionName();
    int countDocumentIds();
    Long getDocumentIds(int index);
}
