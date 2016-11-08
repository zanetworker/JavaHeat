package com.javaheat.client;

/**
 * Created by nle5220 on 29.10.2016.
 */
public class Wrapper <T> {
    T o;

    void set(T o) {
        this.o = o;
    }

    T get() {
        return this.o;
    }
    public static void main (String [] args) {

        Wrapper <String> test= new Wrapper<>();
        test.set("adel");
        System.out.println(test.get());
    }
}
