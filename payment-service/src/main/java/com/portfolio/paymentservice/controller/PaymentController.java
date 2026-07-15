package com.portfolio.paymentservice.controller;

import com.portfolio.paymentservice.dto.CreatePaymentRequest;
import com.portfolio.paymentservice.dto.PaymentResponse;
import com.portfolio.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.created(URI.create("/api/payments/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentService.getPayment(id);
    }

    @GetMapping
    public List<PaymentResponse> listByOrder(@RequestParam UUID orderId) {
        return paymentService.listPaymentsByOrder(orderId);
    }
}
