package com.veterinaria.controller;

import com.veterinaria.dto.MascotaDto;
import com.veterinaria.dto.RegistroMedicoDto;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.dto.TurnoServicioDto;
import com.veterinaria.service.MascotaService;
import com.veterinaria.service.TurnoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista de historial medico: permite buscar una mascota (por ficha o nombre)
 * y ver sus turnos atendidos ordenados cronologicamente, con los servicios
 * recibidos, el veterinario que atendio, el diagnostico y las vacunas.
 */
public class HistorialController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SIN_REGISTRO = "-";
    private static final List<String> TIPOS_SERVICIO = List.of(
            "Consulta medica", "Peluqueria", "Guarderia por dia", "Aplicacion de vacuna");

    private final MascotaService servicioMascotas = new MascotaService();
    private final TurnoService servicioTurnos = new TurnoService();

    @FXML
    private ComboBox<MascotaDto> comboMascota;
    @FXML
    private TextField campoBusqueda;
    @FXML
    private Label etiquetaMascota;
    @FXML
    private ComboBox<String> comboTipo;
    @FXML
    private DatePicker fechaDesde;
    @FXML
    private DatePicker fechaHasta;
    @FXML
    private TableView<TurnoDto> tabla;
    @FXML
    private TableColumn<TurnoDto, String> colFecha;
    @FXML
    private TableColumn<TurnoDto, String> colVeterinario;
    @FXML
    private TableColumn<TurnoDto, String> colServicios;
    @FXML
    private TableColumn<TurnoDto, String> colVacunas;
    @FXML
    private TableColumn<TurnoDto, String> colDiagnostico;
    @FXML
    private TableColumn<TurnoDto, String> colTratamiento;

    private final ObservableList<TurnoDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fechaHora().format(FECHA)));
        colVeterinario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().veterinarioNombreCompleto()));
        colServicios.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().turnoServicios().stream().map(TurnoServicioDto::servicioNombre)
                        .collect(Collectors.joining(", "))));
        colVacunas.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().turnoServicios().stream()
                        .filter(ts -> "Aplicacion de vacuna".equals(ts.servicioTipo()))
                        .map(TurnoServicioDto::servicioNombre)
                        .collect(Collectors.joining(", "))));
        colDiagnostico.setCellValueFactory(c -> new SimpleStringProperty(diagnosticos(c.getValue())));
        colTratamiento.setCellValueFactory(c -> new SimpleStringProperty(tratamientos(c.getValue())));

        comboTipo.getItems().add("Todos");
        comboTipo.getItems().addAll(TIPOS_SERVICIO);
        comboTipo.setValue("Todos");

        comboMascota.setItems(FXCollections.observableArrayList(servicioMascotas.listarTodas()));
        comboMascota.setCellFactory(c -> celda());
        comboMascota.setButtonCell(celda());
        comboMascota.setOnAction(e -> consultar());
        campoBusqueda.setOnAction(e -> buscar());
    }

    /**
     * Muestra el historial de una mascota especifica. Lo invoca la vista de
     * mascotas al pulsar "Ver historial".
     */
    public void mostrarHistorialDe(MascotaDto mascota) {
        comboMascota.setValue(mascota);
        consultar();
    }

    @FXML
    private void buscar() {
        String texto = campoBusqueda.getText() == null ? "" : campoBusqueda.getText().trim();
        if (texto.isBlank()) {
            return;
        }
        String criterio = texto.toLowerCase();
        MascotaDto encontrada = comboMascota.getItems().stream()
                .filter(m -> m.ficha().equalsIgnoreCase(texto)
                        || m.nombre().toLowerCase().contains(criterio))
                .findFirst()
                .orElse(null);
        if (encontrada == null) {
            Alertas.informacion("Sin resultados", "No se encontro una mascota con ese criterio.");
            return;
        }
        comboMascota.setValue(encontrada);
        consultar();
    }

    private void consultar() {
        MascotaDto mascota = comboMascota.getValue();
        if (mascota == null) {
            etiquetaMascota.setText("Seleccione una mascota para ver su historial.");
            datos.clear();
            tabla.setItems(datos);
            return;
        }
        etiquetaMascota.setText("Historial de " + mascota.nombre()
                + " (" + mascota.ficha() + " - " + mascota.clienteNombreCompleto() + ")");
        String tipo = "Todos".equals(comboTipo.getValue()) ? null : comboTipo.getValue();
        datos.setAll(servicioTurnos.listarHistorial(mascota.id(), tipo,
                fechaDesde.getValue(), fechaHasta.getValue()));
        tabla.setItems(datos);
    }

    @FXML
    private void filtrar() {
        consultar();
    }

    @FXML
    private void limpiarFiltros() {
        comboTipo.setValue("Todos");
        fechaDesde.setValue(null);
        fechaHasta.setValue(null);
        consultar();
    }

    private String diagnosticos(TurnoDto turno) {
        List<String> diagnosticos = registros(turno).stream()
                .map(RegistroMedicoDto::diagnostico).distinct().toList();
        return diagnosticos.isEmpty() ? SIN_REGISTRO : String.join(" | ", diagnosticos);
    }

    private String tratamientos(TurnoDto turno) {
        List<String> tratamientos = registros(turno).stream()
                .map(RegistroMedicoDto::tratamiento).distinct().toList();
        return tratamientos.isEmpty() ? SIN_REGISTRO : String.join(" | ", tratamientos);
    }

    private List<RegistroMedicoDto> registros(TurnoDto turno) {
        return turno.turnoServicios().stream()
                .map(TurnoServicioDto::registroMedico)
                .filter(r -> r != null)
                .toList();
    }

    private ListCell<MascotaDto> celda() {
        return new ListCell<>() {
            @Override
            protected void updateItem(MascotaDto mascota, boolean vacio) {
                super.updateItem(mascota, vacio);
                setText(mascota == null ? null
                        : mascota.ficha() + " - " + mascota.nombre()
                        + " (" + mascota.especie() + ", " + mascota.clienteNombreCompleto() + ")");
            }
        };
    }
}
