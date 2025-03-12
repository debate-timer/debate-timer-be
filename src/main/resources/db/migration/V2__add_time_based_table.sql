create table time_based_table
(
    duration     integer      not null,
    finish_bell  boolean      not null,
    warning_bell boolean      not null,
    id           bigint auto_increment,
    member_id    bigint       not null,
    agenda       varchar(255) not null,
    name         varchar(255) not null,
    primary key (id)
);

create table time_based_time_box
(
    sequence          integer not null,
    speaker           integer,
    time              integer,
    time_per_speaking integer,
    time_per_team     integer,
    id                bigint auto_increment,
    table_id          bigint not null,
    stance            enum ('CONS','NEUTRAL','PROS') not null,
    type              enum ('CLOSING','CROSS','LEADING','OPENING','REBUTTAL','TIME_BASED','TIME_OUT') not null,
    primary key (id)
);

alter table time_based_table
    add constraint time_based_table_to_member
    foreign key (member_id)
    references member(id);

alter table time_based_time_box
    add constraint time_based_time_box_to_time_based_table
    foreign key (table_id)
    references time_based_table(id);
