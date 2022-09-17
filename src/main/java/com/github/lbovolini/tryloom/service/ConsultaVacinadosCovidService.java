package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.CovidVaccinationComponent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

class ConsultaVacinadosCovidService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ConsultaVacinadosCovidService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    // https://github.com/M-Media-Group/Covid-19-API
    CovidVaccinationComponent executa(String pais) {

        System.out.println("Início da consulta de estatísticas de vacinação no país: %s na Thread: %s".formatted(pais, Thread.currentThread()));

        var request = HttpRequest.newBuilder(URI.create("https://covid-api.mmediagroup.fr/v1/vaccines?country=%s".formatted(pais))).build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Fim da consulta de estatísticas de vacinação no país: %s na Thread: %s".formatted(pais, Thread.currentThread()));

            var geoLocalizacaoJson = response.body();
            if (geoLocalizacaoJson.isBlank()) {
                throw new RuntimeException("Falha ao consultar estatísticas de vacinação no país: %s na Thread: %s".formatted(pais, Thread.currentThread()));
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            record VacinacaoCovid(
                    @JsonProperty("people_vaccinated")
                    int pessoasVacinadas,
                    @JsonProperty("people_partially_vaccinated")
                    int pessoasParcialmenteVacinadas,
                    @JsonProperty("population")
                    int populacao,
                    @JsonProperty("updated")
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ssx")
                    LocalDateTime atualizadoEm
            ) {}

            @JsonIgnoreProperties(ignoreUnknown = true)
            record ResponseWrapper(
                    @JsonProperty("All") VacinacaoCovid vacinacaoCovid
            ) {}

            var vacinacaoCovid = objectMapper.readValue(geoLocalizacaoJson, ResponseWrapper.class).vacinacaoCovid;

            return new CovidVaccinationComponent(
                    vacinacaoCovid.pessoasVacinadas,
                    vacinacaoCovid.pessoasParcialmenteVacinadas,
                    vacinacaoCovid.populacao,
                    vacinacaoCovid.atualizadoEm);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
