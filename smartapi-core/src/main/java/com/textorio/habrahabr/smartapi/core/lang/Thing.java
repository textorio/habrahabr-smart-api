package com.textorio.habrahabr.smartapi.core.lang;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Thing<T, E extends Exception> {
    Logger logger = LoggerFactory.getLogger(Thing.class);

    private Optional<T> value;
    private Optional<E> error;
    private String explanation; //human readable!

    private static final Thing<?,?> EMPTY = new Thing<>();

    public static Thing empty() {
        @SuppressWarnings("unchecked")
        Thing<Optional,Exception> t = (Thing<Optional, Exception>) EMPTY;
        return t;
    }

    public static <T> Thing<T,Exception> of(T value) {
        return new Thing<>(value);
    }
    public static <T> Thing<T,Exception> of(T value, String explanation) {
        return new Thing<>(value, explanation);
    }
    public static <T,E extends Exception> Thing<T,E> ofError(E error, String explanation) {
        return new Thing<>(error, explanation);
    }
    public static <T,E extends Exception> Thing<T,E> ofInvalidValue(T value, E error, String explanation) {
        return new Thing<>(value, error, explanation);
    }
    public static <T> Thing<T,Err> ofError(String explanation) {
        return new Thing<>(new Err(explanation), explanation);
    }

    public Thing() {
        this.value = Optional.empty();
        this.error = Optional.empty();
        this.explanation = "Empty value";
    }

    public Thing(T value) {
        this.value = Optional.of(value);
        this.error = Optional.empty();
        this.explanation = "";
    }

    public Thing(T value, String explanation) {
        this.value = Optional.of(value);
        this.error = Optional.empty();
        this.explanation = explanation;
    }

    public Thing(E error, String explanation) {
        this.value = Optional.empty();
        this.error = Optional.of(error);
        this.explanation = explanation;
    }

    public Thing(T value, E error, String explanation) {
        this.value = Optional.of(value);
        this.error = Optional.of(error);
        this.explanation = explanation;
    }

    public T get() {
        if (optionallyAbsent(value)) {
            throw new NoSuchElementException("No value present");
        }
        return value.get();
    }


    @Contract(pure = true)
    private boolean optionallyAbsent(Optional target) {
        return target == null || !target.isPresent();
    }

    @Contract(pure = true)
    private boolean optionallyPresent(Optional target) {
        return target != null && target.isPresent();
    }

    public boolean isValid() {
        return optionallyPresent(value) && optionallyAbsent(error);
    }

    public boolean isInvalid() {
        return optionallyAbsent(value) || optionallyPresent(error);
    }

    public void ifValid(Consumer<? super T> action) {
        if (isValid()) {
            action.accept(value.get());
        }
    }

    public void ifInvalid(Consumer<? super E> action) {
        if (isInvalid()) {
            action.accept(error.get());
        }
    }

    public void ifValidOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (isValid()) {
            action.accept(value.get());
        } else {
            emptyAction.run();
        }
    }

    public String fullErrorString() {
        String result;
        if (error.isPresent()) {
            String message = ExceptionUtils.getMessage(error.get());
            String stackTrace = ExceptionUtils.getStackTrace(error.get());
            result = String.format("%s |\n exception: [%s] |\n with message [%s] |\n stack trace is:\n %s", explanation, error.get().getClass().getName(), message, stackTrace);
        } else {
            result = String.format("%s", explanation);
        }
        return result;
    }

    public Thing<T,E> logIfInvalid() {
        if (isInvalid()) {
            logger.error(String.format("!!! %s",fullErrorString()));
        }
        return this;
    }

    public Thing<T,E> crashIfInvalid(String explanation) {
        if (isInvalid()) {
            logger.error(String.format("!!! PANIC. %s |\n %s", explanation, fullErrorString()));
            System.exit(-1);
        }
        return this;
    }

    public Thing<T,E> raiseIfInvalid(String explanation) {
        if (isInvalid()) {
            String panicMessage = String.format("!!! PANIC. %s |\n %s", explanation, fullErrorString());
            logger.error(panicMessage);
            if (error.isPresent()) {
                throw new RuntimeException(panicMessage, error.get());
            } else {
                throw new RuntimeException(panicMessage);
            }
        }
        return this;
    }

    public <U> U extract(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isValid()) {
            return null;
        } else {
            return mapper.apply(value.get());
        }
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isValid()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(value.get()));
        }
    }

}
