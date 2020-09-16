package br.edu.utfpr.dv.siacoes.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentTest {

    @Test
    @DisplayName("Teste: getName - Department")
    void testeDepartment() {
        Department department = new Department();
        String name = "Departamento teste";
        
    	department.setName(name);

    	assertEquals("Departamento teste", department.getName());
    }

    @Test
    @DisplayName("Teste: getName - Campus")
    void testeCampus() {
        Department department = new Department();
    	String name = "Campus teste";

    	department.setName(name);

    	assertEquals("Campus teste", department.getName());
    }
} 