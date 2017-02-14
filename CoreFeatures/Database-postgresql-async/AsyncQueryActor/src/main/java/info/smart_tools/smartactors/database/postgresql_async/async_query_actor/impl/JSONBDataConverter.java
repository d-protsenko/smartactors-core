package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

import com.github.pgasync.impl.Oid;
import com.github.pgasync.impl.conversion.DataConverter;

/**
 *
 */
public class JSONBDataConverter extends DataConverter {
    @Override
    public byte[] toBytes(final Oid oid, final byte[] value) {
        if (oid == Oid.JSONB) {
            return value;
        }

        return super.toBytes(oid, value);
    }

    @Override
    public String toString(final Oid oid, final byte[] value) {
        if (oid == Oid.JSONB) {
            return new String(value);
        }
        return super.toString(oid, value);
    }

    /** The single instance */
    public static final JSONBDataConverter INSTANCE = new JSONBDataConverter();
}
