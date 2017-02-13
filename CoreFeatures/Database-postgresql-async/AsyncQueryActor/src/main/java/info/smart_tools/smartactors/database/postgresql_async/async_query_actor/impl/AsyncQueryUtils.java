package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

/**
 *
 */
public class AsyncQueryUtils {
    /**
     * Replaces JDBC placeholders ({@code "?"}) by native postgres placeholders ({@code "$"+index}).
     *
     * @param buf    the buffer to replace argument placeholders in
     * @return number of query parameters
     */
    public static int reformatBuffer(final StringBuffer buf) {
        int cIndex = 0;
        int aIndex = 0;

        while ((cIndex = buf.indexOf("?", cIndex)) > 0) {
            if (buf.charAt(cIndex + 1) == '?') {
                buf.replace(cIndex, cIndex + 2, "?");
            } else {
                int aI = ++aIndex;
                buf.replace(cIndex, cIndex + 1, "$".concat(String.valueOf(aI)));
            }

            ++cIndex;
        }

        return aIndex;
    }
}
