-- V8: Creates campaign_finance_summary, one row per member per election cycle,
-- aggregated from FEC's /candidate/{id}/totals/ endpoint.

CREATE TABLE campaign_finance_summary (
    id                              BIGSERIAL PRIMARY KEY,
    member_bioguide_id              VARCHAR(10)  NOT NULL REFERENCES member(bioguide_id),
    cycle                           INTEGER      NOT NULL,
    individual_contributions        NUMERIC(14,2),
    pac_contributions               NUMERIC(14,2),
    party_contributions             NUMERIC(14,2),
    total_receipts                  NUMERIC(14,2),
    total_disbursements             NUMERIC(14,2),
    cash_on_hand                    NUMERIC(14,2),
    coverage_end_date               DATE,
    created_at                      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP    NOT NULL DEFAULT now(),

    UNIQUE (member_bioguide_id, cycle)
);