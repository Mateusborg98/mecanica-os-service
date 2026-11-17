package br.com.techchallenge.mecanica.infrastructure.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    public UUID getClienteIdParaAbertura(UUID proprietarioVeiculo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(authority ->
                    "ROLE_OPERADOR".equals(authority.getAuthority())
                    || "ROLE_ADMINISTRADOR".equals(authority.getAuthority()))) {
            return proprietarioVeiculo;
        }
        return getClienteId();
    }
    public UUID getClienteId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        return UUID.fromString(authentication.getName());
    }
}
