-- Add some indices. Some of these are very important to performance
-- $Id$
alter table PHENOTYPE add INDEX IDX_NAME_VALUES_SUBJECT_FK (NAME,VALUE,SUBJECT_FK);
--alter table GENOMIC_LOC add INDEX IDX_CHR_START_END (CHROMOSOME,START,END);
--alter table GENOMIC_LOC add INDEX IDX_CHR_BIN (CHROMOSOME,BIN);
--alter table GENOMIC_LOC add INDEX IDX_BIN_CHR (BIN,CHROMOSOME);
