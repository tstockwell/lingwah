package com.googlecode.lingwah.test;

public class MyClass {
    private int i = 1;
    public void test() {
        if (5 > 2) {
            i = 1 + i++ - 5;
        } 
    }
    @Override
    public String toString() {
        return super.toString().substring(0);
    }
}