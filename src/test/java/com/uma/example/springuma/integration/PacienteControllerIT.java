package com.uma.example.springuma.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.uma.example.springuma.controller.PacienteController;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

public class PacienteControllerIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico creacionMedico(){
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Mario");
        medico.setEspecialidad("Oftalmologia");

        return medico;
    }

    private Paciente creacioPaciente(){
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Juan");
        paciente.setDni("12345678A");

        return paciente;
    }

    @Test
    @DisplayName("Prueba que se asocia un paciente a un medico correctamente")
    public void postPaciente_SeAsociaPacienteAMedicoCorrectamente() throws Exception {
        Medico medico = creacionMedico();

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        Paciente paciente = creacioPaciente();

        paciente.setMedico(medico);
        
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/paciente/1"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.nombre").value("Juan"));

        this.mockMvc.perform(get("/paciente/medico/1"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$[0].nombre").value("Juan"))
            .andExpect(jsonPath("$[0].medico.nombre").value("Mario"));
    }

    @Test
    @DisplayName("Prueba que se edita un paciente a un medico correctamente")
    public void putPaciente_SeEditaPacienteAMedicoCorrectamente() throws Exception {
        Medico medico = creacionMedico();

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        Paciente paciente = creacioPaciente();

        paciente.setMedico(medico);
        
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        String nuevoNombrePaciente = "Pedro";
        paciente.setNombre(nuevoNombrePaciente);

        this.mockMvc.perform(put("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/paciente/medico/1"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$[0].nombre").value(nuevoNombrePaciente));
    }


    @Test
    @DisplayName("Prueba que se edita medico a un paciente correctamente")
    public void putPaciente_SeEditaMedicoDePacienteCorrectamente() throws Exception {
        Medico medico = creacionMedico();

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        Paciente paciente = creacioPaciente();

        paciente.setMedico(medico);
        
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        String nuevoNombreMedico = "Pedro";
        medico.setNombre(nuevoNombreMedico);

        this.mockMvc.perform(put("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/paciente/medico/1"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$[0].medico.nombre").value(nuevoNombreMedico));
    }

}
