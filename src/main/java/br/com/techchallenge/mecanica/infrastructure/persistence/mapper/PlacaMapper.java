package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import org.springframework.stereotype.Component;

@Component
public class PlacaMapper {

    public Placa toDomain (String placaString) {
        return new Placa(placaString);
    }
}
