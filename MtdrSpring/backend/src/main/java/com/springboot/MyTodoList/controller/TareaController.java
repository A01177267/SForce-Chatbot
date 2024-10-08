package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    // Get all tasks
    @GetMapping
    public List<Tarea> getAllTasks() {
        return tareaService.findAll();
    }

    // Create a new task
    @PostMapping
    public ResponseEntity<Tarea> crearTask(@Valid @RequestBody Tarea tarea) {
        Tarea newTask = tareaService.crearTarea(tarea);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    // Get a single task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> getTaskById(@PathVariable Long id) {
        Optional<Tarea> tarea = tareaService.obtenerTareaPorId(id);
        return tarea.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    // Update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> updateTask(@PathVariable Long id, @Valid @RequestBody Tarea tarea) {
        return tareaService.actualizarTarea(id, tarea)
                           .map(updatedTarea -> ResponseEntity.ok(updatedTarea))
                           .orElse(ResponseEntity.notFound().build());
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
