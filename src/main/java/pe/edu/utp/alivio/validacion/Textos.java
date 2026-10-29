package pe.edu.utp.alivio.validacion;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Reglas compartidas para formularios y operaciones del servidor. */
public final class Textos {
    private Textos() { }

    private static final String NOMBRE = "\\p{L}[\\p{L}\\p{M}]*(?:\\.?[ ’'\\-]\\p{L}[\\p{L}\\p{M}]*)*\\.?";
    private static final String DISTRITO = "(?=[\\p{L}\\p{M}\\p{N} .’'\\-]*\\p{L})"
        + "[\\p{L}\\p{M}\\p{N}]+(?:[ .’'\\-][\\p{L}\\p{M}\\p{N}]+)*\\.?";
    public static final String NOMBRE_O_VACIO = "(?:" + NOMBRE + ")?";
    public static final String DISTRITO_O_VACIO = "(?:" + DISTRITO + ")?";
    public static final String COBERTURA_O_VACIO = "(?:" + DISTRITO + "(?:, " + DISTRITO + ")*)?";

    public static String linea(String valor) {
        if (valor == null) return null;
        var limpio = new StringBuilder();
        boolean tieneBase = false;
        for (int c : Normalizer.normalize(valor, Normalizer.Form.NFC).codePoints().toArray()) {
            // CGJ y rellenos Hangul son invisibles aunque no tengan categoría FORMAT.
            if (Character.getType(c) == Character.FORMAT || c == 0x034F
                    || c == 0x115F || c == 0x1160 || c == 0x3164 || c == 0xFFA0) continue;
            if (Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c)) {
                limpio.append(' ');
                tieneBase = false;
            } else {
                int tipo = Character.getType(c);
                boolean marca = tipo == Character.NON_SPACING_MARK || tipo == Character.COMBINING_SPACING_MARK
                    || tipo == Character.ENCLOSING_MARK;
                // Un acento o selector de variante sin una letra/símbolo base no es contenido.
                if (marca && !tieneBase) continue;
                limpio.appendCodePoint(c);
                if (!marca) tieneBase = true;
            }
        }
        return limpio.toString().replaceAll(" +", " ").trim();
    }

    public static String parrafos(String valor) {
        if (valor == null) return null;
        return Arrays.stream(valor.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1))
            .map(Textos::linea).collect(Collectors.joining("\n")).strip();
    }

    public static String telefono(String valor) {
        String limpio = linea(valor);
        return limpio == null ? null : limpio.replace(" ", "");
    }

    public static String cobertura(String valor) {
        String limpio = linea(valor);
        return limpio == null ? null : limpio.replaceAll(" +[-–—] +", ",")
            .replaceAll(" *[,;] *", ", ").trim();
    }

    public static boolean nombreValido(String valor) {
        return valor != null && valor.matches(NOMBRE);
    }

    public static boolean distritoValido(String valor) {
        return valor != null && valor.matches(DISTRITO);
    }

    public static boolean coberturaValida(String valor) {
        return valor != null && !valor.isBlank() && valor.matches(COBERTURA_O_VACIO);
    }

    public static String claveNombre(String valor) {
        String limpio = linea(valor);
        return limpio == null ? "" : Normalizer.normalize(limpio, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
