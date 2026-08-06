package com.veterinaria.controller;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.PersistenciaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.exception.ValidacionException;
import javafx.scene.control.Alert;

/**
 * Utilidad para mostrar alertas de error e informacion y traducir las
 * excepciones del dominio a mensajes legibles para el usuario.
 */
public final class Alertas {

    private Alertas() {
    }

    public static void error(String titulo, String encabezado) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.showAndWait();
    }

    public static void informacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra la excepcion con un mensaje acorde: para las excepciones del
     * dominio se usa su mensaje; para el resto, un mensaje generico.
     */
    public static void mostrarExcepcion(RuntimeException e) {
        String mensaje;
        if (e instanceof ValidacionException || e instanceof ReglaNegocioException
                || e instanceof EntidadNoEncontradaException || e instanceof PersistenciaException) {
            mensaje = e.getMessage();
        } else {
            mensaje = "Ocurrio un error inesperado: " + e.getMessage();
        }
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(mensaje);
        alerta.showAndWait();
    }
}
