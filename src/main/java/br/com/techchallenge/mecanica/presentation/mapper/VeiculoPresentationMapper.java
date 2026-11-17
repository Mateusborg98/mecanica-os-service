package br.com.techchallenge.mecanica.presentation.mapper;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.application.dto.veiculo.AtualizarVeiculoInput;
import br.com.techchallenge.mecanica.application.dto.veiculo.CriarVeiculoInput;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.AtualizarVeiculoRequest;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.CriarVeiculoRequest;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.VeiculoResponse;

@Component
public class VeiculoPresentationMapper {

    ClientePresentationMapper clientePresentationMapper = new ClientePresentationMapper();

    public VeiculoResponse toResponse(Veiculo veiculo, Cliente cliente) {

        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca().getValor(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.isAtivo(),
                clientePresentationMapper.toResumo(cliente));
    }

    public CriarVeiculoInput toCriarVeiculoInput(CriarVeiculoRequest request) {
        return new CriarVeiculoInput(
                request.placa(),
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cpfCnpj());
    }

    public AtualizarVeiculoInput toAtualizarVeiculoInput(AtualizarVeiculoRequest request) {

        return new AtualizarVeiculoInput(
                request.placa(),
                request.marca(),
                request.modelo(),
                request.ano());
    }

}
