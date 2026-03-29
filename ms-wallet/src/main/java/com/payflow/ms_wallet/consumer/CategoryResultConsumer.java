package com.payflow.ms_wallet.consumer;

import com.payflow.ms_wallet.dto.CategoryResultEvent;
import com.payflow.ms_wallet.exception.TransactionNotFoundException;
import com.payflow.ms_wallet.model.Transaction;
import com.payflow.ms_wallet.repository.TransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class CategoryResultConsumer {

    @Autowired
    private TransactionRepository transactionRepository;

    @CacheEvict(value = "statement", allEntries = true)
    @RabbitListener(queuesToDeclare = @org.springframework.amqp.rabbit.annotation.Queue("transfer.category.result"))
    public void receiveCategoryResult(CategoryResultEvent result) {

        System.out.println("Recebendo categorização da IA para a transação: " + result.transactionId());

        Transaction transaction = transactionRepository.findById(result.transactionId())
            .orElseThrow(() -> new TransactionNotFoundException("Transacao nao encontrada para categorizacao"));

        transaction.setCategory(result.category());

        transactionRepository.save(transaction);

        System.out.println("Livro-razão (Ledger) atualizado com sucesso!");
    }
}