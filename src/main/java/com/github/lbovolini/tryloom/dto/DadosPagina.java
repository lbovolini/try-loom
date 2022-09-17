package com.github.lbovolini.tryloom.dto;

public record DadosPagina(
        Localization localization,
        CovidVaccinationComponent covidVaccination,
        WeatherComponent weather,
        FactComponent fact,
        BackgroundComponent background
) {}
