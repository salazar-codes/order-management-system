package com.portfolio.paymentservice.service;

import com.portfolio.paymentservice.domain.Payment;
import com.portfolio.paymentservice.domain.PaymentStatus;
import com.portfolio.paymentservice.dto.CreatePaymentRequest;
import com.portfolio.paymentservice.dto.PaymentResponse;
import com.portfolio.paymentservice.exception.PaymentFailedException;
import com.portfolio.paymentservice.exception.PaymentNotFoundException;
import com.portfolio.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    /**
     * Regla de simulación deliberadamente simple y determinista: cualquier
     * monto mayor a este umbral "falla". Esto nos da un caso reproducible
     * para forzar la compensación del SAGA (y, más adelante, para el caso de
     * prueba en la colección de Postman del Paso 10).
     */
    private static final BigDecimal SIMULATED_FAILURE_THRESHOLD = new BigDecimal("1000");

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        if (request.amount().compareTo(SIMULATED_FAILURE_THRESHOLD) > 0) {
            // Guardamos el intento fallido para que quede rastro (auditoría),
            // y luego lanzamos: el controller lo traduce a 402 Payment Required.
            Payment failed = new Payment(request.orderId(), request.amount(), PaymentStatus.FAILED);
            paymentRepository.save(failed);
            throw new PaymentFailedException(
                    "Pago rechazado: el monto " + request.amount()
                            + " excede el límite simulado de " + SIMULATED_FAILURE_THRESHOLD);
        }

        Payment payment = new Payment(request.orderId(), request.amount(), PaymentStatus.COMPLETED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPaymentsByOrder(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID orderId) {
        // Nota: hoy el SAGA nunca llega a llamar esto (si el pago falla, no
        // hay nada que reembolsar; si tiene éxito, confirmamos sin más). Lo
        // dejamos implementado porque es la compensación natural de "cobrar"
        // y la vamos a necesitar en pasos futuros con flujos más complejos
        // (ej. cancelación de un pedido ya confirmado).
        Payment payment = paymentRepository.findByOrderId(orderId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> PaymentNotFoundException.forOrder(orderId));
        payment.markRefunded();
        return PaymentResponse.from(paymentRepository.save(payment));
    }
}