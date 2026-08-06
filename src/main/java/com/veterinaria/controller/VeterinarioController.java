package com.veterinaria.controller;

import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.entity.Especialidad;
import com.veterinaria.service.VeterinarioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista de gestion de veterinarios: alta con matricula unica, modificacion
 * de datos y especialidades, y baja condicionada a turnos activos.
 */
public class VeterinarioController {

    private final VeterinarioService servicio = new VeterinarioService();

    @FXML
    private TableView<VeterinarioDto> tabla;
    @FXML
    private TableColumn<VeterinarioDto, String> colMatricula;
    @FXML
    private TableColumn<VeterinarioDto, String> colNombre;
    @FXML
    private TableColumn<VeterinarioDto, String> colApellido;
    @FXML
    private TableColumn<VeterinarioDto, String> colEspecialidades;

    private final ObservableList<VeterinarioDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colMatricula.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().matricula()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colApellido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().apellido()));
        colEspecialidades.setCellValueFactory(c -> new SimpleStringProperty(
                String.join(", ", c.getValue().especialidades())));
        cargarTabla();
    }

    @FXML
    private void nuevoVeterinario() {
        dialogVeterinario(null).ifPresent(datos -> {
            try {
                servicio.registrar(datos);
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void modificarVeterinario() {
        VeterinarioDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        dialogVeterinario(seleccionado).ifPresent(datos -> {
            try {
                servicio.actualizar(seleccionado.id(), datos);
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void eliminarVeterinario() {        VeterinarioDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al veterinario " + seleccionado.nombre() + " " + seleccionado.apellido() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Confirmar baja");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                servicio.eliminar(seleccionado.id());
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        }
    }

    @FXML
    private void recargar() {
        cargarTabla();
    }

    private Optional<VeterinarioDto> dialogVeterinario(VeterinarioDto existente) {
        Dialog<VeterinarioDto> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo veterinario" : "Modificar veterinario");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField matricula = new TextField(existente == null ? "" : existente.matricula());
        matricula.setDisable(existente != null);
        TextField nombre = new TextField(existente == null ? "" : existente.nombre());
        TextField apellido = new TextField(existente == null ? "" : existente.apellido());

        Map<Especialidad, CheckBox> casillas = new HashMap<>();
        VBox especialidadesBox = new VBox(4);
        for (Especialidad especialidad : Especialidad.values()) {
            CheckBox casilla = new CheckBox(especialidad.getEtiqueta());
            if (existente != null && existente.especialidades().contains(especialidad.getEtiqueta())) {
                casilla.setSelected(true);
            }
            casillas.put(especialidad, casilla);
            especialidadesBox.getChildren().add(casilla);
        }

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Matricula:"), matricula);
        grilla.addRow(1, new Label("Nombre:"), nombre);
        grilla.addRow(2, new Label("Apellido:"), apellido);
        grilla.addRow(3, new Label("Especialidades:"), especialidadesBox);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                Set<String> elegidas = casillas.entrySet().stream()
                        .filter(e -> e.getValue().isSelected())
                        .map(e -> e.getKey().getEtiqueta())
                        .collect(Collectors.toSet());
                return new VeterinarioDto(existente == null ? null : existente.id(),
                        matricula.getText().trim(), nombre.getText().trim(), apellido.getText().trim(), elegidas);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private void cargarTabla() {
        datos.setAll(servicio.listar());
        tabla.setItems(datos);
    }
}
