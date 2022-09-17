package com.github.lbovolini.tryloom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FactComponent(String text) implements DataComponent {
}
