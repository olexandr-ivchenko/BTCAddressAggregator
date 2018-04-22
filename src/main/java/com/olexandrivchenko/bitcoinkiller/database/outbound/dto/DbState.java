package com.olexandrivchenko.bitcoinkiller.database.outbound.dto;

import javax.persistence.*;

@Entity
@Table(name = "dbstate")
public class DbState {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long lastLoadedBlock;
}
