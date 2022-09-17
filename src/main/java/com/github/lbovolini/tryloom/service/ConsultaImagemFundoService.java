package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.BackgroundComponent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ConsultaImagemFundoService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ConsultaImagemFundoService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    BackgroundComponent executa() {

        System.out.println("Início da consulta de imagem de fundo na Thread: %s".formatted(Thread.currentThread()));

        try {
            var request = HttpRequest.newBuilder(URI.create("https://coffee.alexflipnote.dev/random.json"))
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Fim da consulta de imagem de fundo na Thread: %s".formatted(Thread.currentThread()));

            var backgroundJson = response.body();
            if (backgroundJson.isBlank()) {
                throw new RuntimeException("Falha ao consultar imagem de fundo na Thread: %s".formatted(Thread.currentThread()));
            }

            return objectMapper.readValue(backgroundJson, BackgroundComponent.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
