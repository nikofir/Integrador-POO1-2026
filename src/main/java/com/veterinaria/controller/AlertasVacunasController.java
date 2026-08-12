package com.veterinaria.controller;

import com.veterinaria.dto.AlertaVacunaDto;
import com.veterinaria.service.VacunaAlertaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Control de vacunaciones: lista las mascotas con vacunas vencidas o proximas
 * a vencer dentro de los proximos 30 dias, para poder reprogramar la dosis.
 */
public class AlertasVacunasController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VacunaAlertaService servicio = new VacunaAlertaService();

    @FXML
    private Label resumen;
    @FXML
    private TableView<AlertaVacunaDto> tabla;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colMascota;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colCliente;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colVacuna;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colEnfermedad;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colUltima;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colProxima;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colEstado;
    @FXML
    private TableColumn<AlertaVacunaDto, String> colDias;

    private final ObservableList<AlertaVacunaDto> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colMascota.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().mascotaFicha() + " - " + c.getValue().mascotaNombre()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().clienteNombre()));
        colVacuna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vacunaNombre()));
        colEnfermedad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().enfermedadPrevenida()));
        colUltima.setCellValueFactory(c -> new SimpleStringProperty(fecha(c.getValue().ultimaAplicacion())));
        colProxima.setCellValueFactory(c -> new SimpleStringProperty(fecha(c.getValue().proximaAplicacion())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado().getEtiqueta()));
        colDias.setCellValueFactory(c -> new SimpleStringProperty(dias(c.getValue().diasRestantes())));
        recargar();
    }

    /** Recarga la lista de vacunas vencidas o a vencer. */
    @FXML
    public void recargar() {
        List<AlertaVacunaDto> alertas = servicio.listarAlertas();
        datos.setAll(alertas);
        tabla.setItems(datos);
        long vencidas = alertas.stream().filter(AlertaVacunaDto::esVencida).count();
        long proximas = alertas.size() - vencidas;
        resumen.setText("Vacunas vencidas: " + vencidas + " | A vencer en 30 dias: " + proximas);
    }

    /**
     * Lleva al modulo de turnos, donde se registra una nueva vacunacion creando
     * un turno con el servicio de aplicacion correspondiente.
     */
    @FXML
    private void irATurnos() {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/vistas/turnos.fxml"));
            Node vista = cargador.load();
            StackPane contenedor = (StackPane) tabla.getScene().lookup("#contenido");
            contenedor.getChildren().clear();
            contenedor.getChildren().add(vista);
        } catch (IOException e) {
            Alertas.error("Error de interfaz", "No se pudo abrir el modulo de turnos.");
        }
    }

    private String fecha(java.time.LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    private String dias(long dias) {
        if (dias < 0) {
            return "vence " + (-dias) + " dias atras";
        }
        return "en " + dias + " dias";
    }
}
