package com.github.avenderov.json;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class StackTrace {

    private StackTrace() {
    }

    public static List<StackTraceElement> get() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
            .skip(2) // Thread.getStackTrace and this method
            .collect(Collectors.toList());
    }
}
