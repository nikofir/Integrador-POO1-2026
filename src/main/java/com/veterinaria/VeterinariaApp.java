package com.veterinaria;

import com.veterinaria.util.JpaUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion de gestion veterinaria.
 * Carga la ventana principal (FXML) y cierra la capa de persistencia al salir.
 */
public class VeterinariaApp extends Application {

    @Override
    public void start(Stage escenario) throws Exception {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/vistas/principal.fxml"));
        Parent raiz = cargador.load();
        Scene escena = new Scene(raiz, 1180, 760);
        escena.getStylesheets().add(getClass().getResource("/vistas/estilo.css").toExternalForm());
        escenario.setTitle("Sistema de Gestion Veterinaria");
        escenario.setScene(escena);
        escenario.show();
    }

    @Override
    public void stop() {
        JpaUtil.cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
