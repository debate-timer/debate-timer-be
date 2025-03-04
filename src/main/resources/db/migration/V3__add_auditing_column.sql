alter table member add column created_at timestamp default current_timestamp not null;
alter table member add column modified_at timestamp default current_timestamp not null;

alter table parliamentary_table add column created_at timestamp default current_timestamp not null;
alter table parliamentary_table add column modified_at timestamp default current_timestamp not null;
alter table parliamentary_table add column used_at timestamp default current_timestamp not null;

alter table time_based_table add column created_at timestamp default current_timestamp not null;
alter table time_based_table add column modified_at timestamp default current_timestamp not null;
alter table time_based_table add column used_at timestamp default current_timestamp not null;
