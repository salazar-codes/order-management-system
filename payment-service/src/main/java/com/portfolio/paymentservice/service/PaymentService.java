package com.portfolio.paymentservice.service;

import com.portfolio.paymentservice.dto.CreatePaymentRequest;
import com.portfolio.paymentservice.dto.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPayment(UUID id);

    List<PaymentResponse> listPaymentsByOrder(UUID orderId);

    PaymentResponse refundPayment(UUID orderId);
}