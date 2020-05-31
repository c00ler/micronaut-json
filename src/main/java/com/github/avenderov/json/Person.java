package com.github.avenderov.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Introspected(excludes = "stackTraces")
public class Person {

    private static final int DEFAULT_AGE = 18;

    private final List<List<StackTraceElement>> stackTraces = new LinkedList<>();

    private final String name;
    private final int age;

    public Person(String name) {
        this(name, DEFAULT_AGE);
    }

    @JsonCreator
    public Person(@JsonProperty("name") String name,
                  @JsonProperty("age") int age) {
        stackTraces.add(StackTrace.get());

        this.name = name;
        this.age = age;
    }

    public String getName() {
        stackTraces.add(StackTrace.get());

        return name;
    }

    public int getAge() {
        stackTraces.add(StackTrace.get());

        return age;
    }

    @JsonIgnore
    public List<List<StackTraceElement>> getStackTraces() {
        return List.copyOf(stackTraces);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age &&
            Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
