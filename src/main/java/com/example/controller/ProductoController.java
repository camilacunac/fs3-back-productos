package com.example.controller;

import com.example.model.Producto;
import com.example.model.Response;
import com.example.service.ProductoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Obtener todos los productos
    @GetMapping
    public List<Producto> getAllProductos() {
        return productoService.getAllProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getProductoById(@PathVariable Long id) {
        try {
            return productoService.getProductoById(id);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Crear un nuevo producto
    @PostMapping
    public ResponseEntity<Response> createProducto(@RequestBody @Valid Producto producto) throws Exception {
        try {
            return productoService.createProducto(producto);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Actualizar un producto por ID
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateProducto(@PathVariable Long id, @RequestBody @Valid Producto producto)
            throws Exception {
        try {
            return productoService.updateProducto(id, producto);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteProducto(@PathVariable Long id) {
        try {
            return productoService.deleteProducto(id);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private ResponseEntity<Response> errorResponse(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("error", null, e.getMessage()));
    }

}
