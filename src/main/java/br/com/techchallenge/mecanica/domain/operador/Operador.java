package br.com.techchallenge.mecanica.domain.operador;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Operador {

    private UUID id;
    private String nome;
    private Integer matricula;
    private String email;
    private String contato;
    private String cargo;
    private boolean ativo;
    private LocalDateTime dataInativacao;

    public Operador(UUID id, String nome, Integer matricula, String email,
            String contato, String cargo, boolean ativo, LocalDateTime dataInativacao) {
        validarNome(nome);
        validarEmail(email);
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.contato = contato;
        this.cargo = cargo;
        this.ativo = ativo;
        this.dataInativacao = dataInativacao;
    }

    public Operador(
            String nome,
            Integer matricula,
            String email,
            String contato,
            String cargo) {

        validarNome(nome);
        validarEmail(email);

        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.contato = contato;
        this.cargo = cargo;
        this.ativo = true;
        this.dataInativacao = null;

    }

    public void atualizarDados(
            String nome,
            String email,
            String contato,
            String cargo) {

        validarNome(nome);
        validarEmail(email);

        this.nome = nome;
        this.email = email;
        this.contato = contato;
        this.cargo = cargo;
    }

    public void inativar() {
        if (!Boolean.TRUE.equals(this.ativo) && dataInativacao != null) {
            throw new RegraNegocioException(
                    "Operador já está inativo");
        }
        this.ativo = false;
        this.dataInativacao = LocalDateTime.now();
    }

    private void validarNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }
    }

    private void validarEmail(String email) {

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Operador other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
