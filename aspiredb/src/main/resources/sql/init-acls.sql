create table ACLENTRY (ID BIGINT not null auto_increment, GRANTING TINYINT not null, MASK INTEGER not null, ACE_ORDER INTEGER not null, SID_FK BIGINT not null, OBJECTIDENTITY_FK BIGINT, primary key (ID));
create table ACLOBJECTIDENTITY (ID BIGINT not null auto_increment, OBJECT_ID BIGINT not null, OBJECT_CLASS VARCHAR(255) character set latin1 collate latin1_swedish_ci not null, ENTRIES_INHERITING TINYINT not null, OWNER_SID_FK BIGINT not null, PARENT_OBJECT_FK BIGINT, primary key (ID));
create table ACLSID (ID BIGINT not null auto_increment, class varchar(255) not null, PRINCIPAL VARCHAR(255) character set latin1 collate latin1_swedish_ci unique, GRANTED_AUTHORITY VARCHAR(255) character set latin1 collate latin1_swedish_ci unique, primary key (ID));
alter table ACLENTRY add index FKBE0F46285EB7C2A7 (SID_FK), add constraint FKBE0F46285EB7C2A7 foreign key (SID_FK) references ACLSID (ID);
alter table ACLENTRY add index ACL_ENTRY_OBJECTIDENTITY_FKC (OBJECTIDENTITY_FK), add constraint ACL_ENTRY_OBJECTIDENTITY_FKC foreign key (OBJECTIDENTITY_FK) references ACLOBJECTIDENTITY (ID);
alter table ACLOBJECTIDENTITY add index FK79E443E74551EDB3 (OWNER_SID_FK), add constraint FK79E443E74551EDB3 foreign key (OWNER_SID_FK) references ACLSID (ID);
alter table ACLOBJECTIDENTITY add index FK79E443E7D2DE55AA (PARENT_OBJECT_FK), add constraint FK79E443E7D2DE55AA foreign key (PARENT_OBJECT_FK) references ACLOBJECTIDENTITY (ID);

alter table ACLOBJECTIDENTITY add unique key acloid (OBJECT_CLASS,OBJECT_ID);
alter table ACLOBJECTIDENTITY add key objectclasskey (OBJECT_CLASS);
alter table ACLOBJECTIDENTITY add key oidkey (OBJECT_ID);

-- The table for this are now created from our hibernate config for ACLs.

-- Base SIDs we'll need these (not all used by this script; the others would be inserted automagically when needed, but this
-- gives them predictable ids). Principal names must match init-entities script
insert into ACLSID (ID, class, GRANTED_AUTHORITY) values(1, "GrantedAuthoritySid", "GROUP_ADMIN");
insert into ACLSID (ID, class, GRANTED_AUTHORITY) values(2, "GrantedAuthoritySid", "GROUP_USER"); 
insert into ACLSID (ID, class, GRANTED_AUTHORITY) values(3, "GrantedAuthoritySid", "GROUP_AGENT"); 
insert into ACLSID (ID, class, GRANTED_AUTHORITY) values(4, "GrantedAuthoritySid", "IS_AUTHENTICATED_ANONYMOUSLY"); 
insert into ACLSID (ID, class, PRINCIPAL) values(5, "PrincipalSid", "administrator"); 
insert into ACLSID (ID, class, PRINCIPAL) values(6, "PrincipalSid", "aspiredbAgent");
insert into ACLSID (ID, class, PRINCIPAL) values(7, "PrincipalSid", "user");


-- Add object identity (OI) for the admin user. There is no parent object, the owner = the administrator; non-inheriting.
insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (1, "ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User", 1, 1, 0);

-- OI for the Admin group (assumes id of this group=1, see init-entities.sql)
insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (2, "ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup", 1, 1, 0);

-- OI for the Agent group (assumes id of this group=2, see init-entities.sql)
insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (3, "ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup", 2, 1, 0);

-- OI for the User group (assumes id of group=3, see init-entities.sql)
insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (4, "ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup", 3, 1, 0);

-- Add object identity (OI) for the agent user. There is no parent object, the owner = the administrator; non-inheriting.
insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (5, "ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User", 2, 1, 0);

insert into ACLOBJECTIDENTITY (ID, OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values (6, "ubc.pavlab.aspiredb.server.model.Query", 1, 1, 0);

insert into ACLOBJECTIDENTITY (OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values ("ubc.pavlab.aspiredb.server.model.SNV", 1, 1, 0);

insert into ACLOBJECTIDENTITY (OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values ("ubc.pavlab.aspiredb.server.model.CNV", 1, 1, 0);

insert into ACLOBJECTIDENTITY (OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values ("ubc.pavlab.aspiredb.server.model.Phenotype", 1, 1, 0);

insert into ACLOBJECTIDENTITY (OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values ("ubc.pavlab.aspiredb.server.model.Subject", 1, 1, 0);

insert into ACLOBJECTIDENTITY (OBJECT_CLASS, OBJECT_ID, OWNER_SID_FK, ENTRIES_INHERITING) values ("ubc.pavlab.aspiredb.server.model.Project", 1, 1, 0);

--
-- give GROUP_ADMIN admin priv on everything - we don't need to give it to a specific user.
--
-- user 1 = administrator, grant admin to sid=1 (GROUP_ADMIN)
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (1, 1, 16, 1, 1, 1);
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (2, 1, 16, 1, 2, 1);
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (3, 1, 16, 1, 3, 1);
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (4, 1, 16, 1, 4, 1);
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (5, 1, 16, 1, 5, 1);
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (6, 1, 16, 1, 5, 5);

-- Give GROUP_USER READ priv on user group sid=2, oi=2, perm=1. (is this necessary?)
-- insert into ACLENTRY (id, ace_order, mask, granting, audit_success, audit_failure, ACLOBJECTIDENTITY, sid) values (6, 2, 1, 1, 0, 0, 2, 2);

-- give user administrator admin priv on themselves (in addition to the group privileges)
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (7, 2, 16, 1, 1, 5);

-- give agent admin priv on himself.(sid=6). (no group privileges)
insert into ACLENTRY (ID, ACE_ORDER, MASK, GRANTING, OBJECTIDENTITY_FK, SID_FK) values (8, 2, 16, 1, 5, 6);
