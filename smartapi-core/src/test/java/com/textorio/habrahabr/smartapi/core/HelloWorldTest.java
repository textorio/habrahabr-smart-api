package com.textorio.habrahabr.smartapi.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldTest {
    @Test
    void helloWorldTest() {
        assertEquals(Habra.MSG_HELLO_WORLD, new Habra().helloWorld());
    }
}
