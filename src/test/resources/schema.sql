-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE HENDELSER (
    ID SERIAL PRIMARY KEY,
    TPNR VARCHAR(10) NOT NULL,
    HENDELSE_DATA JSONB
);