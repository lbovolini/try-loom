package com.github.lbovolini.tryloom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoLocalization(
        String country,
        String region,
        String city,
        String latitude,
        String longitude
) implements Localization {}
