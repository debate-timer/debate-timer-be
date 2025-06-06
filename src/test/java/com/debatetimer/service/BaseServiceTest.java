package com.debatetimer.service;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.fixture.CustomizeTableGenerator;
import com.debatetimer.fixture.CustomizeTimeBoxGenerator;
import com.debatetimer.fixture.MemberGenerator;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import com.debatetimer.repository.member.MemberRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseServiceTest {

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected CustomizeTableRepository customizeTableRepository;

    @Autowired
    protected CustomizeTimeBoxRepository customizeTimeBoxRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableGenerator customizeTableGenerator;

    @Autowired
    protected CustomizeTimeBoxGenerator customizeTimeBoxGenerator;

    protected void runAtSameTime(int count, Runnable task) throws InterruptedException {
        List<Thread> threads = IntStream.range(0, count)
                .mapToObj(i -> new Thread(task))
                .toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
