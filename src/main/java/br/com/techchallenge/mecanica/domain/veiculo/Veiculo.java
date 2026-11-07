package br.com.techchallenge.mecanica.domain.veiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Veiculo {

    private UUID id;
    private Placa placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private UUID clienteId;
    private boolean ativo;
    private LocalDateTime dataInativacao;

    public Veiculo(
            UUID id,
            Placa placa,
            String marca,
            String modelo,
            Integer ano,
            UUID clienteId) {

        this(id, placa, marca, modelo, ano, clienteId, true, null);
    }

    public Veiculo(UUID id, Placa placa, String marca, String modelo,
            Integer ano, UUID clienteId, boolean ativo, LocalDateTime dataInativacao) {

        validarMarca(marca);
        validarModelo(modelo);
        validarAno(ano);

        if (clienteId == null) {
            throw new RegraNegocioException(
                    "Cliente obrigatório");
        }

        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.clienteId = clienteId;
        this.ativo = ativo;
        this.dataInativacao = dataInativacao;
    }

    public Veiculo(
            Placa placa,
            String marca,
            String modelo,
            Integer ano,
            UUID clienteId) {

        validarMarca(marca);
        validarModelo(modelo);
        validarAno(ano);

        if (clienteId == null) {
            throw new RegraNegocioException(
                    "Cliente obrigatório");
        }

        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.clienteId = clienteId;
        this.ativo = true;
        this.dataInativacao = null;
    }

    public void atualizarDados(
            Placa placa,
            String marca,
            String modelo,
            Integer ano) {

        validarMarca(marca);
        validarModelo(modelo);
        validarAno(ano);

        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
    }

    public void alterarCliente(UUID clienteId) {

        if (clienteId == null) {
            throw new RegraNegocioException(
                    "Cliente obrigatório");
        }

        this.clienteId = clienteId;
    }

    public void inativar() {
        if (!ativo) {
            throw new RegraNegocioException(
                    "Veículo já está inativo");
        }
        this.ativo = false;
        this.dataInativacao = LocalDateTime.now();
    }

    private void validarMarca(String marca) {

        if (marca == null || marca.isBlank()) {
            throw new RegraNegocioException(
                    "Marca obrigatória");
        }
    }

    private void validarModelo(String modelo) {

        if (modelo == null || modelo.isBlank()) {
            throw new RegraNegocioException(
                    "Modelo obrigatório");
        }
    }

    private void validarAno(Integer ano) {

        if (ano == null
                || ano < 1900
                || ano > LocalDate.now().getYear() + 1) {

            throw new RegraNegocioException(
                    "Ano inválido");
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Veiculo other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
