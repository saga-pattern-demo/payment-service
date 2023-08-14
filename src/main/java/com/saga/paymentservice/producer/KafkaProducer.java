package com.saga.paymentservice.producer;

import com.saga.common.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, PaymentResponseDTO> kafkaTemplate;
    private final String paymentInTopic;

    public KafkaProducer(KafkaTemplate<String, PaymentResponseDTO> kafkaTemplate,
                         @Value("${topic.name.payment.in}") String paymentInTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentInTopic = paymentInTopic;
    }

    public void sendPayment(PaymentResponseDTO payload) {
        log.info("sending to payment topic={}, payload={}", paymentInTopic, payload);
        kafkaTemplate.send(paymentInTopic, payload);
    }
}
