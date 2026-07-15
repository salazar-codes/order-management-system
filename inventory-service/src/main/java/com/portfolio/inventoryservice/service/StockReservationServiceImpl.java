package com.portfolio.inventoryservice.service;

import com.portfolio.inventoryservice.domain.Product;
import com.portfolio.inventoryservice.domain.StockReservation;
import com.portfolio.inventoryservice.dto.ReserveItemRequest;
import com.portfolio.inventoryservice.dto.ReserveStockRequest;
import com.portfolio.inventoryservice.exception.InsufficientStockException;
import com.portfolio.inventoryservice.exception.ProductNotFoundException;
import com.portfolio.inventoryservice.repository.ProductRepository;
import com.portfolio.inventoryservice.repository.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StockReservationServiceImpl implements StockReservationService {

    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;

    public StockReservationServiceImpl(ProductRepository productRepository,
                                       StockReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void reserveStock(ReserveStockRequest request) {
        List<ReserveItemRequest> items = request.items();

        // Fase 1: cargar y VALIDAR todos los productos antes de mutar nada.
        // Esto es lo que hace que la reserva sea "todo o nada": si un solo
        // sku no alcanza, ninguno de los otros se toca — y como esta
        // transacción local nunca hace commit parcial, un rollback de esta
        // única base de datos (no de un SAGA distribuido) ya nos protege.
        List<Product> products = items.stream()
                .map(item -> productRepository.findBySku(item.sku())
                        .orElseThrow(() -> new ProductNotFoundException(item.sku())))
                .toList();

        List<String> insufficient = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int requested = items.get(i).quantity();
            if (product.getQuantityAvailable() < requested) {
                insufficient.add(product.getSku() + " (disponible: "
                        + product.getQuantityAvailable() + ", pedido: " + requested + ")");
            }
        }
        if (!insufficient.isEmpty()) {
            throw new InsufficientStockException(insufficient);
        }

        // Fase 2: ahora sí, mutar y dejar rastro de la reserva.
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = items.get(i).quantity();
            product.reserve(quantity);
            productRepository.save(product);
            reservationRepository.save(new StockReservation(request.orderId(), product.getSku(), quantity));
        }
    }

    @Override
    @Transactional
    public void releaseStock(UUID orderId) {
        List<StockReservation> reservations = reservationRepository.findByOrderIdAndActiveTrue(orderId);

        // Idempotente a propósito: si ya no hay reservas activas (por ejemplo,
        // si release se llamara dos veces por un reintento), simplemente no
        // hace nada en vez de fallar. Esto importa mucho una vez que Kafka
        // (Paso 3) pueda re-entregar el mismo evento de compensación.
        for (StockReservation reservation : reservations) {
            Product product = productRepository.findBySku(reservation.getProductSku())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductSku()));
            product.release(reservation.getQuantity());
            productRepository.save(product);
            reservation.release();
            reservationRepository.save(reservation);
        }
    }
}