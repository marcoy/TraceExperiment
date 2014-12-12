package trace.exp;

import com.sun.btrace.annotations.*;
import com.sun.btrace.AnyType;
import static com.sun.btrace.BTraceUtils.*;

@BTrace public class TraceExp {
    @OnMethod(
        clazz="/traceme\\..*/",
        method="someMethod"
    )

    public static void anyRead(@ProbeClassName String pcn, AnyType arg) {
        println(pcn);
        println(arg);
    }
}
