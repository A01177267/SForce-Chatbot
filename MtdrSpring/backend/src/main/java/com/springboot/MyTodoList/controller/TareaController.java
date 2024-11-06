package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import com.springboot.MyTodoList.dto.TareaDTO;
import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.repository.ProyectoRepository; // Import the ProyectoRepository

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private ProyectoRepository proyectoRepository;

    // Get all tasks
    @GetMapping
    public List<Tarea> getAllTasks() {
        return tareaService.findAll();
    }

    // Create a new task
    @PostMapping("/project/{projectId}")
    public ResponseEntity<Tarea> crearTask(@PathVariable(value="projectId") Long projectId ,@Valid @RequestBody Tarea tarea) {
        Tarea newTask = tareaService.crearTarea(projectId,tarea);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    // Get a single task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> getTaskById(@PathVariable Long id) {
        try {
            Tarea tarea = tareaService.obtenerTareaPorId(id);
            return new ResponseEntity<>(tarea, HttpStatus.OK);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> updateTask(@PathVariable Long id, @Valid @RequestBody Tarea tarea) {
        try {
            Tarea updatedTarea = tareaService.actualizarTarea(id, tarea);
            if (updatedTarea != null) {
                return new ResponseEntity<>(updatedTarea, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean isDeleted = tareaService.eliminarTarea(id);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
