package com.payflow.ms_wallet.service;

import com.payflow.ms_wallet.config.RabbitMQConfig;
import com.payflow.ms_wallet.dto.TransferEvent;
import com.payflow.ms_wallet.dto.TransferRequestDTO;
import com.payflow.ms_wallet.exception.DuplicateTransferException;
import com.payflow.ms_wallet.exception.InsufficientBalanceException;
import com.payflow.ms_wallet.exception.InvalidTransferException;
import com.payflow.ms_wallet.exception.WalletNotFoundException;
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
            throw new DuplicateTransferException("Transacao ja processada ou em processamento");
        }

        if (senderId.equals(transferDto.receiverId())) {
            throw new InvalidTransferException("Nao e permitido transferir para a propria conta");
        }

        Wallet senderWallet = walletRepository.findByUserId(senderId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira do remetente nao encontrada"));

        Wallet receiverWallet = walletRepository.findByUserId(transferDto.receiverId())
                .orElseThrow(() -> new WalletNotFoundException("Carteira do destinatario nao encontrada"));

        if (senderWallet.getBalance().compareTo(transferDto.value()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
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
        System.out.println("Transacao salva e evento enviado para a exchange");
    }

    @Cacheable(value = "statement", key = "#userId")
    public List<Transaction> getStatement(UUID userId) {
        System.out.println("[CACHE MISS] Indo buscar o extrato no lento banco de dados relacional PostgreSQL...");
        return transactionRepository.findStatementByUserId(userId);
    }
}