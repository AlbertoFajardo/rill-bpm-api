create table RILL_WF_TRANSITION_TAKE_TRACE (
    ID_ int not null AUTO_INCREMENT,
    TRANSITION_ID_ varchar(64) not null,
    TRANSITION_NAME_ varchar(255),
    PROC_INST_ID_ varchar(64) not null,
    CREATE_TIME_ timestamp default CURRENT_TIMESTAMP,
    primary key (ID_)
);

create index RILL_WF_TRANSITION_TAKE_TRACE_PIID on RILL_WF_TRANSITION_TAKE_TRACE(PROC_INST_ID_);