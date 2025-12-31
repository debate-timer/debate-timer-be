package com.debatetimer.service;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.fixture.entity.BellEntityGenerator;
import com.debatetimer.fixture.entity.CustomizeTableEntityGenerator;
import com.debatetimer.fixture.entity.CustomizeTimeBoxEntityGenerator;
import com.debatetimer.fixture.entity.MemberGenerator;
import com.debatetimer.fixture.entity.OrganizationEntityGenerator;
import com.debatetimer.fixture.entity.OrganizationTemplateEntityGenerator;
import com.debatetimer.fixture.entity.PollEntityGenerator;
import com.debatetimer.fixture.entity.VoteEntityGenerator;
import com.debatetimer.repository.customize.BellRepository;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.poll.PollRepository;
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
    protected BellRepository bellRepository;

    @Autowired
    protected PollRepository pollRepository;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableEntityGenerator customizeTableEntityGenerator;

    @Autowired
    protected CustomizeTimeBoxEntityGenerator customizeTimeBoxEntityGenerator;

    @Autowired
    protected BellEntityGenerator bellEntityGenerator;

    @Autowired
    protected PollEntityGenerator pollEntityGenerator;

    @Autowired
    protected VoteEntityGenerator voteEntityGenerator;

    @Autowired
    protected OrganizationEntityGenerator organizationEntityGenerator;

    @Autowired
    protected OrganizationTemplateEntityGenerator organizationTemplateEntityGenerator;

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
