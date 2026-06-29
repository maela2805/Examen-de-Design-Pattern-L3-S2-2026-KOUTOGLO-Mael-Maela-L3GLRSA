package ism.l3.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@ToString(exclude = "clientWallet")
@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private BigDecimal montant;

    private LocalDate dateFact;

    private String periode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unite unite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_wallet_id")
    private ClientWallet clientWallet;

    public Facture(String reference, BigDecimal montant, LocalDate dateFact,
                   String periode, Unite unite, ClientWallet clientWallet) {
        this.reference = reference;
        this.montant = montant;
        this.dateFact = dateFact;
        this.periode = periode;
        this.statut = StatutFacture.IMPAYEE;
        this.unite = unite;
        this.clientWallet = clientWallet;
    }
}
