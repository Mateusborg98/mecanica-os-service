package br.com.techchallenge.mecanica.application.gateway;
import java.util.UUID;
import br.com.techchallenge.mecanica.application.input.PagamentoConfirmadoInput;
public interface PagamentoOrdemGateway {
    void registrar(PagamentoConfirmadoInput pagamento);
    void validarParaEntrega(UUID ordemId);
    void registrarEntrega(java.util.UUID id);
}