package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.Bell;
import org.springframework.data.repository.Repository;

public interface BellRepository extends Repository<Bell, Long> {

    Bell save(Bell bell);
}
