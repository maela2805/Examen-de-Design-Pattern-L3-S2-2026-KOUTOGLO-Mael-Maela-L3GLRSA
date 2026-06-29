package ism.l3.badwallet.events;

import ism.l3.badwallet.entity.Transaction;
import ism.l3.badwallet.repository.TransactionRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class TransactionEventListener {

    private final TransactionRepository transactionRepository;

    public TransactionEventListener(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @EventListener
    public void onTransactionEvent(TransactionEvent event) {
        Transaction transaction = new Transaction(
                event.getType(),
                event.getAmount(),
                event.getFees(),
                event.getDescription(),
                event.getWallet()
        );
        transactionRepository.save(transaction);
    }
}
