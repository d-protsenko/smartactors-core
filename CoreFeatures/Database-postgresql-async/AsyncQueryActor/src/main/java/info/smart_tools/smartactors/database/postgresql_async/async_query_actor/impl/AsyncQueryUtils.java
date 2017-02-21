package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

/**
 *
 */
public enum AsyncQueryUtils { ;
    /**
     * Replaces JDBC placeholders ({@code "?"}) by native postgres placeholders ({@code "$"+index}).
     *
     * @param buf           the buffer to replace argument placeholders in
     * @param arguments     the arguments that will be substituted to placeholders
     * @return number of query parameters
     */
    public static int reformatBuffer(final StringBuffer buf, final Object[] arguments) {
        int cIndex = 0;
        int aIndex = 0;

        while ((cIndex = buf.indexOf("?", cIndex)) > 0) {
            if (buf.charAt(cIndex + 1) == '?') {
                buf.replace(cIndex, cIndex + 2, "?");
            } else {
                int aI = aIndex++;
                String castExpr = "::text";

                if (arguments[aI] instanceof Number) {
                    castExpr = "::numeric";
                } else {
                    arguments[aI] = String.valueOf(arguments[aI]);
                }

                buf.replace(cIndex, cIndex + 1, "$".concat(String.valueOf(aIndex)).concat(castExpr));
            }

            ++cIndex;
        }

        return aIndex;
    }

    /**
     * Get count of JDBC parameter placeholders in SQL statement.
     *
     * @param buf    the buffer containing the statement
     * @return cout of parameter placeholders
     */
    public static int countParams(final StringBuffer buf) {
        int cIndex = 0;
        int nParams = 0;

        while ((cIndex = buf.indexOf("?", cIndex)) > 0) {
            if (buf.charAt(cIndex + 1) == '?') {
                ++cIndex;
            } else {
                ++nParams;
            }

            ++cIndex;
        }

        return nParams;
    }
}
