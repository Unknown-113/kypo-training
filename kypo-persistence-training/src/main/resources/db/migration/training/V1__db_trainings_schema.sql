create table abstract_level (
   id  bigserial not null,
    estimated_duration int8,
    max_score int4 not null,
    order_in_training_definition int4 not null,
    title varchar(255) not null,
    training_definition_id int8,
    minimal_possible_solve_time int8 default (null),
    primary key (id)
);

create table access_token (
   id  bigserial not null,
    access_token varchar(255) not null,
    primary key (id)
);

create table access_level (
    cloud_content text not null,
    local_content text not null,
    passkey varchar(255) not null,
    id int8 not null,
    primary key (id),
    foreign key (id) references abstract_level
);

create table assessment_level (
   assessment_type varchar(128) not null,
    instructions varchar(1023) not null,
    id int8 not null,
    primary key (id)
);

create table beta_testing_group (
   id  bigserial not null,
    primary key (id)
);

create table beta_testing_group_user_ref (
   beta_testing_group_id int8 not null,
    user_ref_id int8 not null,
    primary key (beta_testing_group_id, user_ref_id)
);

create table training_level (
   attachments bytea,
    content text not null,
    answer varchar(255),
    incorrect_answer_limit int4,
    solution text not null,
    solution_penalized boolean not null,
    id int8 not null,
    answer_variable_name varchar(255),
    variant_answers boolean default (false),
    reference_solution text DEFAULT '[]',
    commands_required boolean not null default (true),
    primary key (id)
);

create table hint (
    id  bigserial not null,
    content text not null,
    hint_penalty int4 not null,
    title varchar(255) not null,
    training_level_id int8,
    order_in_level int4 not null,
    primary key (id)
);

create table info_level (
   content text not null,
    id int8 not null,
    primary key (id)
);

create table training_definition (
   id  bigserial not null,
    description text,
    last_edited timestamp not null,
    last_edited_by varchar(127) not null default '',
    estimated_duration int8,
    outcomes bytea,
    prerequisites bytea,
    state varchar(128) not null,
    title varchar(255) not null,
    beta_testing_group_id int8,
    created_at timestamp not null default current_timestamp,
    primary key (id)
);

create table training_definition_user_ref (
   training_definition_id int8 not null,
    user_ref_id int8 not null,
    primary key (training_definition_id, user_ref_id)
);

create table training_instance (
   id  bigserial not null,
    access_token varchar(255) not null,
    end_time timestamp not null,
    pool_id int8,
    start_time timestamp not null,
    title varchar(255) not null,
    training_definition_id int8,
    last_edited timestamp not null default CURRENT_TIMESTAMP,
    last_edited_by varchar(127) not null default '',
    local_environment boolean default (false),
    sandbox_definition_id int8 default (null),
    backward_mode boolean not null default (false),
    show_stepper_bar boolean not null default true,
    primary key (id)
);

create table training_instance_user_ref (
   training_instance_id int8 not null,
    user_ref_id int8 not null,
    primary key (training_instance_id, user_ref_id)
);

create table training_run (
   id  bigserial not null,
    assessment_responses text,
    max_level_score int4,
    end_time timestamp not null,
    event_log_reference varchar(255),
    incorrect_answer_count int4 not null,
    level_answered boolean,
    solution_taken boolean not null,
    start_time timestamp not null,
    state varchar(128) not null,
    current_level_id int8 not null,
    user_ref_id int8 not null,
    sandbox_instance_ref_id varchar(36) null,
    training_instance_id int8 not null,
    previous_sandbox_instance_ref_id varchar(36) null,
    current_penalty int4,
    total_training_score int4 default (0),
    total_assessment_score int4 default (0),
    has_detection_event boolean not null default (false),
    sandbox_instance_allocation_id int8,
    primary key (id)
);

create table hint_info(
  training_run_id bigserial not null,
  training_level_id bigserial not null,
  hint_id bigserial not null,
  hint_title varchar(128) not null,
  hint_content varchar(4096) not null,
  order_in_level int4 not null
);

create table user_ref (
   id  bigserial not null,
    user_ref_id int8 not null unique,
    primary key (id)
);

create table attachment (
    id bigserial not null,
    content varchar(255) not null ,
    creation_time timestamp not null,
    training_level_id int8,
    primary key (id)

);

create table training_run_acquisition_lock (
    id bigserial not null,
    participant_ref_id bigserial not null,
    training_instance_id bigserial not null,
    creation_time timestamp not null,
    primary key (id)
);

create table question (
    question_id bigserial not null,
    question_type varchar(64) not null,
    order_in_assessment int4 not null,
    points int4 not null,
    penalty int4 not null,
    text varchar(255) not null,
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
    answer varchar (1023) not null,
    foreign key (question_id, training_run_id) references question_answer
);

