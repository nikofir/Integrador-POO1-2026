package com.veterinaria.controller;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.MascotaDto;
import com.veterinaria.dto.ServicioDto;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.dto.TurnoServicioDto;
import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.service.ClienteService;
import com.veterinaria.service.MascotaService;
import com.veterinaria.service.ServicioService;
import com.veterinaria.service.TurnoService;
import com.veterinaria.service.VeterinarioService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista de agenda de turnos: listado por dia, creacion con reglas de agenda,
 * ciclo de vida (confirmar/atender/cancelar) y registro de consultas.
 */
public class TurnoController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TurnoService servicio = new TurnoService();
    private final ClienteService servicioClientes = new ClienteService();
    private final MascotaService servicioMascotas = new MascotaService();
    private final VeterinarioService servicioVeterinarios = new VeterinarioService();
    private final ServicioService servicioServicios = new ServicioService();

    @FXML
    private DatePicker campoDia;
    @FXML
    private TableView<TurnoDto> tabla;
    @FXML
    private TableColumn<TurnoDto, String> colHora;
    @FXML
    private TableColumn<TurnoDto, String> colMascota;
    @FXML
    private TableColumn<TurnoDto, String> colCliente;
    @FXML
    private TableColumn<TurnoDto, String> colVeterinario;
    @FXML
    private TableColumn<TurnoDto, String> colServicios;
    @FXML
    private TableColumn<TurnoDto, String> colEstado;
    @FXML
    private TableColumn<TurnoDto, BigDecimal> colTotal;

    private final ObservableList<TurnoDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colHora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fechaHora().format(HORA)));
        colMascota.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().mascotaFicha() + " - " + c.getValue().mascotaNombre()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().clienteNombreCompleto()));
        colVeterinario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().veterinarioNombreCompleto()));
        colServicios.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().turnoServicios().stream().map(TurnoServicioDto::servicioNombre)
                        .collect(Collectors.joining(", "))));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado()));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().precioTotal()));

        campoDia.setValue(LocalDate.now());
        campoDia.setOnAction(e -> cargarTabla());
        cargarTabla();
    }

    @FXML
    private void nuevoTurno() {
        List<MascotaDto> mascotas = servicioMascotas.listarActivas();
        List<VeterinarioDto> veterinarios = servicioVeterinarios.listar();
        List<ServicioDto> servicios = servicioServicios.listarCatalogo();
        if (mascotas.isEmpty() || veterinarios.isEmpty() || servicios.isEmpty()) {
            Alertas.error("Nuevo turno", "Se necesita al menos una mascota activa, un veterinario y un servicio.");
            return;
        }

        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Nuevo turno");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<MascotaDto> mascota = new ComboBox<>(FXCollections.observableArrayList(mascotas));
        mascota.setCellFactory(c -> etiquetaMascota());
        mascota.setButtonCell(etiquetaMascota());
        mascota.setValue(mascotas.get(0));

        ComboBox<VeterinarioDto> veterinario = new ComboBox<>(FXCollections.observableArrayList(veterinarios));
        veterinario.setCellFactory(c -> etiquetaVeterinario());
        veterinario.setButtonCell(etiquetaVeterinario());
        veterinario.setValue(veterinarios.get(0));

        ListView<ServicioDto> listaServicios = new ListView<>(FXCollections.observableArrayList(servicios));
        listaServicios.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        listaServicios.setCellFactory(c -> etiquetaServicio());

        DatePicker dia = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> hora = new Spinner<>(0, 23, 10);
        Spinner<Integer> minuto = new Spinner<>(0, 55, 0, 5);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Mascota:"), mascota);
        grilla.addRow(1, new Label("Veterinario:"), veterinario);
        grilla.addRow(2, new Label("Fecha:"), dia);
        grilla.addRow(3, new Label("Hora:"), new javafx.scene.layout.HBox(6, hora, minuto));
        grilla.addRow(4, new Label("Servicios (Ctrl+click):"), listaServicios);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> {
            if (boton == guardar && dia.getValue() != null
                    && !listaServicios.getSelectionModel().getSelectedItems().isEmpty()) {
                return new Object[]{
                        mascota.getValue().id(),
                        veterinario.getValue().id(),
                        listaServicios.getSelectionModel().getSelectedItems().stream()
                                .map(ServicioDto::id).toList()
                };
            }
            return null;
        });

        Optional<Object[]> resultado = dialog.showAndWait();
        resultado.ifPresent(sel -> {
            try {
                Long mascotaId = (Long) sel[0];
                Long veterinarioId = (Long) sel[1];
                @SuppressWarnings("unchecked")
                List<Long> ids = (List<Long>) sel[2];
                LocalDateTime fecha = LocalDateTime.of(dia.getValue(),
                        java.time.LocalTime.of(hora.getValue(), minuto.getValue()));
                servicio.crearTurno(mascotaId, veterinarioId, fecha, ids);
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void confirmarTurno() {
        accionSobreTurno("confirmar", "Confirmado");
    }

    @FXML
    private void atenderTurno() {
        accionSobreTurno("atender", "Atendido");
    }

    @FXML
    private void cancelarTurno() {
        accionSobreTurno("cancelar", "Cancelado");
    }

    private void accionSobreTurno(String accion, String confirmacionMensaje) {
        TurnoDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        try {
            switch (accion) {
                case "confirmar" -> servicio.confirmar(seleccionado.id());
                case "atender" -> servicio.atender(seleccionado.id());
                case "cancelar" -> servicio.cancelar(seleccionado.id(), LocalDateTime.now());
                default -> throw new IllegalArgumentException("Accion no soportada.");
            }
            Alertas.informacion("Turno", "Turno " + confirmacionMensaje.toLowerCase() + ".");
            cargarTabla();
        } catch (RuntimeException e) {
            Alertas.mostrarExcepcion(e);
        }
    }

    @FXML
    private void registrarConsulta() {
        TurnoDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        if (seleccionado.turnoServicios().isEmpty()) {
            Alertas.error("Registrar consulta", "El turno no tiene servicios.");
            return;
        }

        Dialog<TurnoServicioDto> dialog = new Dialog<>();
        dialog.setTitle("Registrar consulta");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<TurnoServicioDto> servicioTurno = new ComboBox<>(
                FXCollections.observableArrayList(seleccionado.turnoServicios()));
        servicioTurno.setCellFactory(c -> etiquetaTurnoServicio());
        servicioTurno.setButtonCell(etiquetaTurnoServicio());
        servicioTurno.setValue(seleccionado.turnoServicios().get(0));

        TextField diagnostico = new TextField();
        TextField tratamiento = new TextField();

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.addRow(0, new Label("Servicio:"), servicioTurno);
        grilla.addRow(1, new Label("Diagnostico:"), diagnostico);
        grilla.addRow(2, new Label("Tratamiento:"), tratamiento);
        dialog.getDialogPane().setContent(grilla);

        dialog.setResultConverter(boton -> boton == guardar ? servicioTurno.getValue() : null);
        Optional<TurnoServicioDto> resultado = dialog.showAndWait();
        resultado.ifPresent(ts -> {
            try {
                servicio.registrarConsulta(seleccionado.id(), ts.id(),
                        diagnostico.getText().trim(), tratamiento.getText().trim());
                Alertas.informacion("Consulta", "Consulta registrada para " + ts.servicioNombre() + ".");
                cargarTabla();
            } catch (RuntimeException e) {
                Alertas.mostrarExcepcion(e);
            }
        });
    }

    @FXML
    private void recargar() {
        campoDia.setValue(LocalDate.now());
        cargarTabla();
    }

    private ListCell<MascotaDto> etiquetaMascota() {
        return new ListCell<>() {
            @Override
            protected void updateItem(MascotaDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.ficha() + " - " + dto.nombre()
                        + " (" + dto.clienteNombreCompleto() + ")");
            }
        };
    }

    private ListCell<VeterinarioDto> etiquetaVeterinario() {
        return new ListCell<>() {
            @Override
            protected void updateItem(VeterinarioDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.nombre() + " " + dto.apellido() + " (Mat. " + dto.matricula() + ")");
            }
        };
    }

    private ListCell<ServicioDto> etiquetaServicio() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ServicioDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.nombre() + " - $" + dto.precioBase());
            }
        };
    }

    private ListCell<TurnoServicioDto> etiquetaTurnoServicio() {
        return new ListCell<>() {
            @Override
            protected void updateItem(TurnoServicioDto dto, boolean vacio) {
                super.updateItem(dto, vacio);
                setText(dto == null ? null : dto.servicioNombre() + " - $" + dto.precioHistorico());
            }
        };
    }

    private void cargarTabla() {
        if (campoDia.getValue() == null) {
            return;
        }
        datos.setAll(servicio.listarPorDia(campoDia.getValue()));
        tabla.setItems(datos);
    }
}
