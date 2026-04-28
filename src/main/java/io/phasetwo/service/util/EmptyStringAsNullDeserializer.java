package io.phasetwo.service.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public class EmptyStringAsNullDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return Optional
                .ofNullable(p.getValueAsString())
                .filter(Predicate.not(String::isEmpty))
                .orElse(null);
    }
}
