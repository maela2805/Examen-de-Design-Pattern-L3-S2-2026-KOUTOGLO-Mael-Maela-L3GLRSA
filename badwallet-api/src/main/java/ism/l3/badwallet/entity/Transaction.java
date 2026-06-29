package ism.l3.badwallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@ToString(exclude = "wallet")
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal netAmount;

    private String description;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public Transaction(TransactionType type, BigDecimal amount,
                       BigDecimal fees, String description, Wallet wallet) {
        this.type = type;
        this.amount = amount;
        this.fees = fees;
        this.netAmount = amount.subtract(fees);
        this.description = description;
        this.wallet = wallet;
        this.createdAt = LocalDateTime.now();
    }
}
