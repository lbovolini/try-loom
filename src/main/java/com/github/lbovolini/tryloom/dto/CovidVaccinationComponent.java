package com.github.lbovolini.tryloom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CovidVaccinationComponent(
        int peopleVaccinated,
        int peoplePartiallyVaccinated,
        int population,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) implements DataComponent {}
