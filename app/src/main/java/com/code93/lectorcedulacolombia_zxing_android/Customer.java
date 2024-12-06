package com.code93.lectorcedulacolombia_zxing_android;

import java.time.LocalDateTime;

public class Customer {

    private String id;
    private String documento;
    private String name;


    public Customer() {
    }

    public Customer(String id, String documento, String name, LocalDateTime horaEntrada) {
        this.id = id;
        this.documento = documento;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return name;
    }

    public void setNombre(String nombre) {
        this.name = nombre;
    }


}
