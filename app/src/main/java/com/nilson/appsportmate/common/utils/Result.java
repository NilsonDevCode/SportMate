package com.nilson.appsportmate.common.utils;

import java.util.function.Consumer;

public abstract class Result<T> {
    private Result() {}

    public static final class Success<T> extends Result<T> {
        public final T data;

        public Success(T data) {
            this.data = data;
        }
    }

    public static final class Error<T> extends Result<T> {
        public final Exception exception;

        public Error(Exception exception) {
            this.exception = exception;
        }
    }

    public static <T> T handleResult(Result<T> result, Consumer<Exception> onError) {

        if (result instanceof Result.Error<T> error) {
            onError.accept(error.exception);
            return null;
        }

        return ((Result.Success<T>) result).data;
    }
}
