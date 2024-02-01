package io.phasetwo.service.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface WithAttributes {
  Map<String, List<String>> getAttributes();

  default Stream<String> getAttributesStream(String name) {
    List<String> attrs = getAttributes().get(name);
    if (attrs != null && attrs.size() > 0) {
      return attrs.stream();
    } else {
      return Stream.empty();
    }
  }

  default String getFirstAttribute(String name) {
    List<String> attrs = getAttributes().get(name);
    if (attrs != null && attrs.size() > 0) {
      return attrs.get(0);
    } else {
      return null;
    }
  }

  void removeAttributes();

  void removeAttribute(String name);

  void setAttribute(String name, List<String> values);

  default void setSingleAttribute(String name, String value) {
    setAttribute(name, ImmutableList.of(value));
  }
}
