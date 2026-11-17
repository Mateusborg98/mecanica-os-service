package br.com.techchallenge.mecanica.domain.cliente;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Cliente {

    private UUID id;
    private String nome;
    private CpfCnpj cpfCnpj;
    private String contato;
    private String email;
    private boolean ativo;
    private LocalDateTime dataInativacao;

    public Cliente(UUID id, String nome, CpfCnpj cpfCnpj, String contato, String email, boolean ativo,
            LocalDateTime dataInativacao) {

        validarNome(nome);
        validarEmail(email);

        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.contato = contato;
        this.email = email;
        this.ativo = ativo;
        this.dataInativacao = dataInativacao;
    }

    public Cliente(
            String nome,
            CpfCnpj cpfCnpj,
            String contato,
            String email) {

        validarNome(nome);
        validarEmail(email);

        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.contato = contato;
        this.email = email;
        this.ativo = true;
        this.dataInativacao = null;
    }

    public void atualizarDados(
            String nome,
            String contato,
            String email) {

        validarNome(nome);
        validarEmail(email);

        this.nome = nome;
        this.contato = contato;
        this.email = email;
    }

    public void inativar() {
        if (!ativo) {
            throw new RegraNegocioException(
                    "Cliente já está inativo");
        }
        this.ativo = false;
        this.dataInativacao = LocalDateTime.now();
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome obrigatório");
        }
    }

    private void validarEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new RegraNegocioException("Email inválido");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Cliente other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}