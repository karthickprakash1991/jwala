CREATE TABLE app (ID INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), documentRoot VARCHAR(255), loadBalanceAcrossServers BOOLEAN, name VARCHAR(255) NOT NULL, secure BOOLEAN, warName VARCHAR(255), warPath VARCHAR(255), webAppContext VARCHAR(255) NOT NULL, GROUP_ID BIGINT, CONSTRAINT U_APP_NAME UNIQUE (name));
CREATE TABLE APP_CONFIG_TEMPLATE (ID INTEGER NOT NULL IDENTITY, TEMPLATE_CONTENT VARCHAR(2147483647) NOT NULL, TEMPLATE_NAME VARCHAR(255) NOT NULL, APP_ID BIGINT, JVM_ID BIGINT, CONSTRAINT U_PP_CPLT_APP_ID UNIQUE (APP_ID, TEMPLATE_NAME, JVM_ID));
CREATE TABLE current_state (ID BIGINT NOT NULL, TYPE VARCHAR(255) NOT NULL, AS_OF TIMESTAMP NOT NULL, MESSAGE VARCHAR(2147483647), STATE VARCHAR(255) NOT NULL, PRIMARY KEY (ID, TYPE));
CREATE TABLE grp (ID INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), name VARCHAR(255) NOT NULL, state VARCHAR(255), stateUpdated TIMESTAMP, CONSTRAINT U_GRP_NAME UNIQUE (name));
CREATE TABLE GRP_JVM (GROUP_ID BIGINT, JVM_ID BIGINT, CONSTRAINT U_GRP_JVM_GROUP_ID UNIQUE (GROUP_ID, JVM_ID));
CREATE TABLE history (id INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), event VARCHAR(10000), EVENTTYPE VARCHAR(2), serverName VARCHAR(255) NOT NULL, groupId BIGINT, CONSTRAINT U_HISTORY_ID UNIQUE (id));
CREATE TABLE jvm (id INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), ajpPort INTEGER NOT NULL, hostName VARCHAR(255), httpPort INTEGER NOT NULL, httpsPort INTEGER, name VARCHAR(255) NOT NULL, redirectPort INTEGER NOT NULL, shutdownPort INTEGER NOT NULL, statusPath VARCHAR(255) NOT NULL, systemProperties VARCHAR(255), CONSTRAINT U_JVM_NAME UNIQUE (name));
CREATE TABLE JVM_CONFIG_TEMPLATE (ID INTEGER NOT NULL IDENTITY, TEMPLATE_CONTENT VARCHAR(2147483647) NOT NULL, TEMPLATE_NAME VARCHAR(255) NOT NULL, JVM_ID BIGINT, CONSTRAINT U_JVM_PLT_JVM_ID UNIQUE (JVM_ID, TEMPLATE_NAME));
CREATE TABLE OPENJPA_SEQUENCE_TABLE (ID TINYINT NOT NULL, SEQUENCE_VALUE BIGINT, PRIMARY KEY (ID));
CREATE TABLE RESOURCE_INSTANCE (RESOURCE_INSTANCE_ID INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), name VARCHAR(255) NOT NULL, RESOURCE_INSTANCE_NAME VARCHAR(255) NOT NULL, RESOURCE_TYPE_NAME VARCHAR(255), GROUP_ID BIGINT, CONSTRAINT U_RSRCTNC_NAME UNIQUE (name), CONSTRAINT U_RSRCTNC_RESOURCE_INSTANCE_ID UNIQUE (RESOURCE_INSTANCE_ID, name, GROUP_ID));
CREATE TABLE RESOURCE_INSTANCE_ATTRIBUTES (RESOURCE_INSTANCE_ID BIGINT, ATTRIBUTE_KEY VARCHAR(255) NOT NULL, ATTRIBUTE_VALUE VARCHAR(255));
CREATE TABLE webserver (id INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, updateBy VARCHAR(255), docRoot VARCHAR(255) NOT NULL, host VARCHAR(255), httpConfigFile VARCHAR(255) NOT NULL, httpsPort INTEGER, name VARCHAR(255), port INTEGER, statusPath VARCHAR(255) NOT NULL, svrRoot VARCHAR(255) NOT NULL, CONSTRAINT U_WBSRVER_NAME UNIQUE (name));
CREATE TABLE WEBSERVER_CONFIG_TEMPLATE (ID INTEGER NOT NULL IDENTITY, TEMPLATE_CONTENT VARCHAR(2147483647) NOT NULL, TEMPLATE_NAME VARCHAR(255) NOT NULL, WEBSERVER_ID BIGINT, CONSTRAINT U_WBSRPLT_WEBSERVER_ID UNIQUE (WEBSERVER_ID, TEMPLATE_NAME));
CREATE TABLE WEBSERVER_GRP (GROUP_ID BIGINT, WEBSERVER_ID BIGINT, CONSTRAINT U_WBSRGRP_WEBSERVER_ID UNIQUE (WEBSERVER_ID, GROUP_ID));
ALTER TABLE app ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE APP_CONFIG_TEMPLATE ADD FOREIGN KEY (APP_ID) REFERENCES app (ID) ON DELETE CASCADE;
ALTER TABLE GRP_JVM ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE GRP_JVM ADD FOREIGN KEY (JVM_ID) REFERENCES jvm (id);
ALTER TABLE history ADD FOREIGN KEY (groupId) REFERENCES grp (ID);
ALTER TABLE JVM_CONFIG_TEMPLATE ADD FOREIGN KEY (JVM_ID) REFERENCES jvm (id) ON DELETE CASCADE;
ALTER TABLE RESOURCE_INSTANCE ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE RESOURCE_INSTANCE_ATTRIBUTES ADD FOREIGN KEY (RESOURCE_INSTANCE_ID) REFERENCES RESOURCE_INSTANCE (RESOURCE_INSTANCE_ID);
ALTER TABLE WEBSERVER_CONFIG_TEMPLATE ADD FOREIGN KEY (WEBSERVER_ID) REFERENCES webserver (id) ON DELETE CASCADE;
ALTER TABLE WEBSERVER_GRP ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE WEBSERVER_GRP ADD FOREIGN KEY (WEBSERVER_ID) REFERENCES webserver (id);
