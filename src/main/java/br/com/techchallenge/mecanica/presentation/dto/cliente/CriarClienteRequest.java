package br.com.techchallenge.mecanica.presentation.dto.cliente;

import br.com.techchallenge.mecanica.presentation.annotation.CpfCnpjAnnotation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarClienteRequest(
        @NotBlank
        String nome,
        @NotBlank
        @CpfCnpjAnnotation
        String cpfCnpj,
        @NotBlank
        String contato,
        @Email
        String email
) {
}
