package com.payflow.ms_wallet.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_wallets")
@Data
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID walletId;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
}