-- Fix MySQL reserved keyword error: column `escaped` breaks Hibernate INSERT.
-- Run this if shelter_daily_labor already has exploited/escaped columns.

ALTER TABLE `shelter_daily_labor`
  CHANGE COLUMN `exploited` `is_exploited` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否压榨';

ALTER TABLE `shelter_daily_labor`
  CHANGE COLUMN `escaped` `is_escaped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逃役';
