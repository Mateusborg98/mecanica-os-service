package br.com.techchallenge.mecanica.application.dto.veiculo;

public record AtualizarVeiculoInput(
        String placa,
        String marca,
        String modelo,
        Integer ano) {
}
