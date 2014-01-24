-- Add some indices. Some of these are very important to performance
-- $Id $
alter table PHENOTYPE add INDEX IDX_NAME_VALUES_SUBJECT_FK (NAME,VALUE,SUBJECT_FK);
