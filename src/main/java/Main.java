import configuration.TraceRestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import traceme.TraceMe;

import java.lang.management.ManagementFactory;

public class Main {

//    public static byte[] compile() {
//        final String classpath = System.getProperty("java.class.path");
//        final String fileName = "Memory.java";
//        final File srcFile = new File(fileName);
//        final Writer out = new PrintWriter(System.out);
//        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        final StandardJavaFileManager stdManger = compiler.getStandardFileManager(null, null, null);
//        final MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManger, null);
//        final Iterable<? extends JavaFileObject> compUnits = stdManger.getJavaFileObjects(srcFile);
//        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
//
//        final List<String> options = new ArrayList<String>();
//        options.add("-Xlint:all");
//        options.add("-g:lines");
//        options.add("-deprecation");
//        options.add("-source");
//        options.add("1.6");
//        options.add("-target");
//        options.add("1.6");
//        options.add("-sourcepath");
//        options.add(".");
//        options.add("-classpath");
//        options.add(classpath);
//
//        final JavacTask task =
//                (JavacTask) compiler.getTask(out, manager, diagnostics, options, null, compUnits);
//        final Verifier verifier = new Verifier(false);
//        task.setTaskListener(verifier);
//        task.setProcessors(Lists.newArrayList(verifier));
//
//        task.call();
//
//        Map<String, byte[]> classBytesMap = manager.getClassBytes();
//        Collection<byte[]> classBytes = classBytesMap.values();
//
//        ClassReader cr = new ClassReader(classBytes.iterator().next());
//        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//        cr.accept(new Postprocessor(cw), ClassReader.EXPAND_FRAMES + ClassReader.SKIP_DEBUG);
//
//        return cw.toByteArray();
//    }

    public static void main(String[] args) throws InterruptedException {
        final TraceMe tm = new TraceMe();
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TraceRestConfiguration.class);
        ctx.refresh();

        final String classpath = System.getProperty("java.class.path");
        final String javaHome  = System.getenv("JAVA_HOME");
        final String toolsJar  = javaHome + "/lib/tools.jar";
        System.setProperty("com.sun.btrace.debug", "true");

        System.out.println("ToolsJar: " + toolsJar);
        System.out.println("Classpath: " + classpath);
        System.out.println("CommandListener: " + ManagementFactory.getRuntimeMXBean().getName());

//        ctx.close();

//        byte[] byteCode = compile();
//        System.out.println("Bytecode: " + byteCode);

//        final Compiler compiler = new Compiler(null, true);
//        final Writer out = new PrintWriter(System.out);
//        Map<String, byte[]> m = compiler.compile(new File("Memory.java"), out, ".", classpath);
//        System.out.println(m);

//        final Client client = new Client(2020, ".", true, false, false, false, null);
//        final byte[] code   = client.compile("/Users/marcoy/Downloads/btrace/samples/Memory.java", ".");
//        final byte[] code   = client.compile("Memory.java", toolsJar);
//        System.out.println("Code: " + code);

        System.out.println("Running ...");
        while(true) {
            tm.someMethod(System.currentTimeMillis());
            Thread.sleep(5000);
        }
    }
}
