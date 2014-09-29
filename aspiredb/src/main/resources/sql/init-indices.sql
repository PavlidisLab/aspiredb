-- Add some indices. Some of these are very important to performance
-- $Id$
alter table PHENOTYPE add INDEX IDX_NAME_VALUES_SUBJECT_FK (NAME,VALUE,SUBJECT_FK);
--alter table GENOMIC_LOC add INDEX IDX_CHR_START_END (CHROMOSOME,START,END);
--alter table GENOMIC_LOC add INDEX IDX_CHR_BIN (CHROMOSOME,BIN);
--alter table GENOMIC_LOC add INDEX IDX_BIN_CHR (BIN,CHROMOSOME);

alter table SUBJECT add key patientidkey (PATIENT_ID);

alter table VARIANT add key uservariantkey (USERVARIANTID);
alter table VARIANT add key patientidkey (PATIENT_ID);
alter table VARIANT add unique key uservariant_patientid_key (USERVARIANTID,PATIENT_ID);

alter table GENOMIC_LOC add key binchrstartend (BIN,CHROMOSOME,START,END);

alter table VARIANT2VARIANTOVERLAP add key variantidprojid (VARIANTID,OVERLAP_PROJECTID);