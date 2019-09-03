create table users
(
    id int auto_increment
        primary key,
    name varchar(100) null,
    email varchar(100) null,
    country varchar(100) null,
    key_cookie varchar(255) null
);

create table minds
(
    id int auto_increment
        primary key,
    text varchar(4000) null,
    user_id int null,
    time timestamp null,
    constraint minds_users__fk
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

create table friendship
(
    user_id int not null,
    friend_id int not null,
    primary key (user_id, friend_id),
    constraint FK_FRIEND
        foreign key (friend_id) references users (id)
            on delete cascade,
    constraint FK_USER
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
);

