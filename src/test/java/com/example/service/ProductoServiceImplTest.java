package com.example.service;

import com.example.model.Producto;
import com.example.model.Response;
import com.example.repository.ProductoRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProductoServiceImplTest {

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProductos() {
        // Arrange
        List<Producto> productos = Arrays.asList(new Producto(), new Producto());
        when(productoRepository.findAll()).thenReturn(productos);

        // Act
        List<Producto> result = productoService.getAllProductos();

        // Assert
        assertEquals(2, result.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void testGetProductoById_Success() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        ResponseEntity<Response> response = productoService.getProductoById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getState());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductoById_NotFound() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Response> response = productoService.getProductoById(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Producto no encontrado", response.getBody().getError());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateProducto_Success() {
        // Arrange
        Producto producto = new Producto();
        when(validator.validate(producto)).thenReturn(Collections.emptySet());
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getState());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void testCreateProducto_ValidationFailure() {
        // Arrange
        Producto producto = new Producto();

        // Crear un mock de ConstraintViolation y configurar su comportamiento
        ConstraintViolation<Producto> violation = mock(ConstraintViolation.class);
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("nombre");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("no puede estar vacío");
        Set<ConstraintViolation<Producto>> violations = Set.of(violation);

        // Configurar el validador para devolver las violaciones
        when(validator.validate(producto)).thenReturn(violations);

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("nombre: no puede estar vacío; ", response.getBody().getError());
    }

    @Test
    void testDeleteProducto_Success() {
        // Arrange
        Producto producto = new Producto();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        ResponseEntity<Response> response = productoService.deleteProducto(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Producto eliminado con éxito", response.getBody().getRes());
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProducto_NotFound() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Response> response = productoService.deleteProducto(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Producto no encontrado", response.getBody().getError());
        verify(productoRepository, never()).deleteById(1L);
    }

    @Test
    void testGetProductoById_InternalServerError() {
        // Arrange
        when(productoRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Response> response = productoService.getProductoById(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Database error", response.getBody().getError());
    }

    @Test
    void testCreateProducto_MultipleValidationErrors() {
        // Arrange
        Producto producto = new Producto();
        ConstraintViolation<Producto> violation1 = mockConstraintViolation("nombre", "no puede estar vacío");
        ConstraintViolation<Producto> violation2 = mockConstraintViolation("precio", "debe ser mayor que 0");
        Set<ConstraintViolation<Producto>> violations = Set.of(violation1, violation2);

        when(validator.validate(producto)).thenReturn(violations);

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getError().contains("nombre: no puede estar vacío"));
        assertTrue(response.getBody().getError().contains("precio: debe ser mayor que 0"));
    }

    @Test
    void testUpdateProducto_PartialUpdate() throws Exception {
        // Arrange
        Producto productoExistente = new Producto("Producto 1", "Descripción 1", 100.0, 10, "Categoría", null, "Activo",
                null);
        Producto productoActualizado = new Producto();
        productoActualizado.setPrecio(150.0); // Solo actualiza el precio

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(validator.validate(productoActualizado)).thenReturn(Collections.emptySet());
        when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);

        // Act
        ResponseEntity<Response> response = productoService.updateProducto(1L, productoActualizado);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(150.0, ((Producto) response.getBody().getRes()).getPrecio());
    }

    @Test
    void testDeleteProducto_InternalServerError() {
        // Arrange
        when(productoRepository.findById(anyLong())).thenThrow(new RuntimeException("Error interno"));

        // Act
        ResponseEntity<Response> response = productoService.deleteProducto(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno", response.getBody().getError());
    }

    private ConstraintViolation<Producto> mockConstraintViolation(String property, String message) {
        ConstraintViolation<Producto> violation = mock(ConstraintViolation.class);

        // Crear un mock para Path
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn(property);

        // Configurar el mock de ConstraintViolation
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn(message);

        return violation;
    }

    @Test
    void testUpdateProducto_InternalServerError() {
        // Arrange
        Producto producto = new Producto();
        when(productoRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Response> response = productoService.updateProducto(1L, producto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Database error", response.getBody().getError());
    }

    @Test
    void testCreateProducto_ValidationMultipleFields() {
        // Arrange
        Producto producto = new Producto();
        ConstraintViolation<Producto> violation1 = mockConstraintViolation("nombre", "no puede estar vacío");
        ConstraintViolation<Producto> violation2 = mockConstraintViolation("stock", "debe ser mayor que 0");

        when(validator.validate(producto)).thenReturn(Set.of(violation1, violation2));

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getError().contains("nombre: no puede estar vacío"));
        assertTrue(response.getBody().getError().contains("stock: debe ser mayor que 0"));
    }

    @Test
    void testCreateProducto_InternalServerError() {
        // Arrange
        Producto producto = new Producto("Producto Test", "Descripción", 100.0, 10, "Categoría", null, "Activo", null);
        when(productoRepository.save(any(Producto.class))).thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error inesperado", response.getBody().getError());
    }

    @Test
    void testGetAllProductos_EmptyList() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Producto> productos = productoService.getAllProductos();

        // Assert
        assertNotNull(productos);
        assertTrue(productos.isEmpty());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void testUpdateProducto_NotFound() {
        // Arrange
        Producto producto = new Producto();
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Response> response = productoService.updateProducto(1L, producto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Producto no encontrado", response.getBody().getError());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void testGetProductoById_NullId() {
        // Act
        ResponseEntity<Response> response = productoService.getProductoById(null);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateProducto_InvalidPrice() {
        // Arrange
        Producto producto = new Producto();
        producto.setPrecio(-10.0);

        ConstraintViolation<Producto> violation = mockConstraintViolation("precio", "debe ser mayor que 0");
        when(validator.validate(producto)).thenReturn(Set.of(violation));

        // Act
        ResponseEntity<Response> response = productoService.createProducto(producto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getError().contains("precio: debe ser mayor que 0"));
    }

    @Test
    void testDeleteProducto_Exception() {
        // Arrange
        when(productoRepository.findById(1L)).thenThrow(new RuntimeException("Error al eliminar"));

        // Act
        ResponseEntity<Response> response = productoService.deleteProducto(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error al eliminar", response.getBody().getError());
    }

}
