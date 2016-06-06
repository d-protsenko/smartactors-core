package delete.psql.wrappers;

public interface DeletionQuery {
    String getCollectionName();
    int countDocumentIds();
    Long getDocumentIds(int index);
}
