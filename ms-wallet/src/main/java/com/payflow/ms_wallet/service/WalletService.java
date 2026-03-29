package com.payflow.ms_wallet.service;

import com.payflow.ms_wallet.config.RabbitMQConfig;
import com.payflow.ms_wallet.dto.TransferEvent;
import com.payflow.ms_wallet.dto.TransferRequestDTO;
import com.payflow.ms_wallet.model.Transaction;
import com.payflow.ms_wallet.model.Wallet;
import com.payflow.ms_wallet.repository.TransactionRepository;
import com.payflow.ms_wallet.repository.WalletRepository;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    TransactionRepository transactionRepository;

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });
    }

    @CacheEvict(value = "statement", allEntries = true)
    @Transactional
    public void transfer(UUID senderId, TransferRequestDTO transferDto, String idempotencyKey) {

        String key = "transfer_idempotency:" + idempotencyKey;

        Boolean isFirstRequest = redisTemplate.opsForValue().setIfAbsent(key, "processed", 5, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw new RuntimeException("Esta transação já foi processada ou está em processamento.");
        }

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

        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(transferDto.receiverId());
        transaction.setAmount(transferDto.value());
        transaction.setDescription(transferDto.description());

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransferEvent event = new TransferEvent(
                savedTransaction.getTransactionId(),
                senderId,
                transferDto.receiverId(),
                transferDto.value(),
                transferDto.description()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.TRANSFER_EXCHANGE, "", event);
        System.out.println("Transação salva e Evento enviado para a Exchange!");
    }

    @Cacheable(value = "statement", key = "#userId")
    public List<Transaction> getStatement(UUID userId) {
        System.out.println("[CACHE MISS] Indo buscar o extrato no lento banco de dados relacional PostgreSQL...");
        return transactionRepository.findStatementByUserId(userId);
    }
}