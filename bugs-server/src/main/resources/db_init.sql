create table if not exists Users(
    id bigserial primary key not null,
    username varchar(32) not null unique,
    email varchar(128) not null,
    password_hash varchar(128) not null,
    salt varchar(32) not null
);

create table if not exists Ratings(
    user_id bigint not null,
    elo_classic int not null,

    constraint fk_user_id foreign key (user_id) references bugs.public.users(id)
);

create or replace procedure register_user(
        _username varchar,
        _email varchar,
        _passhash varchar,
        _salt varchar)
    language plpgsql

as $$
declare
    _id bigint;
    begin
        insert into users (username, email, password_hash, salt)
        values (_username, _email, _passhash, _salt);

        select MAX(id) from users into _id;

        insert into ratings (user_id, elo_classic)
        values (_id, 1000);
    end;$$;

-- password: dkarp
call register_user('example_user1', 'd.karpukhin@mail.ru', 'c9ec411c6c35b50e102d958ebf1a908b3907c04368fccd1a4465af7068ae2636', 'sLArzVvmfG');
call register_user('example_user2', 'd.karpukhin@mail.ru', 'c9ec411c6c35b50e102d958ebf1a908b3907c04368fccd1a4465af7068ae2636', 'sLArzVvmfG');
call register_user('example_user3', 'd.karpukhin@mail.ru', 'c9ec411c6c35b50e102d958ebf1a908b3907c04368fccd1a4465af7068ae2636', 'sLArzVvmfG');

create or replace procedure set_classic_elo(
    _username varchar,
    _new_elo int
) language plpgsql as $$
    begin
        update ratings set elo_classic = _new_elo where user_id = (select id from users where username = _username);
        commit;
    end;$$;

create or replace function get_classic_elo(
    _username varchar
) returns int
    language plpgsql as $$
    begin
        return (select elo_classic as elo from users join ratings r on users.id = r.user_id where username = _username);
    end;$$;
