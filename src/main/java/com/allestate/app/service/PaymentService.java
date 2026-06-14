package com.allestate.app.service;

import com.allestate.app.model.Payment;
import com.allestate.app.model.PaymentStatus;
import com.allestate.app.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // Gateway webhook secret (e.g., Razorpay secret) read from env / secrets manager
    @Value("${payment.gateway.secret:}")
    private String gatewaySecret;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment handleWebhook(String payload, String signature) throws Exception {
        // Verify signature if secret is provided
        if (gatewaySecret != null && !gatewaySecret.isBlank()) {
            boolean ok = verifySignature(payload, signature, gatewaySecret);
            if (!ok) throw new SecurityException("Invalid gateway signature");
        }

        // For simplicity, assume payload is a minimal JSON containing: gateway_payment_id, gateway_event_id, status
        // In production, parse payload properly. Here we do a naive extraction to keep dependency minimal.
        String gatewayPaymentId = extractJsonField(payload, "payment_id");
        String gatewayEventId = extractJsonField(payload, "event_id");
        String status = extractJsonField(payload, "status");

        // Idempotency: check gateway_event_id
        if (gatewayEventId != null) {
            Optional<Payment> existing = paymentRepository.findByGatewayEventId(gatewayEventId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Find payment by gateway payment id
        Payment payment = null;
        if (gatewayPaymentId != null) {
            payment = paymentRepository.findByGatewayPaymentId(gatewayPaymentId).orElse(null);
        }

        if (payment == null) {
            // Create a stub payment record if none exists (safer to surface for manual review)
            payment = new Payment();
            payment.setGatewayPaymentId(gatewayPaymentId);
            payment.setGatewayEventId(gatewayEventId);
            payment.setAmountInPaise(0L);
        }

        // Map gateway status to PaymentStatus
        if (status != null && status.equalsIgnoreCase("captured")) {
            payment.setStatus(PaymentStatus.CAPTURED);
        } else if (status != null && status.equalsIgnoreCase("failed")) {
            payment.setStatus(PaymentStatus.FAILED);
        } else if (status != null && status.equalsIgnoreCase("authorized")) {
            payment.setStatus(PaymentStatus.AUTHORIZED);
        }

        // Save gateway event id
        if (gatewayEventId != null) payment.setGatewayEventId(gatewayEventId);

        paymentRepository.save(payment);
        return payment;
    }

    private boolean verifySignature(String payload, String signature, String secret) throws Exception {
        // Razorpay-style HMAC SHA256 signature (hex or base64 depending on gateway). We'll compute HMAC SHA256 and compare hex/base64.
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] computed = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        String computedBase64 = Base64.getEncoder().encodeToString(computed);
        // Compare with provided signature
        if (signature == null) return false;
        return computedBase64.equals(signature) || bytesToHex(computed).equalsIgnoreCase(signature);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Very small and fragile JSON extractor for a flat string field (for local tests only)
    private String extractJsonField(String json, String field) {
        if (json == null || field == null) return null;
        int idx = json.indexOf('"' + field + '"');
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return null;
        int firstQuote = json.indexOf('"', colon);
        if (firstQuote == -1) return null;
        int endQuote = json.indexOf('"', firstQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(firstQuote + 1, endQuote);
    }
}


