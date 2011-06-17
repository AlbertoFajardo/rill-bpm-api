create table TB_CP (
    id int(10) NOT NULL auto_increment,
    processInstanceCnt int(10) NOT NULL,
    costTime int(10) NOT NULL,
perTaskCostTimeMax int(10) NOT NULL,
perTaskCostTimeMin int(10) NOT NULL,
perTaskCostTimeAvg int(10) NOT NULL,
    primary key (id)
);