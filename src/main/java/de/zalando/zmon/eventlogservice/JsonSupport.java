package de.zalando.zmon.eventlogservice;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility to reduce boilerplate code when reading from an to Json with Jackson.
 * 
 * @author jbellmann
 *
 */
public interface JsonSupport {

    /**
     * Default {@link ObjectMapper} used. For custom - {@link ObjectMapper}
     * override the default {@link JsonSupport#getObjectMapper()} in
     * implementing class.
     */
    static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 
     * @param object
     *            to writeAsString
     * @return {@link Optional#empty()} when argument is null,
     *         {@link Optional#empty()} when a {@link JsonProcessingException}
     *         is thrown, {@link Optional<String>} when value was written by
     *         {@link ObjectMapper}.
     */
    default Optional<String> writeAsString(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(getObjectMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            getLogger().ifPresent(logger -> logger.warn("Failed to serialize map", e));
            return Optional.empty();
        }
    }

    /**
     * 
     * @param value
     * @param typeRef
     * @return Optional<T> according to the {@link TypeReference}-type,
     *         {@link Optional#empty()} on any exception thrown during
     *         {@link ObjectMapper#readValue(value, type)}
     */
    default <T> Optional<T> readValue(String value, TypeReference<T> typeRef) {
        try {
            return Optional.ofNullable(getObjectMapper().readValue(value, typeRef));
        } catch (JsonParseException e) {
            getLogger().ifPresent(logger -> logger.warn(e.getMessage(), e));
        } catch (JsonMappingException e) {
            getLogger().ifPresent(logger -> logger.warn(e.getMessage(), e));
        } catch (IOException e) {
            getLogger().ifPresent(logger -> logger.warn(e.getMessage(), e));
        }
        return Optional.empty();
    }

    default ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    default Optional<Logger> getLogger() {
        return Optional.empty();
    }

}
