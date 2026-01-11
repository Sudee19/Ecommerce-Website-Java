package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final DemoModeService demoModeService;
    
    /**
     * Dummy payment processing - simulates successful payment
     * In production, integrate with real payment gateway (Stripe, Razorpay, etc.)
     */
    public Payment processPayment(Order order, String paymentMethod) {
        Payment.PaymentMethod method = parsePaymentMethod(paymentMethod);

        boolean isDemoUser = demoModeService.isDemoUserId(order.getUserId());
        
        Payment payment = Payment.builder()
                .id(isDemoUser ? ("demo-payment-" + UUID.randomUUID()) : null)
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .currency("INR")
                .method(method)
                .status(Payment.PaymentStatus.INITIATED)
                .build();

        if (!isDemoUser) {
            payment = paymentRepository.save(payment);
        }
        
        // Simulate payment processing
        if (method == Payment.PaymentMethod.COD) {
            // COD doesn't require immediate payment
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
        } else {
            // Simulate successful payment for other methods
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId());
            
            if (method == Payment.PaymentMethod.CREDIT_CARD || 
                method == Payment.PaymentMethod.DEBIT_CARD) {
                payment.setCardLast4("4242"); // Dummy card
                payment.setCardBrand("VISA");
            }
            
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }

        payment.setCompletedAt(LocalDateTime.now());
        
        order.setPaymentId(payment.getId());

        if (!isDemoUser) {
            orderRepository.save(order);
            return paymentRepository.save(payment);
        }

        return payment;
    }
    
    private Payment.PaymentMethod parsePaymentMethod(String method) {
        try {
            return Payment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Payment.PaymentMethod.COD; // Default to COD
        }
    }
    
    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
