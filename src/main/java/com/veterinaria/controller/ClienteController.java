package com.veterinaria.controller;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.service.ClienteService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

/**
 * Vista de gestion de clientes: listado con busqueda, alta, modificacion y baja.
 */
public class ClienteController {

    private final ClienteService servicio = new ClienteService();

    @FXML
    private TextField campoBusqueda;
    @FXML
    private TableView<ClienteDto> tabla;
    @FXML
    private TableColumn<ClienteDto, String> colDni;
    @FXML
    private TableColumn<ClienteDto, String> colNombre;
    @FXML
    private TableColumn<ClienteDto, String> colApellido;
    @FXML
    private TableColumn<ClienteDto, String> colTelefono;
    @FXML
    private TableColumn<ClienteDto, String> colEmail;
    @FXML
    private TableColumn<ClienteDto, Integer> colMascotas;

    private final ObservableList<ClienteDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colDni.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dni()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colApellido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().apellido()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().telefono()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email()));
        colMascotas.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().cantidadMascotas()));
        cargarTabla();
    }

    @FXML
    private void buscar() {
        String criterio = campoBusqueda.getText() == null ? "" : campoBusqueda.getText().trim().toLowerCase();
        List<ClienteDto> todos = servicio.listar();
        List<ClienteDto> filtrados;
        if (criterio.isEmpty()) {
            filtrados = todos;
        } else {
            filtrados = todos.stream()
                    .filter(c -> c.dni().contains(criterio)
                            || c.nombre().toLowerCase().contains(criterio)
                            || c.apellido().toLowerCase().contains(criterio))
                    .toList();
        }
        datos.setAll(filtrados);
    }

    @FXML
    private void nuevoCliente() {
        dialogCliente(null).ifPresent(datos -> {
            try {
                servicio.registrar(datos);
                recargar();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void modificarCliente() {
        ClienteDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        dialogCliente(seleccionado).ifPresent(datos -> {
            try {
                servicio.actualizar(seleccionado.id(), datos);
                recargar();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void eliminarCliente() {
        ClienteDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al cliente " + seleccionado.nombre() + " " + seleccionado.apellido() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Confirmar baja");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                servicio.eliminar(seleccionado.id());
                recargar();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        }
    }

    private Optional<ClienteDto> dialogCliente(ClienteDto existente) {
        Dialog<ClienteDto> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo cliente" : "Modificar cliente");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField dni = new TextField(existente == null ? "" : existente.dni());
        dni.setDisable(existente != null);
        TextField nombre = new TextField(existente == null ? "" : existente.nombre());
        TextField apellido = new TextField(existente == null ? "" : existente.apellido());
        TextField telefono = new TextField(existente == null ? "" : existente.telefono());
        TextField email = new TextField(existente == null ? "" : existente.email());
        TextField domicilio = new TextField(existente == null || existente.domicilio() == null
                ? "" : existente.domicilio());

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("DNI:"), dni);
        grilla.addRow(1, new Label("Nombre:"), nombre);
        grilla.addRow(2, new Label("Apellido:"), apellido);
        grilla.addRow(3, new Label("Telefono:"), telefono);
        grilla.addRow(4, new Label("Email:"), email);
        grilla.addRow(5, new Label("Domicilio:"), domicilio);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                return new ClienteDto(existente == null ? null : existente.id(),
                        dni.getText().trim(), nombre.getText().trim(), apellido.getText().trim(),
                        telefono.getText().trim(), email.getText().trim(), domicilio.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private void recargar() {
        campoBusqueda.clear();
        cargarTabla();
    }

    private void cargarTabla() {
        datos.setAll(servicio.listar());
        tabla.setItems(datos);
    }
}
