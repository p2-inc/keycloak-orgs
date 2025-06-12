package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public class OrganizationMemberAttribute {

    private @Valid Map<String, List<String>> attributes = Maps.newHashMap();

    @JsonProperty("attributes")
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }
}
