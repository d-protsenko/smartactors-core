package info.smart_tools.smartactors.core.create_new_instance_strategy;

import java.io.IOException;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.DynamicClassLoader;

/**
 * Class extends StandardJavaFileManager
 */
public class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private CompiledCode compiledCode;
    private DynamicClassLoader cl;

    /**
     * Constructor
     * @param fileManager instance of {@link JavaFileManager}
     * @param compiledCode instance of {@link CompiledCode}
     * @param cl instance of {@link DynamicClassLoader}
     */
    protected ExtendedStandardJavaFileManager(
            final JavaFileManager fileManager, final CompiledCode compiledCode, final DynamicClassLoader cl
    ) {
        super(fileManager);
        this.compiledCode = compiledCode;
        this.cl = cl;
        this.cl.setCode(compiledCode);
    }

    /**
     * Get compiled code
     * @param location instance of {@link javax.tools.JavaFileManager.Location}
     * @param className full name of future class
     * @param kind instance of {@link Kind}
     * @param sibling ??
     * @return compiled code
     * @throws IOException if any errors occurred
     */
    public JavaFileObject getJavaFileForOutput(
            final Location location,
            final String className,
            final Kind kind,
            final FileObject sibling
    ) throws IOException {
        return this.compiledCode;
    }

    /**
     * Get current instance of {@link DynamicClassLoader}
     * @param location instance of {@link javax.tools.JavaFileManager.Location}
     * @return instance of current {@link DynamicClassLoader}
     */
    public ClassLoader getClassLoader(final Location location) {
        return this.cl;
    }
}

