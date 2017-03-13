package info.smart_tools.smartactors.das;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ProjectInformation {

    public static IObject load(final File file) {
        IObject info = null;
        try (Scanner scanner = new Scanner(file)) {
            info = new DSObject(scanner.useDelimiter("\\Z").next());
            scanner.close();
        } catch (IOException e) {
            System.out.println("Could not read project meta data: ");
            System.err.println(e);
        } catch (InvalidArgumentException e) {
            System.out.println("Could not parse project meta data: ");
            System.err.println(e);
        }

        return info;
    }

    public static void save(final File file, final IObject info) {
        BufferedWriter writer = null;
        try {
            String s = ((DSObject) info).serialize();
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(s);
        } catch (SerializeException e) {
            System.out.println("Could not serialize project meta data: ");
            System.err.println(e);
        } catch (IOException e) {
            System.out.println("Could not save project meta data: ");
            System.err.println(e);
        } finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close write buffer: ");
                System.err.println(e);
            }
        }
    }
}
