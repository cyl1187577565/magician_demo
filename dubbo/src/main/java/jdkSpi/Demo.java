package jdkSpi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2021-03-07 19:26
 **/
public class Demo {
    public static void main(String[] args) {
        ServiceLoader<SomeService> load = ServiceLoader.load(SomeService.class);
        Iterator<SomeService> iterator = load.iterator();

        while (iterator.hasNext()) {
            SomeService someService = iterator.next();
            someService.doSomething();
        }
    }
}
