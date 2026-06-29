package ism.l3.badwallet.events;

import ism.l3.badwallet.entity.TransactionType;
import ism.l3.badwallet.entity.Wallet;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class TransactionEvent extends ApplicationEvent {

    private final Wallet wallet;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal fees;
    private final String description;

    public TransactionEvent(Object source, Wallet wallet, TransactionType type,
                            BigDecimal amount, BigDecimal fees, String description) {
        super(source);
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.fees = fees;
        this.description = description;
    }
}
