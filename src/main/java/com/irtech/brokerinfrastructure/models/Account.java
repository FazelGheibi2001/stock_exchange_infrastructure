package com.irtech.brokerinfrastructure.models;

import com.irtech.brokerinfrastructure.models.enums.AccountRisk;
import com.irtech.brokerinfrastructure.models.enums.BrokerageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "broker_accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;


    @Column(
            nullable = false
    )
    private String name;


    @Column(
            name = "login_name",
            nullable = false
    )
    private String loginName;


    @Column(
            nullable = false
    )
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "brokerage_type",
            nullable = false
    )
    private BrokerageType brokerageType;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private AccountRisk risk;

    @PrePersist
    public void prePersist() {

        if (id == null) {
            id = UUID.randomUUID();
        }

    }

}
