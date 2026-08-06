package com.veterinaria.controller;

import com.veterinaria.service.TurnoService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.dto.TurnoServicioDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista de ingresos: suma el valor de los turnos atendidos en un rango de fechas
 * y lista los turnos que lo componen.
 */
public class IngresosController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TurnoService servicio = new TurnoService();

    @FXML
    private DatePicker campoDesde;
    @FXML
    private DatePicker campoHasta;
    @FXML
    private Label etiquetaTotal;
    @FXML
    private TableView<TurnoDto> tabla;
    @FXML
    private TableColumn<TurnoDto, String> colFecha;
    @FXML
    private TableColumn<TurnoDto, String> colMascota;
    @FXML
    private TableColumn<TurnoDto, String> colCliente;
    @FXML
    private TableColumn<TurnoDto, String> colServicios;
    @FXML
    private TableColumn<TurnoDto, BigDecimal> colTotal;

    private final ObservableList<TurnoDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fechaHora().format(FECHA)));
        colMascota.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().mascotaFicha() + " - " + c.getValue().mascotaNombre()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().clienteNombreCompleto()));
        colServicios.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().turnoServicios().stream().map(TurnoServicioDto::servicioNombre)
                        .collect(Collectors.joining(", "))));
        colTotal.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                c.getValue().precioTotal()));

        campoDesde.setValue(LocalDate.now().withDayOfMonth(1));
        campoHasta.setValue(LocalDate.now());
        calcular();
    }

    @FXML
    private void calcular() {
        if (campoDesde.getValue() == null || campoHasta.getValue() == null) {
            return;
        }
        LocalDateTime desde = LocalDateTime.of(campoDesde.getValue(), LocalTime.MIN);
        LocalDateTime hasta = LocalDateTime.of(campoHasta.getValue(), LocalTime.MAX);

        BigDecimal total = servicio.ingresosEntre(desde, hasta);
        etiquetaTotal.setText("Ingresos por turnos atendidos: $" + total.toPlainString());

        List<TurnoDto> turnos = servicio.listarTodos().stream()
                .filter(t -> "Atendido".equals(t.estado()))
                .filter(t -> !t.fechaHora().isBefore(desde) && !t.fechaHora().isAfter(hasta))
                .toList();
        datos.setAll(turnos);
        tabla.setItems(datos);
    }
}
