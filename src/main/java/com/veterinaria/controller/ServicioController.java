package com.veterinaria.controller;

import com.veterinaria.dto.ServicioDto;
import com.veterinaria.dto.VacunaDto;
import com.veterinaria.service.ServicioService;
import com.veterinaria.service.VacunaService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Vista del catalogo de servicios: alta de cada tipo (consulta, peluqueria,
 * guarderia y aplicacion de vacuna), modificacion de precio y listado.
 */
public class ServicioController {

    private final ServicioService servicio = new ServicioService();
    private final VacunaService servicioVacunas = new VacunaService();

    @FXML
    private TableView<ServicioDto> tabla;
    @FXML
    private TableColumn<ServicioDto, String> colTipo;
    @FXML
    private TableColumn<ServicioDto, String> colNombre;
    @FXML
    private TableColumn<ServicioDto, BigDecimal> colPrecio;
    @FXML
    private TableColumn<ServicioDto, Integer> colDuracion;
    @FXML
    private TableColumn<ServicioDto, String> colExtra;

    private final ObservableList<ServicioDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colPrecio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().precioBase()));
        colDuracion.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().duracionMinutos()));
        colExtra.setCellValueFactory(c -> new SimpleStringProperty(extra(c.getValue())));
        cargarTabla();
    }

    private String extra(ServicioDto dto) {
        if (dto.cupoMaximo() != null) {
            return "Cupo diario: " + dto.cupoMaximo();
        }
        if (dto.vacunaNombre() != null) {
            return "Vacuna: " + dto.vacunaNombre();
        }
        return "";
    }

    @FXML
    private void nuevoServicio() {
        dialogServicio(null).ifPresent(datos -> {
            try {
                switch (datos.tipo()) {
                    case "Consulta medica" -> servicio.crearConsultaMedica(datos.nombre(),
                            datos.precioBase(), datos.duracionMinutos());
                    case "Peluqueria" -> servicio.crearPeluqueria(datos.nombre(),
                            datos.precioBase(), datos.duracionMinutos());
                    case "Guarderia por dia" -> servicio.crearGuarderiaDia(datos.nombre(),
                            datos.precioBase(), datos.duracionMinutos(), datos.cupoMaximo() == null ? 1 : datos.cupoMaximo());
                    case "Aplicacion de vacuna" -> {
                        Long vacunaId = elegirVacuna(datos.vacunaNombre());
                        servicio.crearAplicacionVacuna(datos.nombre(), datos.precioBase(),
                                datos.duracionMinutos(), vacunaId);
                    }
                    default -> throw new IllegalArgumentException("Tipo no soportado.");
                }
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void modificarServicio() {
        ServicioDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        Dialog<ServicioDto> dialog = new Dialog<>();
        dialog.setTitle("Modificar servicio");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField nombre = new TextField(seleccionado.nombre());
        TextField precio = new TextField(seleccionado.precioBase().toPlainString());
        TextField duracion = new TextField(String.valueOf(seleccionado.duracionMinutos()));
        TextField cupo = new TextField(seleccionado.cupoMaximo() == null ? "" : String.valueOf(seleccionado.cupoMaximo()));

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Nombre:"), nombre);
        grilla.addRow(1, new Label("Precio base:"), precio);
        grilla.addRow(2, new Label("Duracion (min):"), duracion);
        if (seleccionado.cupoMaximo() != null) {
            grilla.addRow(3, new Label("Cupo diario:"), cupo);
        }
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                return new ServicioDto(seleccionado.id(), seleccionado.tipo(), nombre.getText().trim(),
                        new BigDecimal(precio.getText().trim()), Integer.parseInt(duracion.getText().trim()),
                        cupo.getText().isBlank() ? null : Integer.parseInt(cupo.getText().trim()),
                        seleccionado.vacunaNombre());
            }
            return null;
        });
        dialog.showAndWait().ifPresent(datos -> {
            try {
                servicio.actualizar(seleccionado.id(), datos.nombre(), datos.precioBase(), datos.duracionMinutos());
                if (datos.cupoMaximo() != null) {
                    servicio.actualizarCupoGuarderia(seleccionado.id(), datos.cupoMaximo());
                }
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    private Long elegirVacuna(String nombreActual) {
        List<VacunaDto> vacunas = servicioVacunas.listar();
        if (vacunas.isEmpty()) {
            throw new RuntimeException("No hay vacunas registradas. Cree una desde el menu Vacunas.");
        }
        ComboBox<VacunaDto> combo = new ComboBox<>(FXCollections.observableArrayList(vacunas));
        combo.setValue(vacunas.stream()
                .filter(v -> v.nombreComercial().equals(nombreActual))
                .findFirst().orElse(vacunas.get(0)));

        Dialog<VacunaDto> dialog = new Dialog<>();
        dialog.setTitle("Vacuna para el servicio");
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(aceptar, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(combo);
        dialog.setResultConverter(boton -> boton == aceptar ? combo.getValue() : null);
        VacunaDto elegida = dialog.showAndWait().orElseThrow(() -> new RuntimeException("Debe elegir una vacuna."));
        return elegida.id();
    }

    private Optional<ServicioDto> dialogServicio(ServicioDto existente) {
        Dialog<ServicioDto> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo servicio" : "Modificar servicio");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList(
                "Consulta medica", "Peluqueria", "Guarderia por dia", "Aplicacion de vacuna"));
        TextField nombre = new TextField();
        TextField precio = new TextField();
        TextField duracion = new TextField();
        ComboBox<String> vacuna = new ComboBox<>(FXCollections.observableArrayList(
                servicioVacunas.listar().stream().map(VacunaDto::nombreComercial).toList()));
        TextField cupo = new TextField("10");

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Tipo:"), tipo);
        grilla.addRow(1, new Label("Nombre:"), nombre);
        grilla.addRow(2, new Label("Precio base:"), precio);
        grilla.addRow(3, new Label("Duracion (min):"), duracion);
        grilla.addRow(4, new Label("Vacuna:"), vacuna);
        grilla.addRow(5, new Label("Cupo diario (guarderia):"), cupo);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar) {
                return new ServicioDto(null, tipo.getValue(), nombre.getText().trim(),
                        new BigDecimal(precio.getText().trim()), Integer.parseInt(duracion.getText().trim()),
                        cupo.getText().isBlank() ? null : Integer.parseInt(cupo.getText().trim()),
                        vacuna.getValue());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    @FXML
    private void recargar() {
        cargarTabla();
    }

    private void cargarTabla() {
        datos.setAll(servicio.listarCatalogo());
        tabla.setItems(datos);
    }
}
