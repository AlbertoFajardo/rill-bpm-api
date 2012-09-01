
DROP TABLE IF EXISTS report;
CREATE TABLE report (
  id INT(10) NOT NULL AUTO_INCREMENT COMMENT 'id，递增主键',
  name VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'report名称',
  paramsXStrem VARCHAR(1024) DEFAULT NULL COMMENT 'report参数',
  cronExpression VARCHAR(200) DEFAULT NULL COMMENT 'report调度参数',
  addDate timestamp default CURRENT_TIMESTAMP COMMENT '保存时间',
  PRIMARY KEY  (id)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='report表';

DROP TABLE IF EXISTS report_byte;
CREATE TABLE report_byte (
  id INT(10) NOT NULL AUTO_INCREMENT COMMENT 'id，递增主键',
  content blob NOT NULL COMMENT 'report内容',
  report_id INT(10) NOT NULL COMMENT 'FK，report id',
  PRIMARY KEY  (id)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='report内容表';