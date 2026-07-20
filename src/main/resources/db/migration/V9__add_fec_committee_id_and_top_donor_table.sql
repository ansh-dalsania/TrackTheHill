-- V9: Adds fec_committee_id to member (their principal campaign committee for
-- the current cycle) and creates top_donor, storing the largest individual
-- itemized contributions to each member's committee, sourced from FEC's
-- schedule_a endpoint.

ALTER TABLE member ADD COLUMN fec_committee_id VARCHAR(20);

CREATE TABLE top_donor (
    id                    BIGSERIAL PRIMARY KEY,
    member_bioguide_id    VARCHAR(10)  NOT NULL REFERENCES member(bioguide_id),
    cycle                 INTEGER      NOT NULL,
    contributor_name      VARCHAR(255),
    contributor_employer  VARCHAR(255),
    contributor_occupation VARCHAR(255),
    contributor_state     VARCHAR(2),
    contribution_amount   NUMERIC(14,2),
    contribution_date     DATE,
    created_at            TIMESTAMP    NOT NULL DEFAULT now()
);