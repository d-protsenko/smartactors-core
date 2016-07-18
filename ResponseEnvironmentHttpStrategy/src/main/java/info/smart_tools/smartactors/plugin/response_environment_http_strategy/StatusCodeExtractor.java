package info.smart_tools.smartactors.plugin.response_environment_http_strategy;


import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Class for extract status code
 */
final class StatusCodeExtractor {
    private StatusCodeExtractor() {
    }

    /**
     * Method for extract status code
     *
     * @param environment Environment {@link IObject}
     * @return status code for response from environment
     */
    static int extract(final IObject environment) {
        return 200;
    }
}
