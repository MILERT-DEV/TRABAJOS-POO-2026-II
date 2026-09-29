# Padrón de Animales Domésticos (Ejercicio 31)

CRUD de mascotas hecho con JavaFX y Maven. Los datos se guardan en un ArrayList.

Para ejecutar:
```
mvn clean javafx:run
```

Estructura:
- model: Mascota (abstracta), Perro, Gato, OtraMascota
- repository: IMascotaRepository y MascotaRepository (ArrayList)
- service: IMascotaService y MascotaService (validaciones y filtros)
- controller: MascotaController (JavaFX)
- resources/view: mascotas.fxml
