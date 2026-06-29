package ism.l3.badwallet.services.deposit;

import ism.l3.badwallet.entity.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class DepositStrategyFactory {

    private final CreditCardDepositStrategy creditCardDepositStrategy;
    private final WalletTargetDepositStrategy walletTargetDepositStrategy;

    public DepositStrategyFactory(CreditCardDepositStrategy creditCardDepositStrategy,
                                   WalletTargetDepositStrategy walletTargetDepositStrategy) {
        this.creditCardDepositStrategy = creditCardDepositStrategy;
        this.walletTargetDepositStrategy = walletTargetDepositStrategy;
    }

    public DepositStrategy getStrategy(String paymentMethod) {
        PaymentMethod method = PaymentMethod.valueOf(paymentMethod.toUpperCase());
        return switch (method) {
            case CREDIT_CARD -> creditCardDepositStrategy;
            case WALLET_TARGET -> walletTargetDepositStrategy;
        };
    }
}
