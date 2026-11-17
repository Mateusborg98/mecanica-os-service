package br.com.techchallenge.mecanica.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    @Test
    void shouldUseRelativeServerForApiGatewayCompatibility() {
        OpenAPI openAPI = new OpenApiConfig().customOpenAPI();

        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
        assertEquals("/", openAPI.getServers().getFirst().getUrl());
    }
}
