package jdkSpi.impl;

import jdkSpi.SomeService;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2021-03-07 19:24
 **/
public class SomeService2 implements SomeService {
    public void doSomething() {
        System.out.println("我是实现2");
    }
}
