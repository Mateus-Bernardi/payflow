package com.payflow.ms_wallet.controller;

import com.payflow.ms_wallet.dto.TransferRequestDTO;
import com.payflow.ms_wallet.exception.UnauthorizedRequestException;
import com.payflow.ms_wallet.model.Transaction;
import com.payflow.ms_wallet.model.Wallet;
import com.payflow.ms_wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallet")
@Validated
public class WalletController {

    @Autowired
    WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getMyWallet() {
        UUID userId = getAuthenticatedUserId();
        Wallet wallet = walletService.getWalletByUserId(userId);

        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestBody @Valid TransferRequestDTO transferDto,
            @RequestHeader("X-Idempotency-Key") @NotBlank String idempotencyKey
    ) {
        UUID senderId = getAuthenticatedUserId();
        walletService.transfer(senderId, transferDto, idempotencyKey);

        return ResponseEntity.ok("Transferencia realizada com sucesso");
    }

    @GetMapping("/statement")
    public ResponseEntity<List<Transaction>> getStatement() {
        UUID userId = getAuthenticatedUserId();
        List<Transaction> statement = walletService.getStatement(userId);

        return ResponseEntity.ok(statement);
    }

    private UUID getAuthenticatedUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return UUID.fromString((String) principal);
        } catch (Exception exception) {
            throw new UnauthorizedRequestException("Token ausente ou invalido");
        }
    }
}