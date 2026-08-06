package com.veterinaria.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controlador principal: menu lateral de navegacion que carga las vistas
 * de cada modulo en el area central.
 */
public class MainController {

    @FXML
    private StackPane contenido;

    @FXML
    private void mostrarClientes() {
        cargarVista("clientes.fxml");
    }

    @FXML
    private void mostrarMascotas() {
        cargarVista("mascotas.fxml");
    }

    @FXML
    private void mostrarVeterinarios() {
        cargarVista("veterinarios.fxml");
    }

    @FXML
    private void mostrarServicios() {
        cargarVista("servicios.fxml");
    }

    @FXML
    private void mostrarVacunas() {
        cargarVista("vacunas.fxml");
    }

    @FXML
    private void mostrarTurnos() {
        cargarVista("turnos.fxml");
    }

    @FXML
    private void mostrarIngresos() {
        cargarVista("ingresos.fxml");
    }

    private void cargarVista(String nombre) {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/vistas/" + nombre));
            contenido.getChildren().clear();
            contenido.getChildren().add(cargador.load());
        } catch (IOException e) {
            Alertas.error("Error de interfaz", "No se pudo cargar la vista " + nombre + ".");
        }
    }
}
