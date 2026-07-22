package com.brotherhood.scipubtts.common.openalex.logging;

import java.util.UUID;
import java.util.concurrent.Callable;

public final class OpenAlexCallContext {

    private static final ThreadLocal<OpenAlexCallContextSnapshot> CURRENT = new ThreadLocal<>();

    private OpenAlexCallContext() {
    }

    public static OpenAlexCallContextSnapshot current() {
        return CURRENT.get();
    }

    public static OpenAlexCallContextSnapshot capture() {
        return CURRENT.get();
    }

    public static void runAsSystemJob(UUID jobId, String jobType, Runnable runnable) {
        runWithContext(new OpenAlexCallContextSnapshot(jobId, jobType, true), runnable);
    }

    public static void runWithContext(OpenAlexCallContextSnapshot snapshot, Runnable runnable) {
        OpenAlexCallContextSnapshot previous = CURRENT.get();
        try {
            setOrClear(snapshot);
            runnable.run();
        } finally {
            setOrClear(previous);
        }
    }

    public static <T> T callWithContext(OpenAlexCallContextSnapshot snapshot, Callable<T> callable) throws Exception {
        OpenAlexCallContextSnapshot previous = CURRENT.get();
        try {
            setOrClear(snapshot);
            return callable.call();
        } finally {
            setOrClear(previous);
        }
    }

    private static void setOrClear(OpenAlexCallContextSnapshot snapshot) {
        if (snapshot == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(snapshot);
        }
    }
}
