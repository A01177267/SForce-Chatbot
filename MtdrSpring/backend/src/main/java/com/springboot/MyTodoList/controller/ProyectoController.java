package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.model.Tarea;

import com.springboot.MyTodoList.service.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.ResponseEntity;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping
    public List<Proyecto> getAllProjects() {
        return proyectoService.findAll();
    }

    @PostMapping
    public ResponseEntity createProject(@RequestBody Proyecto proyecto) throws Exception{
        Integer i = 7;
        //Long l = new Long(i);
        //Proyecto newproject = new Proyecto(null, "test123", "Active", null, null, null);
        //System.out.println("cagada");
        Proyecto td = proyectoService.crearProyecto(proyecto);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+td.getId());
        responseHeaders.set("Access-Control-Expose-Headers","location");
        //URI location = URI.create(""+td.getID())

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> getProjectById(@PathVariable Long id) {
        return proyectoService.obtenerProyectoPorId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> updateProject(@PathVariable Long id, @Valid @RequestBody Proyecto proyecto) {
        return proyectoService.actualizarProyecto(id, proyecto)
                              .map(ResponseEntity::ok)
                              .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (!proyectoService.eliminarProyecto(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
