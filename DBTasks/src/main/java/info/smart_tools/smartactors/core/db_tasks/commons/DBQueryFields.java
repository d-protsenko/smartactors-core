package info.smart_tools.smartactors.core.db_tasks.commons;


import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 *
 */
public final class DBQueryFields {

    public static final IField DOCUMENT;
    public static final IField COLLECTION;
    public static final IField INDEXES;
    public static final IField DOCUMENT_ID;
    public static final IField SEARCH_RESULT;

    static {
        try {
            DOCUMENT_ID = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "documentId");
            DOCUMENT = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document");
            COLLECTION = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "collection");
            INDEXES = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "indexes");
            SEARCH_RESULT = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "searchResult");
        } catch (ResolutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private DBQueryFields() { }
}
