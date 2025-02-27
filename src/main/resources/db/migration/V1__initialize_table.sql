create table member
(
    id    bigint auto_increment,
    email varchar(255) not null unique,
    primary key (id)
);

create table parliamentary_table
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

create table parliamentary_time_box
(
    sequence integer not null,
    speaker  integer,
    time     integer not null,
    id       bigint auto_increment,
    table_id bigint  not null,
    stance   enum ('CONS','NEUTRAL','PROS') not null,
    type     enum ('CLOSING','CROSS','OPENING','REBUTTAL','TIME_OUT') not null,
    primary key (id)
);

alter table parliamentary_table
    add constraint parliamentary_table_to_member
    foreign key (member_id)
    references member(id);

alter table parliamentary_time_box
    add constraint parliamentary_time_box_to_parliamentary_table
    foreign key (table_id)
    references parliamentary_table(id);
