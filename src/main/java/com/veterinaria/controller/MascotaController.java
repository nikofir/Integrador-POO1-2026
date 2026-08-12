package com.veterinaria.controller;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.MascotaDto;
import com.veterinaria.entity.Especie;
import com.veterinaria.service.ClienteService;
import com.veterinaria.service.MascotaService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Vista de gestion de mascotas: alta con ficha automatica, modificacion,
 * baja logica (activa/inactiva) y filtrado por cliente.
 */
public class MascotaController {

    private final MascotaService servicio = new MascotaService();
    private final ClienteService servicioClientes = new ClienteService();

    @FXML
    private ComboBox<ClienteDto> comboCliente;
    @FXML
    private TableView<MascotaDto> tabla;
    @FXML
    private TableColumn<MascotaDto, String> colFicha;
    @FXML
    private TableColumn<MascotaDto, String> colEspecie;
    @FXML
    private TableColumn<MascotaDto, String> colNombre;
    @FXML
    private TableColumn<MascotaDto, String> colRaza;
    @FXML
    private TableColumn<MascotaDto, Long> colEdad;
    @FXML
    private TableColumn<MascotaDto, String> colCliente;
    @FXML
    private TableColumn<MascotaDto, Boolean> colActiva;

    private final ObservableList<MascotaDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colFicha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ficha()));
        colEspecie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().especie()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colRaza.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().raza()));
        colEdad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().edadAnios()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().clienteNombreCompleto()));
        colActiva.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().activa()));
        colActiva.setCellFactory(CheckBoxTableCell.forTableColumn(colActiva));

        comboCliente.getItems().setAll(servicioClientes.listar());
        comboCliente.setOnAction(e -> cargarTabla());
        cargarTabla();
    }

    @FXML
    private void nuevaMascota() {
        dialogMascota(null).ifPresent(datos -> {
            try {
                servicio.registrar(datos.clienteId(), Especie.porEtiqueta(datos.especie()), datos.nombre(),
                        datos.raza(), datos.fechaNacimiento());
                recargar();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void modificarMascota() {
        MascotaDto seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        dialogMascota(seleccionada).ifPresent(datos -> {
            try {
                servicio.actualizar(seleccionada.id(), Especie.porEtiqueta(datos.especie()), datos.nombre(),
                        datos.raza(), datos.fechaNacimiento());
                recargar();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void alternarEstado() {
        MascotaDto seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        try {
            if (seleccionada.activa()) {
                servicio.marcarInactiva(seleccionada.id());
            } else {
                servicio.reactivar(seleccionada.id());
            }
            recargar();
        } catch (RuntimeException e) {
            Alertas.mostrarExcepcion(e);
        }
    }

    @FXML
    private void verHistorial() {
        MascotaDto seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/vistas/historial.fxml"));
            javafx.scene.Node vista = cargador.load();
            HistorialController control = cargador.getController();
            control.mostrarHistorialDe(seleccionada);
            StackPane contenedor = (StackPane) tabla.getScene().lookup("#contenido");
            contenedor.getChildren().clear();
            contenedor.getChildren().add(vista);
        } catch (IOException e) {
            Alertas.error("Error de interfaz", "No se pudo abrir el historial medico.");
        }
    }

    @FXML
    private void recargar() {
        cargarTabla();
    }

    private Optional<MascotaDto> dialogMascota(MascotaDto existente) {
        Dialog<MascotaDto> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nueva mascota" : "Modificar mascota");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<ClienteDto> cliente = new ComboBox<>(FXCollections.observableArrayList(servicioClientes.listar()));
        cliente.setCellFactory(c -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ClienteDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.nombre() + " " + dto.apellido() + " (DNI " + dto.dni() + ")");
            }
        });
        cliente.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ClienteDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.nombre() + " " + dto.apellido() + " (DNI " + dto.dni() + ")");
            }
        });
        if (existente != null) {
            List<ClienteDto> clientes = servicioClientes.listar();
            clientes.stream()
                    .filter(c -> c.id().equals(existente.clienteId()))
                    .findFirst()
                    .ifPresent(cliente::setValue);
            cliente.setDisable(true);
        } else {
            cliente.setValue(cliente.getItems().isEmpty() ? null : cliente.getItems().get(0));
        }

        ComboBox<String> especie = new ComboBox<>(FXCollections.observableArrayList(
                java.util.Arrays.stream(Especie.values()).map(Especie::getEtiqueta).toList()));
        especie.setValue(existente == null ? "Perro" : existente.especie());
        TextField nombre = new TextField(existente == null ? "" : existente.nombre());
        TextField raza = new TextField(existente == null ? "" : existente.raza());
        DatePicker fechaNacimiento = new DatePicker(existente == null
                ? LocalDate.now().minusYears(1) : existente.fechaNacimiento());

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Cliente:"), cliente);
        grilla.addRow(1, new Label("Especie:"), especie);
        grilla.addRow(2, new Label("Nombre:"), nombre);
        grilla.addRow(3, new Label("Raza:"), raza);
        grilla.addRow(4, new Label("Nacimiento:"), fechaNacimiento);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                ClienteDto clienteElegido = cliente.getValue();
                Especie especieElegida = Especie.porEtiqueta(especie.getValue());
                if (clienteElegido == null || especieElegida == null) {
                    return null;
                }
                return new MascotaDto(null, null, especieElegida.getEtiqueta(), nombre.getText().trim(),
                        raza.getText().trim(), fechaNacimiento.getValue(), true, 0,
                        clienteElegido.id(), null);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private void cargarTabla() {
        List<MascotaDto> mascotas = comboCliente.getValue() == null
                ? servicio.listarActivas()
                : servicio.listarPorCliente(comboCliente.getValue().id());
        datos.setAll(mascotas);
        tabla.setItems(datos);
    }
}
