package com.portfolio.paymentservice.service;

import com.portfolio.paymentservice.domain.Payment;
import com.portfolio.paymentservice.dto.CreatePaymentRequest;
import com.portfolio.paymentservice.dto.PaymentResponse;
import com.portfolio.paymentservice.exception.PaymentNotFoundException;
import com.portfolio.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Paso 1: siempre "cobra" con éxito. La lógica de fallo simulado y
        // reembolso (compensación) llega junto con el SAGA en el Paso 2.
        Payment payment = new Payment(request.orderId(), request.amount());
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
}
