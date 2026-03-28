package com.payflow.ms_wallet.controller;

import com.payflow.ms_wallet.dto.TransferRequestDTO;
import com.payflow.ms_wallet.model.Wallet;
import com.payflow.ms_wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getMyWallet() {

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Wallet wallet = walletService.getWalletByUserId(UUID.fromString(userId));

        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestBody @Valid TransferRequestDTO transferDto,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey
    ) {
        String senderId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        walletService.transfer(UUID.fromString(senderId), transferDto, idempotencyKey);

        return ResponseEntity.ok("Transferência realizada com sucesso!");
    }
}