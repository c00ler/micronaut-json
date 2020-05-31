package com.github.avenderov.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.jackson.modules.BeanIntrospectionModule;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

    private static final String TEST_CLASS_NAME = PersonTest.class.getName();
    private static final String REFLECT_PACKAGE = "reflect";

    @Test
    void should_instantiate_via_introspection() {
        // given
        var expectedName = "Bob";
        var expectedAge = 25;
        var introspection = BeanIntrospection.getIntrospection(Person.class);

        // when
        var person = introspection.instantiate(expectedName, expectedAge);

        // then
        assertThat(person.getName()).isEqualTo(expectedName);
        assertThat(person.getAge()).isEqualTo(expectedAge);

        // and
        var stackTraces = trimmedConstructorStackTrace(person);
        assertThat(stackTraces).noneMatch(e -> e.getClassName().contains(REFLECT_PACKAGE));
    }

    @Nested
    class MicronautMapper {

        ObjectMapper micronautMapper = new ObjectMapper().registerModule(new BeanIntrospectionModule());

        @Test
        void should_deserialize_without_reflection() throws Exception {
            // given
            var json = "{\"name\":\"Bob\", \"age\":25}";

            // when
            var person = micronautMapper.readValue(json, Person.class);

            // then
            assertThat(person.getName()).isEqualTo("Bob");
            assertThat(person.getAge()).isEqualTo(25);

            // and
            var stackTraces = trimmedConstructorStackTrace(person);
            assertThat(stackTraces).noneMatch(e -> e.getClassName().contains(REFLECT_PACKAGE));
        }

        @Test
        void should_serialize_without_reflection() throws Exception {
            // given
            var person = new Person("Bob");

            // when
            var json = micronautMapper.writeValueAsString(person);

            // then
            assertThatJson(json).isEqualTo("{name:'Bob', age:18}");

            // and
            var stackTraces = trimmedStackTrace(person, "getName");
            assertThat(stackTraces).noneMatch(e -> e.getClassName().contains(REFLECT_PACKAGE));
        }
    }

    @Nested
    class DefaultMapper {

        ObjectMapper defaultMapper = new ObjectMapper();

        @Test
        void should_deserialize_with_reflection() throws Exception {
            // given
            var json = "{\"name\":\"Bob\", \"age\":25}";

            // when
            var person = defaultMapper.readValue(json, Person.class);

            // then
            assertThat(person.getName()).isEqualTo("Bob");
            assertThat(person.getAge()).isEqualTo(25);

            // and
            var stackTraces = trimmedConstructorStackTrace(person);
            assertThat(stackTraces).anyMatch(e -> e.getClassName().contains(REFLECT_PACKAGE));
        }

        @Test
        void should_serialize_with_reflection() throws Exception {
            // given
            var person = new Person("Bob");

            // when
            var json = defaultMapper.writeValueAsString(person);

            // then
            assertThatJson(json).isEqualTo("{name:'Bob', age:18}");

            // and
            var stackTraces = trimmedStackTrace(person, "getAge");
            assertThat(stackTraces).anyMatch(e -> e.getClassName().contains(REFLECT_PACKAGE));
        }
    }

    private static List<StackTraceElement> trimmedConstructorStackTrace(Person person) {
        return trimmedStackTrace(person, "<init>");
    }

    private static List<StackTraceElement> trimmedStackTrace(Person person, String methodName) {
        return StreamEx.of(person.getStackTraces())
            .filter(stackTrace ->
                StreamEx.of(stackTrace)
                    .findFirst(element ->
                        "com.github.avenderov.json.Person".equals(element.getClassName())
                            && methodName.equals(element.getMethodName()))
                    .isPresent())
            .findFirst()
            .map(PersonTest::trimStackTrace)
            .orElseThrow();
    }

    private static List<StackTraceElement> trimStackTrace(List<StackTraceElement> stackTrace) {
        return StreamEx.of(stackTrace).takeWhile(e -> !e.getClassName().startsWith(TEST_CLASS_NAME)).toList();
    }
}
