create table RIGEL_WF_TRANSITION_TAKE_TRACE (
    ID_ int not null AUTO_INCREMENT,
    TRANSITION_ID_ varchar(64) not null,
    TRANSITION_NAME_ varchar(255),
    PROC_INST_ID_ varchar(64) not null,
    CREATE_TIME_ timestamp default CURRENT_TIMESTAMP,
    primary key (ID_)
);

create index RIGEL_WF_TRANSITION_TAKE_TRACE_PIID on RIGEL_WF_TRANSITION_TAKE_TRACE(PROC_INST_ID_);

create table RIGEL_WF_TASK_COMPLETE_TRACE (
    ID_ int not null AUTO_INCREMENT,
    TASK_ID_ varchar(64) not null,
	PARAM_MAP_SERIALIZED varchar(4000),
	NEXT_TASK_IDS_ varchar(4000),
    CREATE_TIME_ timestamp default CURRENT_TIMESTAMP,
    primary key (ID_)
);

create index RIGEL_WF_TASK_COMPLETE_TRACE_TID on RIGEL_WF_TASK_COMPLETE_TRACE(TASK_ID_);