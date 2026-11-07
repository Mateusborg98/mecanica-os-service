package br.com.techchallenge.mecanica.domain.veiculo.valueObject;

import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;

import java.util.Objects;
import java.util.regex.Pattern;


public class Placa {

    private static final Pattern PATTERN =
            Pattern.compile("^(?:[A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})$");

    private final String valor;

    public Placa(String valor) throws PlacaInvalidaException {

        String placaNormalizada = normalizar(valor);

        validar(placaNormalizada);

        this.valor = placaNormalizada;
    }

    private String normalizar(String placa) throws PlacaInvalidaException {

        if (placa == null) {
            throw new PlacaInvalidaException("Placa não pode ser nula");
        }

        return placa
                .replace("-", "")
                .trim()
                .toUpperCase();
    }

    private void validar(String placa) throws PlacaInvalidaException {

        if (!PATTERN.matcher(placa).matches()) {
            throw new PlacaInvalidaException(
                    "Placa inválida: " + placa);
        }
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Placa placa)) return false;

        return Objects.equals(valor, placa.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
