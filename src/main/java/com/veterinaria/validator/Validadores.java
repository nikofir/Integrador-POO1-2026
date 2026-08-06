package com.veterinaria.validator;

import com.veterinaria.exception.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Metodos genericos de validacion de datos de entrada (texto, numeros,
 * fechas, email). Todas las validaciones lanzan {@link ValidacionException}.
 */
public final class Validadores {

    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");
    private static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} .'\\-]*$");
    private static final Pattern EMAIL = Pattern.compile("^[\\p{L}\\d._%+-]+@[\\p{L}\\d.-]+\\.[\\p{L}]{2,}$");
    private static final Pattern FICHA = Pattern.compile("^M-\\d{4}-\\d{4}$");

    private Validadores() {
    }

    /** El objeto no puede ser {@code null}. */
    public static void noNulo(Object valor, String campo) {
        if (valor == null) {
            throw new ValidacionException(campo + " es obligatorio.");
        }
    }

    /** El texto no puede ser {@code null} ni estar en blanco; devuelve el texto normalizado. */
    public static String noVacio(String valor, String campo) {
        noNulo(valor, campo);
        if (valor.isBlank()) {
            throw new ValidacionException(campo + " no puede estar vacio.");
        }
        return valor.trim();
    }

    /** Texto obligatorio con longitud maxima. */
    public static String longitud(String valor, int maximo, String campo) {
        String limpio = noVacio(valor, campo);
        if (limpio.length() > maximo) {
            throw new ValidacionException(campo + " supera el maximo de " + maximo + " caracteres.");
        }
        return limpio;
    }

    /** El texto debe contener unicamente digitos. */
    public static String soloDigitos(String valor, String campo) {
        String limpio = noVacio(valor, campo);
        if (!SOLO_DIGITOS.matcher(limpio).matches()) {
            throw new ValidacionException(campo + " debe contener solo numeros.");
        }
        return limpio;
    }

    /** Digitos con cantidad entre {@code minimo} y {@code maximo}. */
    public static String rango(String valor, int minimo, int maximo, String campo) {
        String limpio = soloDigitos(valor, campo);
        if (limpio.length() < minimo || limpio.length() > maximo) {
            throw new ValidacionException(campo + " debe tener entre " + minimo + " y " + maximo + " digitos.");
        }
        return limpio;
    }

    /** Nombre de persona: letras (incluye tildes), espacios y algunos separadores. */
    public static String nombre(String valor, String campo) {
        String limpio = longitud(valor, 60, campo);
        if (!NOMBRE.matcher(limpio).matches()) {
            throw new ValidacionException(campo + " contiene caracteres no validos.");
        }
        return limpio;
    }

    /** Direccion de email con formato estandar. */
    public static String email(String valor, String campo) {
        String limpio = longitud(valor, 100, campo);
        if (!EMAIL.matcher(limpio).matches()) {
            throw new ValidacionException(campo + " no es una direccion de email valida.");
        }
        return limpio;
    }

    /** Entero estrictamente positivo a partir de un texto. */
    public static int enteroPositivo(String valor, String campo) {
        String limpio = soloDigitos(valor, campo);
        int numero;
        try {
            numero = Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un numero valido.");
        }
        if (numero <= 0) {
            throw new ValidacionException(campo + " debe ser un numero mayor a cero.");
        }
        return numero;
    }

    /** Importe monetario estrictamente positivo a partir de un texto. */
    public static BigDecimal importePositivo(String valor, String campo) {
        String limpio = noVacio(valor, campo);
        BigDecimal monto;
        try {
            monto = new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un numero valido.");
        }
        if (monto.signum() <= 0) {
            throw new ValidacionException(campo + " debe ser un importe mayor a cero.");
        }
        return monto;
    }

    /** La fecha no puede ser posterior a hoy. */
    public static void noFutura(LocalDate fecha, String campo) {
        noNulo(fecha, campo);
        if (fecha.isAfter(LocalDate.now())) {
            throw new ValidacionException(campo + " no puede ser una fecha futura.");
        }
    }

    /** El momento debe ser posterior al instante actual. */
    public static void futura(LocalDateTime momento, String campo) {
        noNulo(momento, campo);
        if (!momento.isAfter(LocalDateTime.now())) {
            throw new ValidacionException(campo + " debe ser una fecha y hora futura.");
        }
    }

    /** La coleccion no puede ser {@code null}, vacia ni contener elementos nulos. */
    public static void coleccionValida(Collection<?> coleccion, String campo) {
        noNulo(coleccion, campo);
        if (coleccion.isEmpty()) {
            throw new ValidacionException(campo + " no puede estar vacia.");
        }
        if (coleccion.stream().anyMatch(e -> e == null)) {
            throw new ValidacionException(campo + " no puede contener elementos nulos.");
        }
    }

    /** Formato de ficha unico de mascota: M-AAAA-NNNN. */
    public static String ficha(String valor, String campo) {
        String limpio = longitud(valor, 20, campo);
        if (!FICHA.matcher(limpio).matches()) {
            throw new ValidacionException(campo + " debe tener el formato M-AAAA-NNNN.");
        }
        return limpio;
    }
}
