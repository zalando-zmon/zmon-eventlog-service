package de.zalando.zmon.eventlogservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public class JsonSupportTest {

    private final String example_json = "{\"key\":\"value\"}";

    @Test
    public void writeAsString() {
        Map<String, String> toMap = new HashMap<>();
        toMap.put("key", "value");
        Optional<String> result = new Example().writeAsString(toMap);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isTrue();
        result.ifPresent(mapped -> System.out.println(mapped));
    }

    @Test
    public void multiEntries() {
        Map<String, String> toMap = new HashMap<>();
        toMap.put("key", "value");
        toMap.put("key2", "value2");
        Optional<String> result = new Example().writeAsString(toMap);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isTrue();
        result.ifPresent(mapped -> System.out.println(mapped));
    }

    @Test
    public void emptyMap() {
        Map<String, String> toMap = new HashMap<>();
        Optional<String> result = new Example().writeAsString(toMap);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isTrue();
        result.ifPresent(mapped -> System.out.println(mapped));
    }

    /**
     * @see {@link JsonSupport#writeAsString(Object)}, should return
     *      {@link Optional#empty()}
     */
    @Test
    public void writeNullAsString() {
        Optional<String> result = new Example().writeAsString(null);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isFalse();
        result.ifPresent(mapped -> System.out.println(mapped));
    }

    @Test
    public void writeEmptyAsString() {
        Optional<String> result = new Example().writeAsString("");
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isTrue();
        result.ifPresent(mapped -> System.out.println(mapped));
    }

    @Test
    public void read() {
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };
        Optional<Map<String, String>> result = new Example().readValue(example_json, typeRef);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().get("key")).isEqualTo("value");
    }

    /**
     * Example needs some code to write and read. This is a typical how to use.
     * 
     * @author jbellmann
     *
     */
    class Example implements JsonSupport {

        private final Logger LOG = LoggerFactory.getLogger(Example.class);

        @Override
        public Optional<Logger> getLogger() {
            return Optional.ofNullable(LOG);
        }

    }
}
