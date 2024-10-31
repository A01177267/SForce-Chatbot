package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "NEW_PROJECT") // Specify the correct table name and schema
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID") // Ensure the column name matches exactly with DB schema
    private Long id;

    @Column(name = "NAME") // Match the column name for 'NAME'
    private String nombre;

    @Column(name = "STATUS", length = 50) // Match the column name for 'STATUS' and specify length
    private String estatus;

    @Temporal(TemporalType.DATE)
    @Column(name = "START_DATE") // Match the column name for 'START_DATE'
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE") // Match the column name for 'END_DATE'
    private Date fechaFin;

    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Tarea> tareas; // Configuración para manejo de las tareas como parte del proyecto

    public Proyecto() {
    }

    public Proyecto(Long id, String nombre, String estatus, Date fechaInicio, Date fechaFin, List<Tarea> tareas) {
        this.id = id;
        this.nombre = nombre;
        this.estatus = estatus;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tareas = tareas;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public List<Tarea> getTareas() {
        return tareas;
    }

    public void setTareas(List<Tarea> tareas) {
        this.tareas = tareas;
    }
}