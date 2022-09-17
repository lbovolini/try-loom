package com.github.lbovolini.tryloom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lbovolini.tryloom.service.ConsultaLocalizacaoService;
import com.github.lbovolini.tryloom.service.ConsultaPaginaPrincipalService;

public class TryLoom {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static void main(String[] args) throws Exception {

        var start = System.nanoTime();

        var localizacao = ConsultaLocalizacaoService.executa();
        var paginaPrincipal = ConsultaPaginaPrincipalService.executa(localizacao);

        var paginaPrincipalJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(paginaPrincipal);

        System.out.println(paginaPrincipalJson);

        System.out.println("Tempo total de execução: " + (System.nanoTime() - start) / 1_000_000);
    }
}