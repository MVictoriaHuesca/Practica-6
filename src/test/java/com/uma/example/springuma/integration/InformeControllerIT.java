package com.uma.example.springuma.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InformeControllerIT {
    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Paciente paciente;

    private Imagen imagen;

    private Informe informe;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
        .responseTimeout(Duration.ofMillis(30000)).build();

        // Un informe depende de una imagen que a la vez depende de un paciente
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Juan");
        paciente.setDni("12345678A");

        imagen = new Imagen();
        imagen.setId(1);
        imagen.setNombre("healthy.png");
        imagen.setPaciente(paciente);

        informe = new Informe();
        informe.setId(1);
        informe.setImagen(imagen);
        informe.setPrediccion("Not cancer (label 0),  score: 0.984481368213892");
        informe.setContenido("informe1");
    }

    @Test
    @DisplayName("Prueba que se crea un informe correctamente")
    public void postInforme_SeCreaInformeCorrectamente() {
        client.post().uri("/paciente")
        .body(Mono.just(paciente), Paciente.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().returnResult();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody().returnResult();

        client.post().uri("/informe")
        .body(Mono.just(informe), Informe.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().returnResult();

        FluxExchangeResult<Informe> responseBody = client.get()
        .uri("/informe/1")
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(Informe.class);

        Informe result = responseBody.getResponseBody().blockFirst();
        String resultToString = result.toString();

        assertTrue(resultToString.contains("Contenido = informe1"));
    }

    @Test
    @DisplayName("Prueba que se elimina un informe correctamente")
    public void deleteInforme_SeEliminaInformeCorrectamente() {
        client.post().uri("/paciente")
        .body(Mono.just(paciente), Paciente.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().returnResult();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody().returnResult();

        client.post().uri("/informe")
        .body(Mono.just(informe), Informe.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().returnResult();

        client.delete().uri("/informe/1")
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

        // Cuando hago .expectStatus().is5xxServerError() me dice que es successful
        // asi que, para comprobar que al hacer un get no encuentra el informe
        // que acabo de borrar, hago un assertNull de result
        FluxExchangeResult<Informe> responseBody = client.get()
        .uri("/informe/1")
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(Informe.class);

        Informe result = responseBody.getResponseBody().blockFirst();
        
        assertNull(result);

    }
    
}
