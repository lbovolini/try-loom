package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.WeatherComponent;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

class ConsultaPrevisaoTempoService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ConsultaPrevisaoTempoService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    WeatherComponent executa(String cidade) {

        System.out.println("Início da consulta de previsão do tempo para a cidade: %s na Thread: %s".formatted(cidade, Thread.currentThread()));
        try {
            var uriString = "https://goweather.herokuapp.com/weather/%s"
                    .formatted(URLEncoder.encode(cidade, StandardCharsets.UTF_8.toString()));
            var request = HttpRequest.newBuilder(URI.create(uriString)).build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Fim da consulta de previsão do tempo para a cidade: %s na Thread: %s".formatted(cidade, Thread.currentThread()));

            var previsaoTempoJson = response.body();
            if (previsaoTempoJson.isBlank()) {
                throw new RuntimeException("Falha ao consultar previsão do tempo para a cidade: %s na Thread: %s".formatted(cidade, Thread.currentThread()));
            }

            return objectMapper.readValue(previsaoTempoJson, WeatherComponent.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
