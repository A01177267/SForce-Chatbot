package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "TASK") // Ensure this is the correct schema and table name
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DESCRIPTION")
    private String descripcion;

    @Column(name = "STATUS")
    private String estatus;

    @Column(name = "ESTIMATEDTIME")
    private Float tiempoEstimado;

    @Column(name = "ACTUALTIME")
    private Float tiempoReal;

    @Column(name = "COMPLETIONDATE")
    private Date fechaFinalizacion;

    @Column(name = "QUALITYSCORE")
    private Integer puntuacionCalidad;

    @Column(name = "TASKEFFICIENCY")
    private Float eficienciaTarea;

    @Column(name = "TASKPRODUCTIVITY")
    private Float productividadTarea;

    @ManyToOne
    @JoinColumn(name = "PROJECTID", nullable = false)
    //@JsonBackReference
    @JsonBackReference
    private Proyecto proyecto;

    public Tarea(Long id, String descripcion, String estatus, Float tiempoEstimado, Float tiempoReal, Date fechaFinalizacion, Integer puntuacionCalidad, Float eficienciaTarea, Float productividadTarea, Proyecto proyecto) {
        this.id = id;
        this.descripcion = descripcion;
        this.estatus = estatus;
        this.tiempoEstimado = tiempoEstimado;
        this.tiempoReal = tiempoReal;
        this.fechaFinalizacion = fechaFinalizacion;
        this.puntuacionCalidad = puntuacionCalidad;
        this.eficienciaTarea = eficienciaTarea;
        this.productividadTarea = productividadTarea;
        this.proyecto = proyecto;
        
    }

    public Tarea() {

    }
    // Getters
    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstatus() {
        return estatus;
    }

    public Float getTiempoEstimado() {
        return tiempoEstimado;
    }

    public Float getTiempoReal() {
        return tiempoReal;
    }

    public Date getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public Integer getPuntuacionCalidad() {
        return puntuacionCalidad;
    }

    public Float getEficienciaTarea() {
        return eficienciaTarea;
    }

    public Float getProductividadTarea() {
        return productividadTarea;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public void setTiempoEstimado(Float tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public void setTiempoReal(Float tiempoReal) {
        this.tiempoReal = tiempoReal;
    }

    public void setFechaFinalizacion(Date fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public void setPuntuacionCalidad(Integer puntuacionCalidad) {
        this.puntuacionCalidad = puntuacionCalidad;
    }

    public void setEficienciaTarea(Float eficienciaTarea) {
        this.eficienciaTarea = eficienciaTarea;
    }

    public void setProductividadTarea(Float productividadTarea) {
        this.productividadTarea = productividadTarea;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }
}
