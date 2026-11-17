package br.com.techchallenge.mecanica.application.dto.veiculo;

public record CriarVeiculoInput(
        String placa,
        String marca,
        String modelo,
        Integer ano,
        String cpfCnpj) {
}
