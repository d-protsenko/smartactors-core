package info.smart_tools.smartactors.core.db_tasks.utils;

import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class IDContainer {
    private static final ConcurrentMap<String, IField> CACHED_DOCUMENTS_IDS = new ConcurrentHashMap<>();

    /**
     *
     * @param collection
     * @return
     * @throws ResolutionException
     */
    public static IField getIdFieldFor(final String collection) throws ResolutionException {
        IField id = CACHED_DOCUMENTS_IDS.get(collection);
        if (id == null) {
            id = IOC.resolve(Keys.getOrAdd(IField.class.toString()), collection + "Id");
            CACHED_DOCUMENTS_IDS.put(collection, id);
        }

        return id;
    }
}
