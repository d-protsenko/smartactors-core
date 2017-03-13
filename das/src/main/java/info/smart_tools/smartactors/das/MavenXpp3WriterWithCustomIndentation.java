package info.smart_tools.smartactors.das;

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

    private String indentationString;

    public MavenXpp3WriterWithCustomIndentation(final String indentationString) {
        this.indentationString = indentationString;
    }

    @Override
    public void write(Writer writer, Model model)
            throws IOException {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", this.indentationString);
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
        serializer.setOutput( writer );
        serializer.startDocument( model.getModelEncoding(), null );

        try {
            Method method = getClass().getSuperclass().getDeclaredMethod("writeModel", new Class[]{Model.class,  String.class, XmlSerializer.class});
            method.setAccessible(true);
            method.invoke(this, new Object[]{model, "project", serializer});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }
        serializer.endDocument();
    }

    @Override
    public void write(OutputStream stream, Model model) throws IOException {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", this.indentationString);
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
        serializer.setOutput( stream, model.getModelEncoding() );
        serializer.startDocument( model.getModelEncoding(), null );
        try {
            Method method = getClass().getSuperclass().getDeclaredMethod("writeModel", new Class[]{Model.class,  String.class, XmlSerializer.class});
            method.setAccessible(true);
            method.invoke(this, new Object[]{model, "project", serializer});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }
        serializer.endDocument();
    }
}
