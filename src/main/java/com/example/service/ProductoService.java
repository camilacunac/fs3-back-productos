package com.example.service;

import com.example.model.DetalleCompra;
import com.example.model.Producto;
import com.example.model.Response;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface ProductoService {
    List<Producto> getAllProductos();

    ResponseEntity<Response> getProductoById(Long idProducto) throws Exception;

    ResponseEntity<Response> createProducto(Producto producto);

    ResponseEntity<Response> updateProducto(Long idProducto, Producto productoActualizado) throws Exception;

    ResponseEntity<Response> deleteProducto(Long idProducto) throws Exception;

    ResponseEntity<Response> realizarCompra(Long idUsuario, List<DetalleCompra> detallesCompra);

}
