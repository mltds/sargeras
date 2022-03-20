-- Create syntax for TABLE 'saga_record'
CREATE TABLE `saga_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Saga ID',
  `app_name` varchar(200) NOT NULL DEFAULT '' COMMENT '应用/系统名',
  `biz_name` varchar(200) NOT NULL DEFAULT '' COMMENT '业务名',
  `biz_id` varchar(200) NOT NULL DEFAULT '' COMMENT '业务ID',
  `status` varchar(100) NOT NULL DEFAULT '' COMMENT 'Saga 状态',
  `trigger_id` varchar(100) DEFAULT NULL COMMENT '触发ID，获取锁时必须，释放锁时删除',
  `trigger_count` int(11) NOT NULL COMMENT '已执行次数，每次重新启动加一',
  `next_trigger_time` datetime NOT NULL COMMENT '期望的下一次触发时间',
  `locked` tinyint(4) NOT NULL DEFAULT '0' COMMENT '锁状态，0未锁，1已锁',
  `lock_expire_time` datetime DEFAULT NULL COMMENT '锁过期时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间，创建时间加业务超时时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最新修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_biz_id_uk` (`app_name`,`biz_name`,`biz_id`),
  KEY `next_trigger_time` (`next_trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'saga_record_param'
CREATE TABLE `saga_record_param` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `parameter_type` varchar(100) NOT NULL DEFAULT '',
  `parameter_name` varchar(100) NOT NULL DEFAULT '',
  `parameter` longblob,
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'saga_record_result'
CREATE TABLE `saga_record_result` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `cls` varchar(100) NOT NULL DEFAULT '',
  `result` longblob,
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'saga_tx_record'
CREATE TABLE `saga_tx_record` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `cls` varchar(100) NOT NULL DEFAULT '',
  `method` varchar(100) NOT NULL DEFAULT '',
  `compensate_method` varchar(100) DEFAULT NULL,
  `parameter_types` varchar(2000) NOT NULL DEFAULT '',
  `status` varchar(100) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'saga_tx_record_param'
CREATE TABLE `saga_tx_record_param` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `tx_record_id` bigint(20) NOT NULL,
  `parameter_type` varchar(100) NOT NULL DEFAULT '',
  `parameter_name` varchar(100) NOT NULL DEFAULT '',
  `parameter` longblob,
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_tx_record_id` (`tx_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'saga_tx_record_result'
CREATE TABLE `saga_tx_record_result` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `tx_record_id` bigint(20) NOT NULL,
  `cls` varchar(100) NOT NULL DEFAULT '',
  `result` longblob,
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tx_record_id` (`tx_record_id`),
  KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;