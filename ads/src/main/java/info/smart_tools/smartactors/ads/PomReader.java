package info.smart_tools.smartactors.ads;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PomReader {

    public static String getGroupId(final File target)
            throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(target);
            model = mavenReader.read(reader);
            reader.close();
            return model.getGroupId();
        } catch (Throwable e) {
            System.out.println("Could not read the pom file.");
            throw new Exception("Could not read the pom file.");
        }
    }

    public static String getVersion(final File target)
            throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(target);
            model = mavenReader.read(reader);
            reader.close();
            return model.getVersion();
        } catch (Throwable e) {
            System.out.println("Could not read the pom file.");
            throw new Exception("Could not read the pom file.");
        }
    }

    public static String getArtifactId(final File target)
            throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(target);
            model = mavenReader.read(reader);
            reader.close();
            return model.getArtifactId();
        } catch (Throwable e) {
            System.out.println("Could not read the pom file.");
            throw new Exception("Could not read the pom file.");
        }
    }

    public static List<String> getModules(final File target)
            throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(target);
            model = mavenReader.read(reader);
            reader.close();
            return model.getModules();
        } catch (Throwable e) {
            System.out.println("Could not read the pom file.");
            throw new Exception("Could not read the pom file.");
        }
    }

    public static List<Dependency> getDependencies(final File target)
            throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(target);
            model = mavenReader.read(reader);
            reader.close();
            return model.getDependencies();
        } catch (Throwable e) {
            System.out.println("Could not read the pom file.");
            throw new Exception("Could not read the pom file.");
        }
    }

}
