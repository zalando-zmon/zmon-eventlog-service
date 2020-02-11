CREATE SCHEMA zmon_eventlog;

CREATE TABLE zmon_eventlog.event_types(
  et_id serial,
  et_name text,
  PRIMARY KEY (et_id)
);

CREATE TABLE zmon_eventlog.events(
  e_type_id int,
  e_created timestamp,
  e_data jsonb
);

CREATE INDEX ON zmon_eventlog.events (((e_data->'checkId')::text), e_created);
CREATE INDEX ON zmon_eventlog.events (((e_data->'alertId')::text), e_created);

INSERT INTO zmon_eventlog.event_types VALUES (213263,'GROUP_MODIFIED'),
                                             (212994,'ALERT_ENDED'),
                                             (212993,'ALERT_STARTED'),
                                             (212996,'ALERT_ENTITY_ENDED'),
                                             (212998,'DOWNTIME_ENDED'),
                                             (213000,'ACCESS_DENIED'),
                                             (212995,'ALERT_ENTITY_STARTED'),
                                             (212999,'SMS_SENT'),
                                             (212997,'DOWNTIME_STARTED'),
                                             (213253,'ALERT_COMMENT_REMOVED'),
                                             (213255,'CHECK_DEFINITION_UPDATED'),
                                             (213256,'ALERT_DEFINITION_CREATED'),
                                             (213251,'TRIAL_RUN_SCHEDULED'),
                                             (213257,'ALERT_DEFINITION_UPDATED'),
                                             (213262,'INSTANTANEOUS_ALERT_EVALUATION_SCHEDULED'),
                                             (213254,'CHECK_DEFINITION_CREATED'),
                                             (213261,'DASHBOARD_UPDATED'),
                                             (213260,'DASHBOARD_CREATED'),
                                             (213258,'CHECK_DEFINITION_DELETED'),
                                             (213249,'DOWNTIME_SCHEDULED'),
                                             (213250,'DOWNTIME_REMOVED'),
                                             (213252,'ALERT_COMMENT_CREATED'),
                                             (213505,'CHECK_DEFINITION_IMPORT_FAILED'),
                                             (213259,'ALERT_DEFINITION_DELETED'),
                                             -- Paging events
                                             (213760,'PAGE_TRIGGERED'),       -- 0x34300
                                             (213761,'PAGE_ACKNOWLEDGED'),    -- 0x34301
                                             (213762,'PAGE_UNACKNOWLEDGED'),  -- 0x34302
                                             (213763,'PAGE_RESOLVED');        -- 0x34303
