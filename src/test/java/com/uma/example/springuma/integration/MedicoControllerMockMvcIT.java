package com.uma.example.springuma.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;

public class MedicoControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test para crear un médico y leerlo correctamente por su id y por su dni")
    public void testCrearMedico() throws JsonProcessingException, Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setDni("12345678A");
        medico.setNombre("Medico1");
        medico.setEspecialidad("Especialidad1");
        
        this.mockMvc.perform(post("/medico")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.id").value(1));

        this.mockMvc.perform(get("/medico/dni/12345678A"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    @DisplayName("Test para actualizar un médico y leerlo correctamente")
    public void actualizarTest() throws JsonProcessingException, Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setDni("12345678A");
        medico.setNombre("Medico1");
        medico.setEspecialidad("Especialidad1");
        
        this.mockMvc.perform(post("/medico")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        medico.setNombre("Medico2");
        medico.setEspecialidad("Especialidad2");
        medico.setDni("12345678B");
        medico.setId(2);

        this.mockMvc.perform(put("/medico")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isNoContent())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/2"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.nombre").value("Medico2"));

        this.mockMvc.perform(get("/medico/dni/12345678B"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.nombre").value("Medico2"));
    }

    @Test
    @DisplayName("Test para eliminar un medico y comprobar que no existe")
    public void eliminarMedicoTest () throws JsonProcessingException, Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setDni("12345678A");
        medico.setNombre("Medico1");
        medico.setEspecialidad("Especialidad1");

        this.mockMvc.perform(post("/medico")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
        
        this.mockMvc.perform(delete("/medico/1"))
        .andExpect(status().isOk());

        
        this.mockMvc.perform(get("/medico/1"))
        .andDo(print())
        .andExpect(status().is5xxServerError());
    }
}
