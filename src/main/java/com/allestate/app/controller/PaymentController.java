package com.allestate.app.controller;

import com.allestate.app.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Webhook endpoint for payment gateway callbacks
    // Many gateways post JSON payload and include a signature header (e.g., X-Razorpay-Signature)
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestHeader(value = "X-Gateway-Signature", required = false) String signature,
            @RequestBody String payload
    ) {
        try {
            paymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().body("ok");
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        } catch (Exception e) {
            // In production, log and handle error properly. For now return 500.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}



