package br.com.arreda.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regra de força combinada com o time: min 8, máx 72, com letras e números. */
class RedefinirSenhaDTOTest {

    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> camposInvalidos(RedefinirSenhaDTO dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void senhaValida_naoGeraErros() {
        assertTrue(validator.validate(new RedefinirSenhaDTO("token-x", "abcd1234")).isEmpty());
    }

    @Test
    void senhaCurta_apontaNovaSenha() {
        assertEquals(Set.of("novaSenha"),
                camposInvalidos(new RedefinirSenhaDTO("token-x", "abc1")));
    }

    @Test
    void senhaSemNumero_apontaNovaSenha() {
        assertEquals(Set.of("novaSenha"),
                camposInvalidos(new RedefinirSenhaDTO("token-x", "abcdefgh")));
    }

    @Test
    void senhaSemLetra_apontaNovaSenha() {
        assertEquals(Set.of("novaSenha"),
                camposInvalidos(new RedefinirSenhaDTO("token-x", "123456789")));
    }

    @Test
    void senhaComMaiusculasESimbolosEteLetraENumero_eValida() {
        assertTrue(validator.validate(new RedefinirSenhaDTO("token-x", "Abc3$%efe")).isEmpty());
    }

    @Test
    void senhaComMaisDe72Caracteres_apontaNovaSenha() {
        assertEquals(Set.of("novaSenha"),
                camposInvalidos(new RedefinirSenhaDTO("token-x", "a1".repeat(36) + "a")));
    }

    @Test
    void tokenEmBranco_apontaToken() {
        assertEquals(Set.of("token"),
                camposInvalidos(new RedefinirSenhaDTO("", "abcd1234")));
    }
}
