package pe.edu.utp.alivio.web;

import jakarta.validation.constraints.*;
import pe.edu.utp.alivio.validacion.Textos;
import pe.edu.utp.alivio.model.ResultadoVerificacion;

public class VerificacionForm {
    private boolean identidadComprobada;
    private boolean credencialComprobada;
    private boolean referenciasComprobadas;
    @Min(value = 0, message = "La experiencia debe estar entre 0 y 80 años")
    @Max(value = 80, message = "La experiencia debe estar entre 0 y 80 años")
    private Integer aniosExperiencia;
    @Size(max = 100, message = "La referencia debe tener como máximo 100 caracteres")
    private String referenciaCredencial;
    @Size(max = 1000, message = "La evidencia debe tener como máximo 1000 caracteres")
    private String notaEvidencia;
    @NotNull(message = "Selecciona un resultado válido")
    private ResultadoVerificacion resultado;

    public boolean isIdentidadComprobada() { return identidadComprobada; }
    public void setIdentidadComprobada(boolean valor) { identidadComprobada = valor; }
    public boolean isCredencialComprobada() { return credencialComprobada; }
    public void setCredencialComprobada(boolean valor) { credencialComprobada = valor; }
    public boolean isReferenciasComprobadas() { return referenciasComprobadas; }
    public void setReferenciasComprobadas(boolean valor) { referenciasComprobadas = valor; }
    public Integer getAniosExperiencia() { return aniosExperiencia; }
    public void setAniosExperiencia(Integer valor) { aniosExperiencia = valor; }
    public String getReferenciaCredencial() { return referenciaCredencial; }
    public void setReferenciaCredencial(String valor) { referenciaCredencial = Textos.linea(valor); }
    public String getNotaEvidencia() { return notaEvidencia; }
    public void setNotaEvidencia(String valor) { notaEvidencia = Textos.parrafos(valor); }
    public ResultadoVerificacion getResultado() { return resultado; }
    public void setResultado(ResultadoVerificacion valor) { resultado = valor; }
}
