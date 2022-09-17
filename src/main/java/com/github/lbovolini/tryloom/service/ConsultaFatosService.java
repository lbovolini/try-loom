package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.FactComponent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ConsultaFatosService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ConsultaFatosService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    FactComponent executa() {
        System.out.println("Início da consulta de fatos na Thread: %s".formatted(Thread.currentThread()));

        try {
            var request = HttpRequest.newBuilder(URI.create("https://uselessfacts.jsph.pl/random.json?language=en")).build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Fim da consulta de previsão do fatos na Thread: %s".formatted(Thread.currentThread()));

            var previsaoTempoJson = response.body();
            if (previsaoTempoJson.isBlank()) {
                throw new RuntimeException("Falha ao consultar fatos na Thread: %s".formatted(Thread.currentThread()));
            }

            return objectMapper.readValue(previsaoTempoJson, FactComponent.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
