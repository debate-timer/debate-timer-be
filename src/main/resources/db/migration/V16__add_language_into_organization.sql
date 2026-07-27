alter table organization
    add column language varchar(255) null;
update organization
set language = 'KR';
alter table organization
    modify column language varchar(255) not null;

alter table organization_template
    add column language varchar(255) null;
update organization_template
set language = 'KR';
alter table organization_template
    modify column language varchar(255) not null;
