package br.com.techchallenge.mecanica.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRoutesIntegrationTest {

    private static final String JWT_ISSUER = "mecanica-auth";
    private static final KeyPair JWT_KEY_PAIR = generateKeyPair();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key", SecurityRoutesIntegrationTest::publicKeyPem);
        registry.add("app.jwt.issuer", () -> JWT_ISSUER);
    }





    @Test
    void estoqueNaoDeveSerExpostoPelaOs() throws Exception {
        mockMvc.perform(get("/estoques"))
                .andExpect(status().isForbidden());

        String token = issueToken();
        mockMvc.perform(get("/estoques").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }





    @Test
    void rotasRespeitamResponsabilidadeDosServicos() {
        var mappings=handlerMapping.getHandlerMethods().keySet();
        for(var path:java.util.List.of("/pecas","/servicos","/estoques")) {
            assertFalse(temRota(mappings,path,RequestMethod.GET));
            assertFalse(temRota(mappings,path,RequestMethod.POST));
        }
        for(var suffix:java.util.List.of("iniciar-diagnostico","aguardar-aprovacao","aprovar","recusar","finalizar","pecas","servicos"))
            assertFalse(temRota(mappings,"/ordens-servico/{id}/"+suffix,RequestMethod.POST));
        assertTrue(temRota(mappings,"/ordens-servico/{id}/historico",RequestMethod.GET));
        assertTrue(temRota(mappings,"/ordens-servico/{id}/entregar",RequestMethod.POST));
    }

    @Test
    void historicoExigeAutenticacaoEValidaCursor() throws Exception {
        String path="/ordens-servico/"+UUID.randomUUID()+"/historico";
        mockMvc.perform(get(path)).andExpect(status().isForbidden());
        mockMvc.perform(get(path).header("Authorization","Bearer "+issueToken())).andExpect(status().isNotFound());
        mockMvc.perform(get(path+"?apos=-1").header("Authorization","Bearer "+issueToken())).andExpect(status().isUnprocessableEntity());
    }
    @Test
    void clienteNaoPodeAdministrarCadastrosHistoricoOuEntrega() throws Exception {
        String client = "Bearer " + issueToken("CLIENTE");
        for (String path : java.util.List.of("/clientes", "/veiculos", "/operadores", "/ordens-servico",
                "/ordens-servico/"+UUID.randomUUID()+"/historico", "/ordens-servico/"+UUID.randomUUID()+"/reparo")) {
            mockMvc.perform(get(path).header("Authorization", client)).andExpect(status().isForbidden());
        }
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                "/ordens-servico/"+UUID.randomUUID()+"/entregar").header("Authorization", client))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/ordens-servico").header("Authorization", "Bearer "+issueToken("OPERADOR")))
            .andExpect(status().isOk());
        mockMvc.perform(get("/ordens-servico").header("Authorization", "Bearer "+issueToken("ADMINISTRADOR")))
            .andExpect(status().isOk());
        mockMvc.perform(get("/ordens-servico/acompanhamento/"+UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
    private static String issueToken() { return issueToken("OPERADOR"); }

    private static String issueToken(String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(900)))
                .claim("role", role)
                .claim("documentType", "CPF")
                .signWith(JWT_KEY_PAIR.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate test key", exception);
        }
    }

    private static String publicKeyPem() {
        String encoded = Base64.getMimeEncoder(64, new byte[] {'\n'})
                .encodeToString(JWT_KEY_PAIR.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";
    }

    private boolean temRota(
            java.util.Set<org.springframework.web.servlet.mvc.method.RequestMappingInfo> mappings,
            String caminho,
            RequestMethod metodo) {
        return mappings.stream().anyMatch(mapping ->
                mapping.getPatternValues().contains(caminho)
                        && mapping.getMethodsCondition().getMethods().contains(metodo));
    }
}
