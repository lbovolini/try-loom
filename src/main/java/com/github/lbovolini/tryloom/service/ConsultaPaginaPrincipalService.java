package com.github.lbovolini.tryloom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.dto.*;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Segundo passo: consulta 4 serviços ao mesmo tempo.
 * Consulta os serviços de: estatisticas de vacinação de Covid-19 no país,
 * previsão do tempo, um fato aleatório e uma imagem aleatória de café.
 * O tempo total de execução é dado pelo serviço com maior tempo de resposta.
 */
public class ConsultaPaginaPrincipalService {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final ConsultaVacinadosCovidService consultaVacinadosCovidService;
    private static final ConsultaPrevisaoTempoService consultaPrevisaoTempoService;
    private static final ConsultaFatosService consultaFatosService;
    private static final ConsultaImagemFundoService consultaImagemFundoService;

    static  {
        consultaVacinadosCovidService = new ConsultaVacinadosCovidService(httpClient, objectMapper);
        consultaPrevisaoTempoService = new ConsultaPrevisaoTempoService(httpClient, objectMapper);
        consultaFatosService = new ConsultaFatosService(httpClient, objectMapper);
        consultaImagemFundoService = new ConsultaImagemFundoService(httpClient, objectMapper);
    }

    public static DadosPagina executa(Localization localization) {

        CovidVaccinationComponent covidVaccinationComponent = null;
        WeatherComponent weatherComponent = null;
        FactComponent factComponent = null;
        BackgroundComponent backgroundComponent = null;

        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<DataComponent>> callableList = List.of(
                    () -> consultaVacinadosCovidService.executa(localization.country()),
                    () -> consultaPrevisaoTempoService.executa(localization.city()),
                    () -> consultaFatosService.executa(),
                    () -> consultaImagemFundoService.executa()
            );

            var futures = pool.invokeAll(callableList);

            for (var future : futures) {
                switch (future.get()) {
                    case CovidVaccinationComponent covidVaccination -> covidVaccinationComponent = covidVaccination;
                    case WeatherComponent weather -> weatherComponent = weather;
                    case FactComponent fact -> factComponent = fact;
                    case BackgroundComponent background -> backgroundComponent = background;
                }
            }

            return new DadosPagina(localization, covidVaccinationComponent, weatherComponent, factComponent, backgroundComponent);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
