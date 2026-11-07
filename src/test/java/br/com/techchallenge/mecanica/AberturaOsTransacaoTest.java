package br.com.techchallenge.mecanica;

import java.security.KeyPairGenerator;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:abertura;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.show-sql=false","app.messaging.enabled=false"})
@ActiveProfiles("test")
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
class AberturaOsTransacaoTest {

	private static final String PUBLIC_KEY_PEM = generatePublicKeyPem();

	@DynamicPropertySource
	static void jwtProperties(DynamicPropertyRegistry registry) {
		registry.add("app.jwt.public-key", () -> PUBLIC_KEY_PEM);
	}

    @org.springframework.beans.factory.annotation.Autowired
    br.com.techchallenge.mecanica.application.usecase.ordemdeservico.CriarOrdemDeServicoUseCase useCase;
    @org.springframework.beans.factory.annotation.Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @org.springframework.beans.factory.annotation.Autowired
    br.com.techchallenge.mecanica.application.gateway.SolicitarReparoGateway solicitacoes;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    br.com.techchallenge.mecanica.application.usecase.ordemdeservico.NotificarAlteracaoStatusOrdemUseCase notificacao;
    @org.junit.jupiter.api.BeforeAll void prepararBanco() throws Exception {
        new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(
            new org.springframework.core.io.ClassPathResource("db/migration/V7__historico_os.sql"),new org.springframework.core.io.ClassPathResource("db/migration/V4__solicitacoes_reparo.sql"))
            .execute(jdbc.getDataSource());
        jdbc.update("INSERT INTO cliente_jpa_entity(id,nome,cpf_cnpj,contato,email,ativo) VALUES (?,?,?,?,?,?)",
            java.util.UUID.fromString("10000000-0000-0000-0000-000000000001"),"Cliente teste","52998224725","11999999999","teste@example.com",true);
        jdbc.update("INSERT INTO veiculo_jpa_entity(id,placa,marca,modelo,ano,ativo,cliente_jpa_entity_id) VALUES (?,?,?,?,?,?,?)",
            java.util.UUID.fromString("20000000-0000-0000-0000-000000000001"),"ABC1D23","Marca","Modelo",2020,true,
            java.util.UUID.fromString("10000000-0000-0000-0000-000000000001"));
        jdbc.update("INSERT INTO operador_jpa_entity(id,nome,matricula,cargo,email,contato,ativo) VALUES (?,?,?,?,?,?,?)",
            java.util.UUID.fromString("30000000-0000-0000-0000-000000000001"),"Operador teste",1,"ADMINISTRADOR","operador@example.com","11999999999",true);
    }
    private br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput input() {
        return new br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput(
            java.util.UUID.fromString("10000000-0000-0000-0000-000000000001"),
            java.util.UUID.fromString("20000000-0000-0000-0000-000000000001"),
            java.util.UUID.fromString("30000000-0000-0000-0000-000000000001"));
    }
    @Test void aberturaPersisteOsEOutboxNaMesmaTransacao() {
        var ordem=useCase.executar(input());
        org.junit.jupiter.api.Assertions.assertNotNull(solicitacoes.buscar(ordem.getId()).reparoId());
        org.junit.jupiter.api.Assertions.assertFalse(solicitacoes.buscar(ordem.getId()).publicado());
    }
    @Test void falhaPosteriorReverteJpaEOutboxJdbc() {
        var ordens=jdbc.queryForObject("SELECT COUNT(*) FROM ordem_de_servico_jpa_entity",Integer.class);
        var eventos=jdbc.queryForObject("SELECT COUNT(*) FROM solicitacoes_reparo",Integer.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("falha simulada")).when(notificacao).executar(org.mockito.ArgumentMatchers.any());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,()->useCase.executar(input()));
        org.junit.jupiter.api.Assertions.assertEquals(ordens,jdbc.queryForObject("SELECT COUNT(*) FROM ordem_de_servico_jpa_entity",Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals(eventos,jdbc.queryForObject("SELECT COUNT(*) FROM solicitacoes_reparo",Integer.class));
    }

	private static String generatePublicKeyPem() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			String encoded = Base64.getMimeEncoder(64, new byte[] {'\n'})
					.encodeToString(generator.generateKeyPair().getPublic().getEncoded());
			return "-----BEGIN PUBLIC KEY-----\n"
					+ encoded
					+ "\n-----END PUBLIC KEY-----";
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to generate test key", exception);
		}
	}

}
