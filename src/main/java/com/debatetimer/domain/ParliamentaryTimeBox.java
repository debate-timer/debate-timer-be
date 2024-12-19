package com.debatetimer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParliamentaryTimeBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private ParliamentaryTable parliamentaryTable;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "stance", nullable = false)
    private Stance stance;

    @Column(name = "type", nullable = false)
    private Type type;

    @Column(name = "time", nullable = false)
    private int time;

    @Column(name = "speaker", nullable = false)
    private int speaker;
}
