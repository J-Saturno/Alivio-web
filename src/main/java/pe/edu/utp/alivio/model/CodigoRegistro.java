package pe.edu.utp.alivio.model;

import java.util.Optional;

/** Convierte los códigos visibles en identificadores persistentes. */
public final class CodigoRegistro {
    private CodigoRegistro() {}

    public static Optional<Long> id(String codigo, String prefijo) {
        if (codigo == null || !codigo.matches(prefijo + "-[0-9]+")) return Optional.empty();
        try {
            long id = Long.parseLong(codigo.substring(prefijo.length() + 1));
            return id > 0 ? Optional.of(id) : Optional.empty();
        } catch (NumberFormatException error) {
            return Optional.empty();
        }
    }
}
