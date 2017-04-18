package info.smart_tools.smartactors.das.utilities;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MavenXpp3WriterWithCustomIndentation extends MavenXpp3Writer {

    private final static String SERIALIZER_INDENTATION_PROPERTY = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";
    private final static String SERIALIZER_LINE_SEPARATOR_PROPERTY = "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator";

    private final static String WRITE_MODEL_METHOD_NAME = "writeModel";
    private final static String PROJECT_TAG_NAME = "project";

    private String indentationString;
    private String lineSeparator = "\n";

    public MavenXpp3WriterWithCustomIndentation(final String indentationString) {
        this.indentationString = indentationString;
    }

    public MavenXpp3WriterWithCustomIndentation(final String indentationString, final String lineSeparator) {
        this.indentationString = indentationString;
        this.lineSeparator = lineSeparator;
    }

    @Override
    public void write(Writer writer, Model model)
            throws IOException {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty(SERIALIZER_INDENTATION_PROPERTY , this.indentationString);
        serializer.setProperty(SERIALIZER_LINE_SEPARATOR_PROPERTY , this.lineSeparator);
        serializer.setOutput(writer);
        serializer.startDocument(model.getModelEncoding(), null);
        try {
            Method method = getClass().getSuperclass().getDeclaredMethod(
                    WRITE_MODEL_METHOD_NAME, new Class[]{Model.class,  String.class, XmlSerializer.class}
            );
            method.setAccessible(true);
            method.invoke(this, new Object[]{model, PROJECT_TAG_NAME, serializer});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }
        serializer.endDocument();
    }

    @Override
    public void write(OutputStream stream, Model model)
            throws IOException {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty(SERIALIZER_INDENTATION_PROPERTY, this.indentationString);
        serializer.setProperty(SERIALIZER_LINE_SEPARATOR_PROPERTY, this.lineSeparator);
        serializer.setOutput(stream, model.getModelEncoding());
        serializer.startDocument(model.getModelEncoding(), null);
        try {
            Method method = getClass().getSuperclass().getDeclaredMethod(
                    WRITE_MODEL_METHOD_NAME, new Class[]{Model.class,  String.class, XmlSerializer.class}
            );
            method.setAccessible(true);
            method.invoke(this, new Object[]{model, PROJECT_TAG_NAME, serializer});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }
        serializer.endDocument();
    }
}
