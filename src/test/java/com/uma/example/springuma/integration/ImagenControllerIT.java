/*
 * @autor1 = Eduardo García Rivas
 * @autor2 = María Victoria Huesca Peláez
 */

package com.uma.example.springuma.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Paciente;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagenControllerIT{
    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Paciente paciente;

    private Imagen imagen;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
        .responseTimeout(Duration.ofMillis(30000)).build();

        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Juan");
        paciente.setDni("12345678A");

        imagen = new Imagen();
        imagen.setId(1);
        imagen.setPaciente(paciente);
    }

    @Test
    @DisplayName("Prueba que se sube una imagen correctamente")
    public void postImagen_SeSubeImagenCorrectamente() {
        client.post().uri("/paciente")
        .body(Mono.just(paciente), Paciente.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().returnResult();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        FluxExchangeResult<String> responseBody = client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(String.class);

        String result = responseBody.getResponseBody().blockFirst();

        String expected = "{\"response\" : \"file uploaded successfully : healthy.png\"}";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Prueba que da una prediccion de un paciente sin sintomas de cáncer")
    public void getImagen_SePredicePacienteSinCancer() {
        client.post().uri("/paciente")
        .body(Mono.just(paciente), Paciente.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(Paciente.class);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(String.class);

        FluxExchangeResult<String> responseBody = client.get()
        .uri("/imagen/predict/1")
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(String.class);

        String result = responseBody.getResponseBody().blockFirst();

        assertTrue(result.contains("Not cancer (label 0)"));
    }

    @Test
    @DisplayName("Prueba que da una prediccion de un paciente con sintomas de cáncer")
    public void getImagen_SePredicePacienteConCancer() {
        client.post().uri("/paciente")
        .body(Mono.just(paciente), Paciente.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(Paciente.class);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/no_healthty.png"));
        builder.part("paciente", paciente);

        client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(String.class);

        // con el get hace el predict
        FluxExchangeResult<String> responseBody = client.get()
        .uri("/imagen/predict/1")
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(String.class);

        String result = responseBody.getResponseBody().blockFirst();

        assertTrue(result.contains("Cancer (label 1)"));
    }
}
