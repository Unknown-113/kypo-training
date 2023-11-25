create table abstract_detection_event (
   id  bigserial not null,
    training_instance_id int8 not null,
    cheating_detection_id int8 not null,
    level_id int8 not null,
    level_title varchar(255) not null,
    detected_at timestamp not null,
    participant_count int8 not null,
    detection_event_type text not null,
    primary key (id)
);

create table answer_similarity_detection_event (
   id  bigserial not null,
    answer varchar(255) not null,
    answer_owner varchar(255) not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table location_similarity_detection_event (
   id  bigserial not null,
    ip_address varchar(255) not null,
    dns varchar(255) not null,
    is_address_deploy boolean not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table time_proximity_detection_event (
   id  bigserial not null,
    threshold int8 not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table minimal_solve_time_detection_event (
   id  bigserial not null,
    minimal_solve_time int8 not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table no_commands_detection_event (
   id  bigserial not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table forbidden_commands_detection_event (
   id  bigserial not null,
    forbidden_commands text not null,
    primary key (id),
    foreign key (id) references abstract_detection_event
);

create table cheating_detection (
   id  bigserial not null,
    training_instance_id int8 not null,
    executed_by varchar(255) not null,
    execute_time timestamp not null,
    proximity_threshold int8,
    results int8 not null,
    current_state text not null,
    answer_similarity_state text not null,
    location_similarity_state text not null,
    time_proximity_state text not null,
    minimal_solve_time_state text not null,
    forbidden_commands_state text not null,
    no_commands_state text not null,
    primary key (id)
);

create table forbidden_command (
   id  bigserial not null,
    command varchar(255) not null,
    command_type varchar(255) not null,
    cheating_detection_id int8 not null,
    primary key (id),
    foreign key (cheating_detection_id) references cheating_detection
);

create table detection_event_participant (
   id  bigserial not null,
    user_id varchar(255) not null,
    ip_address varchar(255) not null,
    occurred_at timestamp,
    participant_name varchar(255),
    solved_in_time int8,
    detection_event_id int8 not null,
    primary key (id)
);
