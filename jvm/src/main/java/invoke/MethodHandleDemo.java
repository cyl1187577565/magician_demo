package invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @description: 方法句柄demo
 * @author: yulong.cao
 * @since: 2021-03-09 21:57
 **/
public class MethodHandleDemo {
    public static void main(String[] args) throws Throwable{
        Demo demo = new Demo();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle hello = lookup.findVirtual(Demo.class, "hello", MethodType.methodType(String.class));
        String result = (String) hello.invokeExact(demo);
        System.out.println(result);
    }
}
