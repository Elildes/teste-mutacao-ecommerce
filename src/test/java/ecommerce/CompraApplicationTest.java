package ecommerce;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.main.web-application-type=none"}) // Desativa o servidor web
class CompraApplicationTest {

    @Test
    void mainDoesNotFail() {
        // Mock do método estático SpringApplication.run
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            // Configura o mock para não lançar exceção
            mockedSpringApplication.when(() -> SpringApplication.run(CompraApplication.class, new String[]{}))
                    .thenReturn(null);

            // Testa o método main
            assertDoesNotThrow(() -> CompraApplication.main(new String[]{}),
                    "O método main lançou uma exceção inesperada.");

            // Verifica se SpringApplication.run foi chamado corretamente
            mockedSpringApplication.verify(() -> SpringApplication.run(CompraApplication.class, new String[]{}),
                    times(1));
        }
    }
}
