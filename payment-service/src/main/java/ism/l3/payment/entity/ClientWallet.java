package ism.l3.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "client_wallets")
public class ClientWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String walletCode;

    @Column(unique = true)
    private String phoneNumber;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "clientWallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Facture> factures = new ArrayList<>();

    public ClientWallet(String walletCode, String phoneNumber) {
        this.walletCode = walletCode;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
    }
}
