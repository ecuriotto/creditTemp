package com.company.scenario.util

import java.util.concurrent.TimeoutException

class Condition {

    private Closure code

    int timeout = 0;

    static Condition loop( Closure code ) {
        new Condition(code:code)
    }

    void until( Closure test ) {
        if(timeout > 30000) {
            throw new TimeoutException("Condition cannot be evaluated to true before the timeout expires.")
        }
        Thread.sleep(100)
        code()
        while (!test()) {
            Thread.sleep(100)
            timeout += 100
            code()
        }
    }
}