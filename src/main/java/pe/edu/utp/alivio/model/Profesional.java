package pe.edu.utp.alivio.model;

public class Profesional {
    private String codigo;
    private String nombreCompleto;
    private TipoProfesional tipoProfesional;
    private String telefono;
    private String zonaCobertura;
    private TipoServicio especialidad;
    private boolean disponible;

    public Profesional() {
    }

    public Profesional(String codigo, String nombreCompleto, TipoProfesional tipoProfesional,
                       String telefono, String zonaCobertura, TipoServicio especialidad,
                       boolean disponible) {
        this.codigo = codigo;
        this.nombreCompleto = nombreCompleto;
        this.tipoProfesional = tipoProfesional;
        this.telefono = telefono;
        this.zonaCobertura = zonaCobertura;
        this.especialidad = especialidad;
        this.disponible = disponible;
    }

    public Profesional(Profesional original) {
        this(original.codigo, original.nombreCompleto, original.tipoProfesional,
            original.telefono, original.zonaCobertura, original.especialidad, original.disponible);
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
