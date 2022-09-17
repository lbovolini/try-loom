package com.github.lbovolini.tryloom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherComponent(
        String temperature,
        String wind,
        String description
) implements DataComponent {
}
