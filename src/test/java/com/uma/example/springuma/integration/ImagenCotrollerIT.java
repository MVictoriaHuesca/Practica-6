/*
 * Autores:
 * - Eduardo García Rivas
 * - María Victoria Huesca Peláez
 */

package com.uma.example.springuma.integration;


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

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagenCotrollerIT {
    @LocalServerPort
    private Integer port;
    private WebTestClient webTestClient;
    private Paciente paciente;
    private Imagen imagen;
    
    @PostConstruct
    public void init(){
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofMillis(100000)).build();

        this.paciente = new Paciente();
        this.paciente.setId(1);
        this.paciente.setDni("12345678A");
        this.paciente.setNombre("Paciente1");

        this.imagen = new Imagen();
        this.imagen.setId(1);
        this.imagen.setPaciente(paciente);
    }

    @Test
    @DisplayName("Test que prueba que se sube una imagen de paciente correctamente")
    public void subirImagenPacienteTest() {
        //creacion del paciente
        this.webTestClient.post()
            .uri("/paciente")
            .body(Mono.just(paciente), Paciente.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().returnResult();

        //sube la imagen de paciente 1
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        //captura el resultado del request post en /imagen
        FluxExchangeResult<String> responseBody =  
            this.webTestClient.post()
            .uri("/imagen")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().is2xxSuccessful().returnResult(String.class);
        
        String result = responseBody.getResponseBody().blockFirst();
        String expected = "{\"response\" : \"file uploaded successfully : healthy.png\"}";

        //comprueba que el resultado es el esperado
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Test que prueba predicción de paciente sin cáncer correctamente")
    public void prediccionSinCancerTest() {
        //creacion del paciente
        this.webTestClient.post()
            .uri("/paciente")
            .body(Mono.just(paciente), Paciente.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().returnResult();
        
        //sube la imagen de paciente 1
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/healthy.png"));
        builder.part("paciente", paciente);

        this.webTestClient.post()
            .uri("/imagen")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(String.class);
        
        FluxExchangeResult<String> responseBody =  
            this.webTestClient.get()
            .uri("/imagen/predict/1")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(String.class);
        
        String result = responseBody.getResponseBody().blockFirst();
        String expected = "Not cancer (label 0)";
        assertTrue(result.contains(expected));
    }

    @Test
    @DisplayName("Test que prueba predicción de paciente sin cáncer correctamente")
    public void prediccionConCancerTest() {
        //creacion del paciente
        this.webTestClient.post()
            .uri("/paciente")
            .body(Mono.just(paciente), Paciente.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().returnResult();
        
        //sube la imagen de paciente 1
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource("./src/test/resources/no_healthty.png"));
        builder.part("paciente", paciente);

        this.webTestClient.post()
            .uri("/imagen")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(String.class);
        
        FluxExchangeResult<String> responseBody =  
            this.webTestClient.get()
            .uri("/imagen/predict/1")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(String.class);
        
        String result = responseBody.getResponseBody().blockFirst();
        String expected = "Cancer (label 1)";
        assertTrue(result.contains(expected));
    }
}
