package com.saga.paymentservice.service;

import com.saga.common.dto.OrchestratorResponseDTO;
import com.saga.common.dto.PaymentResponseDTO;
import com.saga.common.enums.OrderStatus;
import com.saga.common.enums.PaymentStatus;
import com.saga.paymentservice.entity.Payment;
import com.saga.paymentservice.producer.KafkaProducer;
import com.saga.paymentservice.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    private final KafkaProducer kafkaProducer;
    private Map<Integer, Double> userBalance;

    public PaymentService(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostConstruct
    private void init() {
        userBalance = new HashMap<>();
        userBalance.put(1, 100d);
        userBalance.put(2, 100d);
        userBalance.put(3, 200d);
    }

    private void debit(OrchestratorResponseDTO responseDTO) {
        double balance = userBalance.get(responseDTO.getUserID());
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setUserID(responseDTO.getUserID());
        paymentResponseDTO.setOrderID(responseDTO.getOrderID());
        paymentResponseDTO.setAmount(responseDTO.getAmount());

        if (balance >= responseDTO.getAmount()) {
            paymentResponseDTO.setStatus(PaymentStatus.PAYMENT_APPROVED);
            userBalance.put(responseDTO.getUserID(), balance - responseDTO.getAmount());
        } else {
            paymentResponseDTO.setStatus(PaymentStatus.PAYMENT_REJECTED);
        }

        paymentRepository.save(dtoToEntity(paymentResponseDTO));
        kafkaProducer.sendPayment(paymentResponseDTO);
        log.info("USER " + responseDTO.getUserID() + " BALANCE: " + userBalance.get(responseDTO.getUserID()));
    }

    private void credit(OrchestratorResponseDTO responseDTO) {
        double balance = userBalance.get(responseDTO.getUserID());
        userBalance.put(responseDTO.getUserID(), balance + responseDTO.getAmount());
        log.info("USER " + responseDTO.getUserID() + " BALANCE: " + userBalance.get(responseDTO.getUserID()));
    }

    @KafkaListener(
            topics = "${topic.name.payment.out}",
            groupId = "${spring.kafka.consumer.payment-group-id}"
    )
    private void getOrchestratorMessage(OrchestratorResponseDTO responseDTO) {
        if (responseDTO.getStatus().equals(OrderStatus.ORDER_CANCELLED)) {
            credit(responseDTO);
        }
        debit(responseDTO);
    }

    private Payment dtoToEntity(PaymentResponseDTO dto) {
        Payment payment = new Payment();
        payment.setUserID(dto.getUserID());
        payment.setOrderID(dto.getOrderID());
        payment.setAmount(dto.getAmount());
        payment.setStatus(dto.getStatus());

        return payment;
    }

}
