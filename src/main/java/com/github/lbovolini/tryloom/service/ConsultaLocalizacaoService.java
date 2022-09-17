package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.GeoLocalization;
import com.github.lbovolini.tryloom.dto.Localization;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Primeiro passo: Consultar a localização atual do usuário
 * Consulta em 4 serviços e usa o resultado da consulta que finalizar com sucesso primeiro,
 * sem que seja necessario aguardar pelas outras consultas.
 */
public class ConsultaLocalizacaoService {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static Localization executa() {
        try (var taskScope = new StructuredTaskScope.ShutdownOnSuccess<Localization>()) {
            taskScope.fork(ConsultaLocalizacaoService::consultaLocalizacaoGeoJs);
            taskScope.fork(ConsultaLocalizacaoService::consultaLocalizacaoIpGeo);
            taskScope.fork(ConsultaLocalizacaoService::consultaLocalizacaoIpApi);
            taskScope.fork(ConsultaLocalizacaoService::consultaLocalizacaoGeoPlugin);

            taskScope.join();

            return taskScope.result();
        } catch (Exception e) {
            System.out.println("Todas as consultas de localização falharam...");
            return new GeoLocalization("Unknown", "Unknown", "Unknown", "0", "0");
        }
    }

    private static Localization consultaLocalizacaoGeoJs() {
        URI uri = URI.create("https://get.geojs.io/v1/ip/geo.json");

        return consultaLocalizacao(uri, GeoLocalization.class);
    }

    // com redirecionamento
    private static Localization consultaLocalizacaoIpGeo() {

        URI uri = URI.create("https://api.techniknews.net/ipgeo/");

        @JsonIgnoreProperties(ignoreUnknown = true)
        record GeoLocalizationData(
                String city,
                @JsonProperty("regionName")
                String region,
                String country,
                @JsonProperty("lat")
                String latitude,
                @JsonProperty("lon")
                String longitude
        ) implements Localization {}

        return consultaLocalizacao(uri, GeoLocalizationData.class);
    }

    private static Localization consultaLocalizacaoIpApi() {

        URI uri = URI.create("https://ipapi.co/json/");

        @JsonIgnoreProperties(ignoreUnknown = true)
        record GeoLocalizationData(
                String city,
                String region,
                @JsonProperty("country_name")
                String country,
                String latitude,
                String longitude
        ) implements Localization {}

        return consultaLocalizacao(uri, GeoLocalizationData.class);

    }

    private static Localization consultaLocalizacaoGeoPlugin() {

        URI uri = URI.create("http://www.geoplugin.net/json.gp");

        @JsonIgnoreProperties(ignoreUnknown = true)
        record GeoLocalizationData(
                @JsonProperty("geoplugin_city")
                String city,
                @JsonProperty("geoplugin_region")
                String region,
                @JsonProperty("geoplugin_countryName")
                String country,
                @JsonProperty("geoplugin_latitude")
                String latitude,
                @JsonProperty("geoplugin_longitude")
                String longitude
        ) implements Localization {}

        return consultaLocalizacao(uri, GeoLocalizationData.class);
    }

    private static <T extends Localization> GeoLocalization consultaLocalizacao(URI uri, Class<T> localizationType) {

        System.out.println("Início da consulta de localização com o serviço: %s na Thread: %s".formatted(uri.toString(), Thread.currentThread()));

        var request = HttpRequest.newBuilder(uri).build();
        try {
            httpClient.followRedirects();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Fim da consulta de localização com o serviço: %s na Thread: %s".formatted(uri.toString(), Thread.currentThread()));

            var geoLocalizacaoJson = response.body();
            if (geoLocalizacaoJson.isBlank()) {
                throw new RuntimeException("Falha ao consultar localização na Thread: %s".formatted(Thread.currentThread()));
            }

            var geoLocalizacao = objectMapper.readValue(geoLocalizacaoJson, localizationType);

            return new GeoLocalization(
                    geoLocalizacao.country(),
                    geoLocalizacao.region(),
                    geoLocalizacao.city(),
                    geoLocalizacao.latitude(),
                    geoLocalizacao.longitude());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
