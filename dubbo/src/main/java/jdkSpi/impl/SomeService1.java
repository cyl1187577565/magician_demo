package jdkSpi.impl;

import jdkSpi.SomeService;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2021-03-07 19:24
 **/
public class SomeService1 implements SomeService {
    public void doSomething() {
        System.out.println("我是实现1");
    }
    public String hello(){
        return "hello";
    }
}
