import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.converters.JavetBridgeConverter;
import com.caoccao.javet.interop.engine.JavetEngine;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.values.reference.V8ValueGlobalObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Context implements Runnable {

    private static final boolean debug = true;

    private static final JavetEnginePool<NodeRuntime> pool;

    static {
        pool = new JavetEnginePool<>(new JavetEngineConfig()
                .setAllowEval(true)
                .setAutoSendGCNotification(true)
                .setJSRuntimeType(JSRuntimeType.Node)
        );
    }

    private final String content;
    private final String resourceName;


    public Context(File file) throws IOException {
        this(file.toPath());
    }

    public Context(Path path) throws IOException {
        this(Files.readString(path), path.toFile().getAbsolutePath());
    }

    public Context(String code, String resourceName){
        this.content = code;
        this.resourceName = resourceName;
    }


    @Override
    public void run() {
            if (debug) {
                System.out.println(this.content);
                System.out.println(this.resourceName);
            }
            try (JavetEngine<NodeRuntime> engine = (JavetEngine<NodeRuntime>) pool.getEngine()){
                try (NodeRuntime runtime = engine.getV8Runtime()){
                    JavetBridgeConverter converter = new JavetBridgeConverter();
                    runtime.setConverter(converter);
                    JavetJVMInterceptor interceptor = new JavetJVMInterceptor(runtime);
                    try (V8ValueGlobalObject globalObject = runtime.getGlobalObject()){
                        interceptor.register(globalObject);
                    }
                    runtime.getExecutor(this.content).setModule(true).setResourceName(this.resourceName).executeVoid();
                    runtime.await();
                }
            } catch (JavetException e) {
                e.printStackTrace();
            }
    }
}
