package com.debatetimer.domain.ParliamentaryDebate;

import com.debatetimer.domain.Stance;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private ParliamentaryTable parliamentaryTable;

    @NotNull
    @Column(name = "sequence")
    private int sequence;

    @NotNull
    @Column(name = "stance")
    @Enumerated(EnumType.STRING)
    private Stance stance;

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ParliamentaryType parliamentaryType;

    @NotNull
    @Column(name = "time")
    private int time;

    @Column(name = "speaker")
    private int speaker;
}
