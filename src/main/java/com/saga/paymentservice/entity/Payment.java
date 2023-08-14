package com.saga.paymentservice.entity;

import com.saga.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userID;
    private UUID orderID;
    private Double amount;
    private PaymentStatus status;
}
