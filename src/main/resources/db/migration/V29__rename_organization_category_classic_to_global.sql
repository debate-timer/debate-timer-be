alter table organization
    modify column category enum ('CLASSIC', 'GLOBAL', 'DEBATE_CLUB', 'TOURNAMENT') not null;

update organization
set category = 'GLOBAL'
where category = 'CLASSIC';

alter table organization
    modify column category enum ('GLOBAL', 'DEBATE_CLUB', 'TOURNAMENT') not null;
