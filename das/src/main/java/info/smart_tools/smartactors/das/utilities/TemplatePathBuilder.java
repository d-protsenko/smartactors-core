package info.smart_tools.smartactors.das.utilities;

import java.io.File;

public class TemplatePathBuilder {
    private static final String RESOURCE_DIR = "resources";

    public static String buildTemplatePath(String templateName) {
        return TemplatePathBuilder.GetExecutionPath() +
                File.separator + RESOURCE_DIR + File.separator + templateName;
    }

    private static String GetExecutionPath(){
        String absolutePath = FileBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));

        return absolutePath;
    }
}
