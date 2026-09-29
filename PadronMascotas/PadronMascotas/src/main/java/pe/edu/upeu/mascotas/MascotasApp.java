package pe.edu.upeu.mascotas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pe.edu.upeu.mascotas.controller.MascotaController;
import pe.edu.upeu.mascotas.repository.IMascotaRepository;
import pe.edu.upeu.mascotas.repository.MascotaRepository;
import pe.edu.upeu.mascotas.service.IMascotaService;
import pe.edu.upeu.mascotas.service.MascotaService;

public class MascotasApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        IMascotaRepository repositorio = new MascotaRepository();
        IMascotaService servicio = new MascotaService(repositorio);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mascotas.fxml"));
        loader.setControllerFactory(tipo -> new MascotaController(servicio));

        stage.setTitle("Padrón de Animales Domésticos");
        stage.setScene(new Scene(loader.load(), 1100, 600));
        stage.show();
    }
}
