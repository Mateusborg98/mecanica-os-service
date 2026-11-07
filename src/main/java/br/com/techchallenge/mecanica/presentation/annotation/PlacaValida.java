package br.com.techchallenge.mecanica.presentation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.com.techchallenge.mecanica.presentation.validator.PlacaValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = PlacaValidator.class)
public @interface PlacaValida {

    String message() default "Placa inválida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}