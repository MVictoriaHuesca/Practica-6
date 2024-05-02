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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;

public class MedicoControllerIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Prueba que un medico se crea correctamente")
    public void medico_SeCreaCorrectamente() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Juan");
        medico.setEspecialidad("Oftalmologia");
        
        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Prueba que un medico se obtiene correctamente dado su id")
    public void medico_SeObtieneCorrectamente() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setDni("12345678A");
        medico.setNombre("Juan");
        medico.setEspecialidad("Oftalmologia");
        

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.nombre").value("Juan"));
        
        this.mockMvc.perform(get("/medico/dni/12345678A"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("Prueba que un medico se actualiza correctamente")
    public void medico_SeActualizaCorrectamente() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Juan");
        medico.setEspecialidad("Oftalmologia");
        

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        medico.setNombre("Pedro");
        medico.setEspecialidad("Cardiologia");

        this.mockMvc.perform(put("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.nombre").value("Pedro"));
    }

    @Test
    @DisplayName("Prueba que un medico se elimina correctamente")
    public void medico_SeEliminaCorrectamente() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Juan");
        medico.setEspecialidad("Oftalmologia");
        

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(delete("/medico/1"))
            .andExpect(status().isOk())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/1"))
            .andDo(print())
            .andExpect(status().is5xxServerError());
    }
}
