create table if not exists Users(
                                    id bigserial primary key not null,
                                    username varchar(32) not null unique,
                                    email varchar(128) not null,
                                    password_hash varchar(128) not null,
                                    salt varchar(32) not null
);

insert into Users (username, email, password_hash, salt)
values ('username1', 'd.karpukhin@mail.ru', '07d0c1159c0029eb7d5c84328a8ebe84b073c3eb1f9e63d35e26ef5243234480', 'POSOSI');

create table if not exists Ratings(
                                      user_id bigint not null,
                                      elo_classic int not null,

                                      constraint fk_user_id foreign key (user_id) references bugs.public.users(id)
);

insert into Ratings (user_id, elo_classic) values (1, 1000);