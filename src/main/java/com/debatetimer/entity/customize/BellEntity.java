package com.debatetimer.entity.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "bell")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BellEntity {

    public static final int MAX_BELL_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customize_time_box_id")
    private CustomizeTimeBox customizeTimeBox;

    @Column(name = "bell_time")
    private int time;
    private int count;

    public BellEntity(CustomizeTimeBox customizeTimeBox, int time, int count) {
        validateTime(time);
        validateCount(count);

        this.customizeTimeBox = customizeTimeBox;
        this.time = time;
        this.count = count;
    }

    private void validateTime(int time) {
        if (time < 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_BELL_TIME);
        }
    }

    private void validateCount(int count) {
        if (count <= 0 || count > MAX_BELL_COUNT) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_BELL_COUNT);
        }
    }
}
