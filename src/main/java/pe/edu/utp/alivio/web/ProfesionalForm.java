package pe.edu.utp.alivio.web;

import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;

public class ProfesionalForm {
    private String codigo;
    private String nombreCompleto;
    private TipoProfesional tipoProfesional;
    private String telefono;
    private String zonaCobertura;
    private TipoServicio especialidad;
    private boolean disponible;

    public Profesional toProfesional() {
        return new Profesional(codigo, nombreCompleto, tipoProfesional, telefono,
                zonaCobertura, especialidad, disponible);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public TipoProfesional getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(TipoProfesional tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getZonaCobertura() {
        return zonaCobertura;
    }

    public void setZonaCobertura(String zonaCobertura) {
        this.zonaCobertura = zonaCobertura;
    }

    public TipoServicio getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(TipoServicio especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
