package com.example.service;

import com.example.model.Producto;
import com.example.model.Response;
import com.example.repository.ProductoRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private Validator validator;

    // Obtener todos los productos
    @Override
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    // Obtener producto por ID
    @Override
    public ResponseEntity<Response> getProductoById(Long idProducto) {
        Response response;
        try {
            Optional<Producto> productoOpt = productoRepository.findById(idProducto);
            if (!productoOpt.isPresent()) {
                response = new Response("error", null, "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response = new Response("success", productoOpt.get(), "");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response = new Response("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Crear un nuevo producto
    @Override
    public ResponseEntity<Response> createProducto(Producto producto) {
        Response response;
        try {
            Set<ConstraintViolation<Producto>> violations = validator.validate(producto);

            if (!violations.isEmpty()) {
                StringBuilder errorMessages = new StringBuilder();
                for (ConstraintViolation<Producto> violation : violations) {
                    errorMessages.append(violation.getPropertyPath())
                            .append(": ")
                            .append(violation.getMessage())
                            .append("; ");
                }
                response = new Response("error", null, errorMessages.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            producto.setFechaCreacion(LocalDate.now());
            Producto nuevoProducto = productoRepository.save(producto);
            response = new Response("success", nuevoProducto, "");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response = new Response("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Actualizar un producto existente
    @Override
    public ResponseEntity<Response> updateProducto(Long idProducto, Producto productoActualizado) {
        Response response;
        try {
            Optional<Producto> productoOpt = productoRepository.findById(idProducto);
            if (!productoOpt.isPresent()) {
                response = new Response("error", null, "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Set<ConstraintViolation<Producto>> violations = validator.validate(productoActualizado);

            if (!violations.isEmpty()) {
                StringBuilder errorMessages = new StringBuilder();
                for (ConstraintViolation<Producto> violation : violations) {
                    errorMessages.append(violation.getPropertyPath())
                            .append(": ")
                            .append(violation.getMessage())
                            .append("; ");
                }
                response = new Response("error", null, errorMessages.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Producto productoExistente = productoOpt.get();
            // Actualizar los campos del producto
            productoExistente.setNombre(productoActualizado.getNombre());
            productoExistente.setDescripcion(productoActualizado.getDescripcion());
            productoExistente.setPrecio(productoActualizado.getPrecio());
            productoExistente.setStock(productoActualizado.getStock());
            productoExistente.setCategoria(productoActualizado.getCategoria());
            productoExistente.setEstado(productoActualizado.getEstado());
            productoExistente.setFechaModificacion(LocalDate.now());

            Producto productoActualizadoResult = productoRepository.save(productoExistente);
            response = new Response("success", productoActualizadoResult, "");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response = new Response("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Eliminar un producto por ID
    @Override
    public ResponseEntity<Response> deleteProducto(Long idProducto) {
        Response response;
        try {
            Optional<Producto> productoOpt = productoRepository.findById(idProducto);
            if (!productoOpt.isPresent()) {
                response = new Response("error", null, "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            productoRepository.deleteById(idProducto);
            response = new Response("success", "Producto eliminado con éxito", "");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response = new Response("error", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
