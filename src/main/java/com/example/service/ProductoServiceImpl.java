package com.example.service;

import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Producto;
import com.example.model.Response;
import com.example.repository.CompraRepository;
import com.example.repository.DetalleCompraRepository;
import com.example.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    // Obtener todos los productos
    @Override
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    // Obtener producto por ID
    @Override
    public ResponseEntity<Response> getProductoById(Long idProducto) {
        Response response;
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (!productoOpt.isPresent()) {
            response = new Response("error", null, "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response = new Response("success", productoOpt.get(), "");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Crear un nuevo producto
    @Override
    public ResponseEntity<Response> createProducto(Producto producto) {
        producto.setFechaCreacion(LocalDate.now());
        Producto nuevoProducto = productoRepository.save(producto);
        Response response = new Response("success", nuevoProducto, "");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Actualizar un producto existente
    @Override
    public ResponseEntity<Response> updateProducto(Long idProducto, Producto productoActualizado) {
        Response response;
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (!productoOpt.isPresent()) {
            response = new Response("error", null, "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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
    }

    // Eliminar un producto por ID
    @Override
    public ResponseEntity<Response> deleteProducto(Long idProducto) {
        Response response;
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (!productoOpt.isPresent()) {
            response = new Response("error", null, "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        productoRepository.deleteById(idProducto);
        response = new Response("success", "Producto eliminado con éxito", "");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public ResponseEntity<Response> realizarCompra(Long idUsuario, List<DetalleCompra> detallesCompra) {
        double totalCompra = 0;

        // Crear una nueva compra
        Compra nuevaCompra = new Compra();
        nuevaCompra.setIdUsuario(idUsuario);
        nuevaCompra.setFechaCompra(LocalDate.now());
        nuevaCompra.setEstado("Pendiente");

        // Calcular el total de la compra y actualizar el stock
        for (DetalleCompra detalle : detallesCompra) {
            Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Validación de stock disponible
            if (producto.getStock() < detalle.getCantidad()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response("error", null,
                                "Stock insuficiente para el producto: " + producto.getNombre()));
            }

            // Calcular subtotal y actualizar stock
            detalle.setCompra(nuevaCompra);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(detalle.getCantidad() * producto.getPrecio());
            totalCompra += detalle.getSubtotal();

            // Reducir el stock del producto
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        nuevaCompra.setTotal(totalCompra);
        compraRepository.save(nuevaCompra);

        // Guardar los detalles de la compra
        for (DetalleCompra detalle : detallesCompra) {
            detalleCompraRepository.save(detalle);
        }

        nuevaCompra.setDetalles(detallesCompra);

        nuevaCompra.getDetalles().forEach(detalle -> {
            detalle.setProducto(productoRepository.findById(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
        });

        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response("success", nuevaCompra, "Compra realizada con éxito"));
    }
}
