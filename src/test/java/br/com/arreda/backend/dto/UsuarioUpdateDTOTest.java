package br.com.arreda.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioUpdateDTOTest {

    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> camposInvalidos(UsuarioUpdateDTO dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void dadosValidos_naoGeramErros() {
        assertTrue(validator.validate(new UsuarioUpdateDTO("João Silva", "31988887777")).isEmpty());
    }

    @Test
    void telefoneInvalido_apontaSomenteTelefone() {
        assertEquals(Set.of("telefone"),
                camposInvalidos(new UsuarioUpdateDTO("João Silva", "abc")));
    }

    @Test
    void nomeEmBranco_apontaSomenteNome() {
        assertEquals(Set.of("nome"),
                camposInvalidos(new UsuarioUpdateDTO("  ", "31988887777")));
    }

    @Test
    void nomeETelefoneInvalidos_apontamOsDois() {
        assertEquals(Set.of("nome", "telefone"),
                camposInvalidos(new UsuarioUpdateDTO("", "123")));
    }
}