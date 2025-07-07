create table bell
(
    id           bigint auto_increment,
    time    bigint       not null,
    count       bigint not null,
    customize_time_box_id bigint not null,
    primary key (id)
);

alter table bell
    add constraint bell_to_customize_time_box
        foreign key (customize_time_box_id)
            references customize_time_box(id);
