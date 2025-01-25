# Projeto de Testes Automatizados para Funcionalidade de Finalização de Compra

## Integrantes
- ELILDES FORTALEZA SANTOS - 20240078023
- KATRIEL ALBUQUERQUE GALVAO DE ARAUJO - 20210050308
- MANUEL JONAS FONSECA BARBALHO - 20240078168

## Como Executar o Projeto

### Pré-requisitos
- Java 21
- Maven

### Passos para Executar o Projeto

1. Clone o repositório:
    ```sh
    git clone https://github.com/Elildes/teste-mutacao-ecommerce
    cd ElildesFortaleza-KatrielAraujo-ManuelBarbalho
    ```

2. Compile o projeto:
    ```sh
    mvn clean compile
    ```

3. Execute o projeto:
    ```sh
    mvn spring-boot:run
    ```

### Executando os Testes Unitários

Para executar os testes unitários com JUnit, utilize o comando:
```sh
mvn test
```

### Executando os Testes de Mutação e Verificando a Cobertura dos Testes

Para executar os testes de mutação com a ferramenta PIT, utilize o comando:
```sh
mvn test-compile org.pitest:pitest-maven:mutationCoverage
 ```
