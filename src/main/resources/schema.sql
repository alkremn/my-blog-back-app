-- Таблица с пользователями
create table if not exists posts(
  id bigserial primary key,
  title varchar(256) not null,
  text varchar(256) not null,
  likesCount integer not null,
  commentsCount integer not null);

insert into posts(title, text, likesCount, commentsCount) values ('Post Title #1', 'This is a post', 2, 3);
insert into posts(title, text, likesCount, commentsCount) values ('Post Title #2', 'This is a post', 1, 2);
insert into posts(title, text, likesCount, commentsCount) values ('Post Title #3', 'This is a post', 0, 23);
insert into posts(title, text, likesCount, commentsCount) values ('Post Title #4', 'This is a post', 10, 1);
