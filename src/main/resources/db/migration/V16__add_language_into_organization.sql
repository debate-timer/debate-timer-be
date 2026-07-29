alter table organization
    add column language varchar(255) not null default 'KO_KR';

alter table organization_template
    add column language varchar(255) not null default 'KO_KR';
