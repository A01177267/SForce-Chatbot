package com.springboot.MyTodoList.dto;

import java.util.Date;

public class TareaDTO {
    private String descripcion;
    private String estatus;
    private Float tiempoEstimado;
    private Float tiempoReal;
    private Date fechaFinalizacion;
    private Integer puntuacionCalidad;
    private Float eficienciaTarea;
    private Float productividadTarea;
    private Long projectId; // This will hold the projectId from the JSON

    // Getters and setters
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public Float getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(Float tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public Float getTiempoReal() {
        return tiempoReal;
    }

    public void setTiempoReal(Float tiempoReal) {
        this.tiempoReal = tiempoReal;
    }

    public Date getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(Date fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Integer getPuntuacionCalidad() {
        return puntuacionCalidad;
    }

    public void setPuntuacionCalidad(Integer puntuacionCalidad) {
        this.puntuacionCalidad = puntuacionCalidad;
    }

    public Float getEficienciaTarea() {
        return eficienciaTarea;
    }

    public void setEficienciaTarea(Float eficienciaTarea) {
        this.eficienciaTarea = eficienciaTarea;
    }

    public Float getProductividadTarea() {
        return productividadTarea;
    }

    public void setProductividadTarea(Float productividadTarea) {
        this.productividadTarea = productividadTarea;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
