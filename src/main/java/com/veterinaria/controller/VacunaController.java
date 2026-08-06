package com.veterinaria.controller;

import com.veterinaria.dto.VacunaDto;
import com.veterinaria.service.VacunaService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Vista de gestion de vacunas: alta con nombre comercial unico y listado.
 */
public class VacunaController {

    private final VacunaService servicio = new VacunaService();

    @FXML
    private TableView<VacunaDto> tabla;
    @FXML
    private TableColumn<VacunaDto, String> colNombreComercial;
    @FXML
    private TableColumn<VacunaDto, String> colEnfermedad;
    @FXML
    private TableColumn<VacunaDto, Integer> colPeriodicidad;

    private final ObservableList<VacunaDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colNombreComercial.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreComercial()));
        colEnfermedad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().enfermedadPrevenida()));
        colPeriodicidad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().periodicidadMeses()).asObject());
        cargarTabla();
    }

    @FXML
    private void nuevaVacuna() {
        Dialog<VacunaDto> dialog = new Dialog<>();
        dialog.setTitle("Nueva vacuna");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField nombre = new TextField();
        TextField enfermedad = new TextField();
        Spinner<Integer> periodicidad = new Spinner<>(1, 120, 12);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Nombre comercial:"), nombre);
        grilla.addRow(1, new Label("Enfermedad prevenida:"), enfermedad);
        grilla.addRow(2, new Label("Periodicidad (meses):"), periodicidad);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                return new VacunaDto(null, nombre.getText().trim(), enfermedad.getText().trim(),
                        periodicidad.getValue());
            }
            return null;
        });
        Optional<VacunaDto> resultado = dialog.showAndWait();
        resultado.ifPresent(datos -> {
            try {
                servicio.registrar(datos);
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void recargar() {
        cargarTabla();
    }

    private void cargarTabla() {
        datos.setAll(servicio.listar());
        tabla.setItems(datos);
    }
}
