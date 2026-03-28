package com.payflow.ms_wallet.controller;

import com.payflow.ms_wallet.model.Wallet;
import com.payflow.ms_wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}