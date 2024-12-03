package com.example.controller;

import com.example.model.Producto;
import com.example.model.Response;
import com.example.service.ProductoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProductoControllerTest {

        private MockMvc mockMvc;

        @InjectMocks
        private ProductoController productoController;

        @Mock
        private ProductoService productoService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        }

        @Test
        void testGetAllProductos() throws Exception {
                List<Producto> productos = Arrays.asList(new Producto(), new Producto());
                when(productoService.getAllProductos()).thenReturn(productos);

                mockMvc.perform(get("/productos"))
                                .andExpect(status().isOk());
                verify(productoService, times(1)).getAllProductos();
        }

        @Test
        void testGetProductoById_Success() throws Exception {
                Response response = new Response("success", new Producto(), "");
                when(productoService.getProductoById(1L)).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

                mockMvc.perform(get("/productos/1"))
                                .andExpect(status().isOk());
                verify(productoService, times(1)).getProductoById(1L);
        }

        @Test
        void testCreateProducto() throws Exception {
                Response response = new Response("success", new Producto(), "");
                when(productoService.createProducto(any(Producto.class)))
                                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

                mockMvc.perform(post("/productos")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "nombre": "Producto Test",
                                                    "descripcion": "Descripción Test",
                                                    "precio": 100.0,
                                                    "stock": 10,
                                                    "categoria": "Categoría Test",
                                                    "estado": "Activo"
                                                }
                                                """))
                                .andExpect(status().isOk());
                verify(productoService, times(1)).createProducto(any(Producto.class));
        }

        @Test
        void testDeleteProducto() throws Exception {
                Response response = new Response("success", "Producto eliminado con éxito", "");
                when(productoService.deleteProducto(1L)).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

                mockMvc.perform(delete("/productos/1"))
                                .andExpect(status().isOk());
                verify(productoService, times(1)).deleteProducto(1L);
        }

        @Test
        void testGetProductoById_ServiceThrowsException() throws Exception {
                when(productoService.getProductoById(1L)).thenThrow(new RuntimeException("Error interno"));

                mockMvc.perform(get("/productos/1"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.error").value("Error interno"));

                verify(productoService, times(1)).getProductoById(1L);
        }

        @Test
        void testCreateProducto_ValidationError() throws Exception {
                Response response = new Response("error", null, "nombre: no puede estar vacío; ");
                when(productoService.createProducto(any(Producto.class)))
                                .thenReturn(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));

                mockMvc.perform(post("/productos")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "nombre": "",
                                                    "descripcion": "Descripción Test",
                                                    "precio": -100.0,
                                                    "stock": -10,
                                                    "categoria": "",
                                                    "estado": ""
                                                }
                                                """))
                                .andExpect(status().isBadRequest());

        }

        @Test
        void testCreateProducto_InvalidJson() throws Exception {
                mockMvc.perform(post("/productos")
                                .contentType("application/json")
                                .content("{")) // JSON malformado
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testDeleteProducto_InternalServerError() throws Exception {
                when(productoService.deleteProducto(1L)).thenThrow(new RuntimeException("Error interno"));

                mockMvc.perform(delete("/productos/1"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.error").value("Error interno"));

                verify(productoService, times(1)).deleteProducto(1L);
        }

        @Test
        void testUpdateProducto_InternalServerError() throws Exception { // Error: falta el tipo de retorno
                Producto producto = new Producto("Producto Test", "Descripción", 100.0, 10, "Categoría", null, "Activo",
                                null);

                when(productoService.updateProducto(eq(1L), any(Producto.class)))
                                .thenThrow(new RuntimeException("Error inesperado"));

                mockMvc.perform(put("/productos/1")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "nombre": "Producto Test",
                                                    "descripcion": "Descripción Actualizada",
                                                    "precio": 200.0,
                                                    "stock": 20,
                                                    "categoria": "Categoría Actualizada",
                                                    "estado": "Activo"
                                                }"""))
                                .andExpect(status().is(500))
                                .andExpect(jsonPath("$.error").value("Error inesperado"));
        }

        @Test
        void testGetProductoById_InternalServerError() throws Exception {
                when(productoService.getProductoById(1L)).thenThrow(new RuntimeException("Error interno"));

                mockMvc.perform(get("/productos/1"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.error").value("Error interno"));

                verify(productoService, times(1)).getProductoById(1L);
        }

        @Test
        void testCreateProducto_ControllerValidationError() throws Exception {
                Response response = new Response("error", null,
                                "nombre: no puede estar vacío; precio: debe ser mayor que 0;");

                when(productoService.createProducto(any(Producto.class)))
                                .thenReturn(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));

                mockMvc.perform(post("/productos")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "nombre": "",
                                                    "descripcion": "Descripción inválida",
                                                    "precio": -100.0,
                                                    "stock": -10,
                                                    "categoria": "",
                                                    "estado": ""
                                                }
                                                """))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetAllProductos_EmptyList() throws Exception {
                when(productoService.getAllProductos()).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/productos"))
                                .andExpect(status().isOk())
                                .andExpect(content().json("[]")); // Verifica que la respuesta es una lista vacía
                verify(productoService, times(1)).getAllProductos();
        }

}
