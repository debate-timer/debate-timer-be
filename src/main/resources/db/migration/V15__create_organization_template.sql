create table organization
(
    id          bigint auto_increment,
    name        varchar(255) not null,
    affiliation varchar(255) not null,
    icon_path   varchar(255) not null,
    created_at  timestamp    not null DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp    not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key (id)
);

create table organization_template
(
    id              bigint auto_increment,
    name            varchar(255)  not null,
    data            varchar(8191) not null,
    organization_id bigint        not null,
    created_at      timestamp     not null DEFAULT CURRENT_TIMESTAMP,
    modified_at     timestamp     not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key (id)
);

alter table organization_template
    add constraint organization_template_to_organization
        foreign key (organization_id)
            references organization (id);
