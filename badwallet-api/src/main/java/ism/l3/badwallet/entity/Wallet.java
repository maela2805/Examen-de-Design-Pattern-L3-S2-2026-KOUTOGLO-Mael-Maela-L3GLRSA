package ism.l3.badwallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String currency = "XOF";

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Wallet(String phoneNumber, String email, String code,
                  String currency, BigDecimal balance) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.code = code;
        this.currency = currency;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }
}
