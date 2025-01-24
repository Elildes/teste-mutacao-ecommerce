package ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class CompraApplicationTests {

    @Test
    void mainMethodInitializesSpringContext() {
        // Inicializa o contexto manualmente
        ApplicationContext context = SpringApplication.run(CompraApplication.class);

        // Verifica se o contexto não é nulo
        assertNotNull(context, "O contexto Spring não foi inicializado.");

        // Verifica se algum bean essencial está presente
        assertTrue(context.containsBean("compraApplication"), 
            "O bean 'compraApplication' não foi registrado no contexto.");
    }
}
