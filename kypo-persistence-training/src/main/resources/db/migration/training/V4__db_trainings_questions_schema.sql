create table question (
    question_id bigserial not null,
    question_type varchar(64) not null,
    order_in_assessment int4 not null,
    points int4 not null,
    penalty int4 not null,
    text text not null,
    answer_required boolean,
    assessment_level_id  int8 not null,
    primary key (question_id),
    foreign key (assessment_level_id) references assessment_level
);

create table question_choice (
    question_choice_id bigserial not null,
    correct boolean not null,
    text text not null,
    order_in_question int4 not null,
    question_id  int8 not null,
    primary key (question_choice_id),
    foreign key (question_id) references question
);

create table extend_matching_option (
    extend_matching_option_id bigserial not null,
    text text not null,
    order_in_row int4 not null,
    question_id  int8 not null,
    primary key (extend_matching_option_id),
    foreign key (question_id) references question
);

create table extended_matching_statement (
    extended_matching_statement_id bigserial not null,
    text text not null,
    order_in_column int4 not null,
    question_id  int8 not null,
    extended_matching_option_id int8,
    primary key (extended_matching_statement_id),
    foreign key (question_id) references question,
    foreign key (extended_matching_option_id) references extend_matching_option
);

create table question_answer (
    question_id int8 not null,
    training_run_id int8 not null,
    primary key (question_id, training_run_id),
    foreign key (question_id) references question,
    foreign key (training_run_id) references training_run,
    unique (question_id, training_run_id)
);

create table question_answers (
    question_id int8 not null,
    training_run_id int8 not null,
    answer varchar(255) not null,
    foreign key (question_id, training_run_id) references question_answer
);

alter table assessment_level drop column questions;
alter table training_run drop column total_score;
alter table training_run add column total_game_score int4 default (0);
alter table training_run add column total_assessment_score int4 default (0);
