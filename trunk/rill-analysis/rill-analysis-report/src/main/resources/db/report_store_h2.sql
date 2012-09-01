create table report (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(128) not null,
    paramsXStrem varchar(1024) default null,
    cronExpression varchar(200) default null,
    addDate timestamp default CURRENT_TIMESTAMP,
    primary key (id)
);

CREATE TABLE report_byte (
  id INT NOT NULL AUTO_INCREMENT,
  content BLOB NOT NULL,
  report_id int not null,
  PRIMARY KEY  (id)
);