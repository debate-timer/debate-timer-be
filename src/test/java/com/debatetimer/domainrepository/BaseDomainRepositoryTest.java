package com.debatetimer.domainrepository;

import com.debatetimer.DataBaseCleaner;
import com.debatetimer.fixture.domain.BellGenerator;
import com.debatetimer.fixture.domain.CustomizeTableGenerator;
import com.debatetimer.fixture.domain.CustomizeTimeBoxGenerator;
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
import com.debatetimer.repository.poll.PollRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseDomainRepositoryTest {

    @Autowired
    protected CustomizeTableGenerator tableGenerator;

    @Autowired
    protected CustomizeTimeBoxGenerator timeBoxGenerator;

    @Autowired
    protected BellGenerator bellGenerator;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected CustomizeTableEntityGenerator tableEntityGenerator;

    @Autowired
    protected CustomizeTimeBoxEntityGenerator timeBoxEntityGenerator;

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

    @Autowired
    protected PollRepository pollRepository;

    @Autowired
    protected CustomizeTableRepository tableRepository;

    @Autowired
    protected CustomizeTimeBoxRepository timeBoxRepository;

    @Autowired
    protected BellRepository bellRepository;
}
