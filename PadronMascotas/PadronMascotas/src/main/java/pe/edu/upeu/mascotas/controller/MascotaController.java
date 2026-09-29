package pe.edu.upeu.mascotas.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.mascotas.model.Gato;
import pe.edu.upeu.mascotas.model.Mascota;
import pe.edu.upeu.mascotas.model.OtraMascota;
import pe.edu.upeu.mascotas.model.Perro;
import pe.edu.upeu.mascotas.service.IMascotaService;

import java.util.List;

public class MascotaController {

    private final IMascotaService servicio;

    @FXML private TextField txtNombre, txtRaza, txtEdad, txtPropietario, txtTelefono;
    @FXML private ComboBox<String> cbxEspecie, cbxColor, cbxSexo, cbxVacunacion;

    @FXML private ComboBox<String> cbxFiltroEspecie, cbxFiltroVacunacion;

    @FXML private Button btnGuardar, btnEliminar;
    @FXML private Label lblMensaje;

    @FXML private TableView<Mascota> tablaMascotas;
    @FXML private TableColumn<Mascota, Integer> colId, colEdad;
    @FXML private TableColumn<Mascota, String> colNombre, colEspecie, colRaza, colColor, colSexo,
            colPropietario, colTelefono, colVacunacion;

    private Mascota seleccionada = null;

    public MascotaController(IMascotaService servicio) {
        this.servicio = servicio;
    }

    @FXML
    public void initialize() {

        cbxEspecie.setItems(FXCollections.observableArrayList("Perro", "Gato", "Otro"));
        cbxColor.setItems(FXCollections.observableArrayList("Negro", "Plomo", "Blanco", "Amarillo", "Otro"));
        cbxSexo.setItems(FXCollections.observableArrayList("Macho", "Hembra"));
        cbxVacunacion.setItems(FXCollections.observableArrayList("Al día", "Pendiente"));
        cbxFiltroEspecie.setItems(FXCollections.observableArrayList("Todas", "Perro", "Gato", "Otro"));
        cbxFiltroVacunacion.setItems(FXCollections.observableArrayList("Todos", "Al día", "Pendiente"));
        cbxFiltroEspecie.setValue("Todas");
        cbxFiltroVacunacion.setValue("Todos");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEspecie.setCellValueFactory(new PropertyValueFactory<>("especie"));
        colRaza.setCellValueFactory(new PropertyValueFactory<>("raza"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colPropietario.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colVacunacion.setCellValueFactory(new PropertyValueFactory<>("estadoVacunacion"));

        tablaMascotas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null) {
                cargarFormulario(nueva);
            }
        });

        limpiarFormulario();
        listar();
    }

    @FXML
    private void onGuardar() {
        try {
            Mascota mascota = leerFormulario();
            if (seleccionada == null) {
                servicio.guardar(mascota);
                mostrarMensaje("Mascota registrada. Hace: " + mascota.hacerSonido(), false);
            } else {
                mascota.setId(seleccionada.getId());
                servicio.actualizar(mascota);
                mostrarMensaje("Mascota actualizada correctamente.", false);
            }
            limpiarFormulario();
            listar();
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        lblMensaje.setText("");
    }

    @FXML
    private void onEliminar() {
        if (seleccionada == null) {
            mostrarMensaje("Seleccione una mascota de la tabla.", true);
            return;
        }
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea eliminar a " + seleccionada.getNombre() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        alerta.setHeaderText(null);
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                servicio.eliminar(seleccionada.getId());
                mostrarMensaje("Mascota eliminada.", false);
                limpiarFormulario();
                listar();
            }
        });
    }

    @FXML
    private void onFiltrar() {
        listar();
    }

    private void listar() {
        List<Mascota> lista = servicio.filtrar(cbxFiltroEspecie.getValue(), cbxFiltroVacunacion.getValue());
        tablaMascotas.setItems(FXCollections.observableArrayList(lista));
    }

    private Mascota leerFormulario() {
        String especie = cbxEspecie.getValue();
        if (especie == null) {
            throw new IllegalArgumentException("Seleccione la especie.");
        }
        if (cbxVacunacion.getValue() == null) {
            throw new IllegalArgumentException("Seleccione el estado de vacunación.");
        }

        int edad;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La edad debe ser un número entero.");
        }

        String nombre = txtNombre.getText();
        String raza = txtRaza.getText();
        String color = cbxColor.getValue();
        String sexo = cbxSexo.getValue();
        String propietario = txtPropietario.getText();
        String telefono = txtTelefono.getText();
        boolean vacunado = cbxVacunacion.getValue().equals("Al día");

        if (especie.equals("Perro")) {
            return new Perro(nombre, raza, color, sexo, edad, propietario, telefono, vacunado);
        }
        else if (especie.equals("Gato")) {
            return new Gato(nombre, raza, color, sexo, edad, propietario, telefono, vacunado);
        }
        else {
            return new OtraMascota(nombre, raza, color, sexo, edad, propietario, telefono, vacunado);
        }
    }

    private void cargarFormulario(Mascota m) {
        seleccionada = m;
        txtNombre.setText(m.getNombre());
        cbxEspecie.setValue(m.getEspecie());
        txtRaza.setText(m.getRaza());
        cbxColor.setValue(m.getColor());
        cbxSexo.setValue(m.getSexo());
        txtEdad.setText(String.valueOf(m.getEdad()));
        txtPropietario.setText(m.getPropietario());
        txtTelefono.setText(m.getTelefono());
        cbxVacunacion.setValue(m.getEstadoVacunacion());
        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
    }

    private void limpiarFormulario() {
        seleccionada = null;
        tablaMascotas.getSelectionModel().clearSelection();
        txtNombre.clear();
        cbxEspecie.setValue(null);
        txtRaza.clear();
        cbxColor.setValue(null);
        cbxSexo.setValue(null);
        txtEdad.clear();
        txtPropietario.clear();
        txtTelefono.clear();
        cbxVacunacion.setValue(null);
        btnGuardar.setText("Guardar");
        btnEliminar.setDisable(true);
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        if (esError) {
            lblMensaje.setStyle("-fx-text-fill: red;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: green;");
        }
    }
}
