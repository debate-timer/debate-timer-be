create table customize_table
(
    finish_bell  boolean      not null,
    warning_bell boolean      not null,
    id           bigint auto_increment,
    member_id    bigint       not null,
    agenda       varchar(255) not null,
    name         varchar(255) not null,
    pros_team_name varchar(255) not null,
    cons_team_name varchar(255) not null,
    primary key (id)
);

create table customize_time_box
(
    sequence          integer not null,
    speaker           integer,
    time              integer not null,
    time_per_speaking integer,
    time_per_team     integer,
    id                bigint auto_increment,
    table_id          bigint not null,
    stance            enum ('CONS','NEUTRAL','PROS') not null,
    speech_type       varchar(255) not null,
    box_type          enum ('NORMAL','TIME_BASED') not null,
    primary key (id)
);

alter table customize_table
    add constraint customize_table_to_member
        foreign key (member_id)
            references member(id);

alter table customize_time_box
    add constraint customize_time_box_to_time_based_table
        foreign key (table_id)
            references customize_table(id);
