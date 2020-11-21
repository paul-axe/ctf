import java.lang.instrument.Instrumentation;
import java.lang.Runtime;

public class A {

    public static void agentmain(String string, Instrumentation instrumentation) throws Exception {
        java.lang.Runtime.getRuntime().exec(string);
    }

}
