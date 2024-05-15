package com.uma.example.springuma.integration;

/*
 * Autores:
 * - Eduardo García Rivas
 * - María Victoria Huesca Peláez
 */

import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InformeControllerIT {
    @LocalServerPort
    private Integer port;
    private WebTestClient webTestClient;
    private Paciente paciente;
    private Imagen imagen;
    private Informe informe;
    
    @PostConstruct
    public void init(){
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofMillis(100000)).build();

        this.paciente = new Paciente();
        this.paciente.setId(1);
        this.paciente.setDni("12345678A");
        this.paciente.setNombre("Paciente1");

        this.imagen = new Imagen();
        this.imagen.setId(1);
        this.imagen.setNombre("healthy.png");
        this.imagen.setPaciente(this.paciente);

        this.informe = new Informe();
        this.informe.setId(1);
        this.informe.setContenido("informe1");
        this.informe.setImagen(this.imagen);
    }

    @Test
    @DisplayName ("Test que prueba que se sube un informe de paciente correctamente")
    public void subirInformePacienteTest() {
        //creacion del paciente
        this.webTestClient.post()
            .uri("/paciente")
            .body(Mono.just(this.paciente), Paciente.class)
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

        //creacion del informe
        this.webTestClient.post()
            .uri("/informe")
            .body(Mono.just(this.informe), Informe.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().returnResult();
        
        //compreba que el informe se ha creado correctamente con petición get
        FluxExchangeResult<Informe>  responseBody =  
            this.webTestClient.get()
            .uri("/informe/1")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(Informe.class);
        
        Informe result = responseBody.getResponseBody().blockFirst();
        Boolean condicion = result.toString().contains("informe1 ") && result.toString().contains(this.imagen.toString());

        assertTrue(condicion);
    }

    @Test
    @DisplayName ("Test que prueba que se borra un informe de paciente correctamente")
    public void borrarInformePacienteTest() {
        //creacion del paciente
        this.webTestClient.post()
            .uri("/paciente")
            .body(Mono.just(this.paciente), Paciente.class)
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

        //creacion del informe
        this.webTestClient.post()
            .uri("/informe")
            .body(Mono.just(this.informe), Informe.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().returnResult();
        
        //borra el informe
        this.webTestClient.delete()
            .uri("/informe/1")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().returnResult();
        
        //comprueba que el informe se ha borrado correctamente con petición get
        FluxExchangeResult<Informe>  responseBody =  
            this.webTestClient.get()
            .uri("/informe/1")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(Informe.class);
        
        Informe result = responseBody.getResponseBody().blockFirst();
        assertTrue(result == null);
    }

}
