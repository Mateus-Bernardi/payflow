package com.payflow.ms_wallet.service;

import com.payflow.ms_wallet.dto.TransferRequestDTO;
import com.payflow.ms_wallet.model.Wallet;
import com.payflow.ms_wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });
    }

    @Transactional
    public void transfer(UUID senderId, TransferRequestDTO transferDto) {

        if (senderId.equals(transferDto.receiverId())) {
            throw new RuntimeException("Você não pode transferir dinheiro para si mesmo.");
        }

        Wallet senderWallet = walletRepository.findByUserId(senderId)
                .orElseThrow(() -> new RuntimeException("Carteira do remetente não encontrada."));

        Wallet receiverWallet = walletRepository.findByUserId(transferDto.receiverId())
                .orElseThrow(() -> new RuntimeException("Carteira do destinatário não encontrada."));

        if (senderWallet.getBalance().compareTo(transferDto.value()) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(transferDto.value()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferDto.value()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
    }
}