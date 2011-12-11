create table RILL_WF_TRANSITION_TAKE_TRACE (
    ID_ int(10) not null auto_increment,
    TRANSITION_ID_ varchar(64) not null,
    TRANSITION_NAME_ varchar(255),
    PROC_INST_ID_ varchar(64) not null,
    CREATE_TIME_ timestamp default CURRENT_TIMESTAMP,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index RILL_WF_TRANSITION_TAKE_TRACE_PIID on RILL_WF_TRANSITION_TAKE_TRACE(PROC_INST_ID_);