create table submission (
    id bigserial not null,
    provided text not null,
    type varchar(255) not null,
    level_id int8 not null,
    training_run_id int8 not null,
    date timestamp not null,
    ip_address varchar(255) not null,
    primary key (id),
    foreign key (level_id) references abstract_level,
    foreign key (training_run_id) references training_run
);

create table mitre_technique (
    id  bigserial not null,
    technique_key varchar(64) not null unique,
    primary key (id)
);

create table training_level_mitre_technique (
    training_level_id int8 not null,
    mitre_technique_id int8 not null,
    primary key (training_level_id, mitre_technique_id),
    foreign key (training_level_id) references training_level,
    foreign key (mitre_technique_id) references mitre_technique
);

create table expected_commands (
    training_level_id int8 not null,
    command varchar(128) not null,
    primary key (training_level_id, command),
    foreign key (training_level_id) references training_level
);

create table solution_info(
    training_run_id bigserial not null,
    training_level_id bigserial not null,
    solution_content text not null,
    foreign key (training_run_id) references training_run
);


alter table access_token
   add constraint UK_qglhb4xi0iwstguebaliifr1n unique (access_token);

alter table training_definition
   add constraint UK_8k8if9s1vogmedxasdadcr4tb unique (beta_testing_group_id);

alter table training_instance
   add constraint UK_b81w12g91hiuhdvsmoanyel6m unique (access_token);

alter table training_run_acquisition_lock
   add constraint UK_b81w12g91hiuhdasdgfcyel6m unique (participant_ref_id, training_instance_id);

alter table hint_info
   add constraint FKi9smgl25av8pb1yv3fl4ycby0
   foreign key (training_run_id)
   references training_run;

alter table abstract_level
   add constraint FK24361n3estpsxei7bx7sfvcxs
   foreign key (training_definition_id)
   references training_definition;

alter table assessment_level
   add constraint FK7jxec7ef838ovnrnfw73kh95
   foreign key (id)
   references abstract_level;

alter table beta_testing_group_user_ref
   add constraint FK4ph4xvdeggto33vg4g6cs18s1
   foreign key (user_ref_id)
   references user_ref;

alter table beta_testing_group_user_ref
   add constraint FKf01kbc9ae5599gnci7d0j2ldx
   foreign key (beta_testing_group_id)
   references beta_testing_group;

alter table training_level
   add constraint FKrg7pvp6aqm4gxshunqq77noma
   foreign key (id)
   references abstract_level;

alter table hint
   add constraint FKikeediy8uqdf22egpfmdaboor
   foreign key (training_level_id)
   references training_level;

alter table info_level
   add constraint FKa9ssogmfce6duhtlm8chrqcc4
   foreign key (id)
   references abstract_level;

alter table training_definition
   add constraint FKdps9cuy3u6c6v1n8igr9hnu1r
   foreign key (beta_testing_group_id)
   references beta_testing_group;

alter table training_definition_user_ref
   add constraint FKq5ejeyb8ced1s2t9lv4ld1uyl
   foreign key (user_ref_id)
   references user_ref;

alter table training_definition_user_ref
   add constraint FK99adq71p6nym0emx1xvr4qk4
   foreign key (training_definition_id)
   references training_definition;

alter table training_instance
   add constraint FK28s41pqjyqwrni7thb54tidru
   foreign key (training_definition_id)
   references training_definition;

alter table training_instance_user_ref
   add constraint FK53k0sdbkfgu7ddn902b3x0fsy
   foreign key (user_ref_id)
   references user_ref;

alter table training_instance_user_ref
   add constraint FKj92s3xyn59494b3kwxbmbs9ct
   foreign key (training_instance_id)
   references training_instance;

alter table training_run
   add constraint FKddva9h2olm0h0aj9veb6jfe9r
   foreign key (current_level_id)
   references abstract_level;

alter table training_run
   add constraint FKmfyx8wi2fu400w1h6gikyp9cy
   foreign key (user_ref_id)
   references user_ref;

alter table training_run
   add constraint FK7vajehsxurugwfg363f4ppb0s
   foreign key (training_instance_id)
   references training_instance;

   alter table attachment
   add constraint FKikeediy8uqdf22egpfmdaaar
   foreign key (training_level_id)
   references training_level;
   
CREATE INDEX abstract_level_order_in_training_definition_index
ON abstract_level (order_in_training_definition);

CREATE UNIQUE INDEX access_token_access_token_index
ON access_token (access_token);

CREATE UNIQUE INDEX training_instance_access_token_index
ON training_instance (access_token);

CREATE INDEX training_instance_start_time_and_end_time_index
ON training_instance (start_time, end_time DESC);

CREATE INDEX training_definition_state_index
ON training_definition (state);

CREATE INDEX training_run_start_time_and_end_time_index
ON training_run (start_time, end_time DESC);

create index training_level_expected_commands_index
on expected_commands (training_level_id);

CREATE SEQUENCE extend_matching_option_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE extended_matching_statement_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE question_choice_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE question_seq AS bigint INCREMENT 50 MINVALUE 1;
