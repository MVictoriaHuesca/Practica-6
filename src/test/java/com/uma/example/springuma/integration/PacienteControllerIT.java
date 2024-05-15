package com.uma.example.springuma.integration;

/*
 * Autores:
 * - Eduardo García Rivas
 * - María Victoria Huesca Peláez
 */

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


public class PacienteControllerIT extends AbstractIntegration{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico crearMedico(){
        Medico medico = new Medico();
        medico.setId(1);
        medico.setDni("12345678A");
        medico.setNombre("Medico1");
        medico.setEspecialidad("Especialidad1");
        return medico;
    }

    private Paciente crearPaciente() {
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setDni("12345678A");
        paciente.setNombre("Paciente1");
        return paciente;
    }

    @Test
    @DisplayName("Test que crea un paciente y comprueba que se ha creado correctamente y se ha creado correctamente la relacion con su médico")
    public void crearPacienteTest() throws Exception {
        //el medico se crea correctamente
        Medico medico = this.crearMedico();

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        //el paciente se crea correctamente
        Paciente paciente = crearPaciente();
        paciente.setMedico(medico);
        
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        //request get paciente por id
        this.mockMvc.perform(get("/paciente/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1));

        //la relación medico-paciente se crea correctamente, get por medico id
        this.mockMvc.perform(get("/paciente/medico/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Test para actualizar un paciente y leerlo correctamente")
    public void actualizarPacienteTest() throws Exception {
         //el medico se crea correctamente
         Medico medico = this.crearMedico();
 
         this.mockMvc.perform(post("/medico")
             .contentType("application/json")
             .content(objectMapper.writeValueAsString(medico)))
             .andExpect(status().isCreated())
             .andExpect(status().is2xxSuccessful());
 
         //el paciente se crea correctamente
         Paciente paciente = crearPaciente();
         paciente.setMedico(medico);
         
         this.mockMvc.perform(post("/paciente")
             .contentType("application/json ")
             .content(objectMapper.writeValueAsString(paciente)))
             .andExpect(status().isCreated())
             .andExpect(status().is2xxSuccessful());
        
        //Se actualiza datos del paciente
        String nuevoNombre = "Paciente2";
        paciente.setNombre(nuevoNombre);

        // se comprueba request put
        this.mockMvc.perform(put("/paciente")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isNoContent())
            .andExpect(status().is2xxSuccessful());

        // se comprueba que se ha actualizado correctamente por paciente id y por medico id
        this.mockMvc.perform(get("/paciente/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.nombre").value("Paciente2"));

        this.mockMvc.perform(get("/paciente/medico/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$[0].nombre").value("Paciente2"));

    }

    @Test
    @DisplayName("Test para actualizar un medico de un paciente y comprobar que la relación se ha actualizado correctamente")
    public void actualizarMedicoPacienteTest() throws Exception {
        //el medico se crea correctamente
        Medico medico = this.crearMedico();

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        //el paciente se crea correctamente
        Paciente paciente = crearPaciente();
        paciente.setMedico(medico);
        
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
       
       //Se actualiza datos del medico
        String nuevoNombre = "Medico2";
        medico.setNombre(nuevoNombre);

        // se comprueba request put
        this.mockMvc.perform(put("/medico")
            .contentType("application/json ")
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isNoContent())
            .andExpect(status().is2xxSuccessful());
        
        // se comprueba que se ha actualizado correctamente por paciente id y por medico id
        this.mockMvc.perform(get("/paciente/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.medico.nombre").value("Medico2"));

        this.mockMvc.perform(get("/paciente/medico/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$[0].medico.nombre").value("Medico2"));

    }

    
}
