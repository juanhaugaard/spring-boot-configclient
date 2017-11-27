INSERT INTO PUBLIC.CONFIGURATION (APPLICATION_ID,PROFILE_ID,LABEL_ID,PROP_NAME,PROP_VALUE) VALUES
    ('pmm-test',NULL,NULL,'spring.datasource.url','jdbc:h2:tcp://10.117.10.42:8001/Config;mode=oracle'),
    ('pmm-test',NULL,NULL,'spring.datasource.username','sa'),
    ('pmm-test',NULL,NULL,'spring.datasource.password','sa'),
    ('pmm-test',NULL,NULL,'spring.jpa.hibernate.ddl-auto','update'),
    ('pmm-test',NULL,NULL,'spring.datasource.initialize','false'),
    ('pmm-test',NULL,NULL,'server.port','8010'),
    ('pmm-test',NULL,NULL,'spring.boot.admin.url','http://10.117.10.42:8000/admin'),
    ('pmm-test',NULL,NULL,'management.context-path','/actuator'),
    ('pmm-test',NULL,NULL,'management.security.enabled','false'),
    ('pmm-test',NULL,NULL,'spring.boot.admin.auto-registration','true'),
    ('pmm-test',NULL,NULL,'spring.boot.admin.auto-deregistration','true'),
    ('pmm-test',NULL,NULL,'security.ignored','/*');
