package com.github.lbovolini.tryloom.dto;

public sealed interface DataComponent
        permits CovidVaccinationComponent, WeatherComponent, FactComponent, BackgroundComponent {
}
