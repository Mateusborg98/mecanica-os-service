package br.com.techchallenge.mecanica;

import java.security.KeyPairGenerator;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class MecanicaApplicationTests {

	private static final String PUBLIC_KEY_PEM = generatePublicKeyPem();

	@DynamicPropertySource
	static void jwtProperties(DynamicPropertyRegistry registry) {
		registry.add("app.jwt.public-key", () -> PUBLIC_KEY_PEM);
	}

	@Test
	void contextLoads() {
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
