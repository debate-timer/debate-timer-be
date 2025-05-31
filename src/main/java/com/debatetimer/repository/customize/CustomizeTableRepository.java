package com.debatetimer.repository.customize;

import com.debatetimer.domain.customize.CustomizeTableEntity;
import com.debatetimer.domain.member.Member;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface CustomizeTableRepository extends Repository<CustomizeTableEntity, Long> {

    CustomizeTableEntity save(CustomizeTableEntity customizeTableEntity);

    Optional<CustomizeTableEntity> findById(long id);

    List<CustomizeTableEntity> findAllByMember(Member member);

    Optional<CustomizeTableEntity> findByIdAndMember(long tableId, Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomizeTableEntity c WHERE c.id = :id AND c.member = :member")
    Optional<CustomizeTableEntity> findByIdAndMemberWithLock(long id, Member member);

    void delete(CustomizeTableEntity table);
}
