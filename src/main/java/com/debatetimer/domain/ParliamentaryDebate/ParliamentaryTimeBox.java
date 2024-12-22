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
    private int sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Stance stance;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ParliamentaryType type;

    @NotNull
    private int time;

    private Integer speaker;
}
