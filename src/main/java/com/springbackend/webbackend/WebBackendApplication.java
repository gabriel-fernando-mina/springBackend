package com.springbackend.webbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.springbackend.webbackend")
public class WebBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebBackendApplication.class, args);
    }

    /* Estructura del backend
    Separación de responsabilidades:

- ProductController maneja las peticiones HTTP.
- ProductService define la lógica de negocio.
- ProductServiceImpl implementa los métodos de servicio.
- ProductRepository (JPA) gestiona la base de datos.
- ProductDTO actúa como intermediario para validar y transferir datos.
- Uso de DTO para evitar exponer la entidad directamente en la API.

    Implementamos Paginación y Filtrado
- Metodo findAll(Pageable pageable) para devolver productos paginados.
- Metodo findByName(String name, Pageable pageable) para buscar por nombre.
- Metodo findByPriceBetween(Double min, Double max, Pageable pageable) para filtrar por rango de precio.
- Uso del objeto Page<ProductDTO> para una respuesta más ordenada y eficiente.

    Validación de Datos con @Valid y BindingResult
- No se pueden guardar productos con nombre vacío o precio menor a 0.
- Si los datos no son válidos, Spring devuelve un error 400 Bad Request con detalles de validación.

    Metodos CRUD
- GET /api/products/{id} ahora devuelve un ResponseEntity<ProductDTO> con 200 OK o 404 Not Found.
- POST /api/products usa ResponseEntity<?> para validar datos antes de guardar.
- PUT /api/products/{id} actualiza solo si el producto existe.
- DELETE /api/products/{id} devuelve 204 No Content si elimina correctamente y 404 Not Found si el ID no existe.

Seguridad
- Maneja errores correctamente en cada operación (404 Not Found, 400 Bad Request, 201 Created, etc.).
- No se expone la entidad Product directamente, usamos ProductDTO para mejor encapsulación.
- Codigo más limpio y escalable con inyección de dependencias (@Service, @Autowired).

*/
}
