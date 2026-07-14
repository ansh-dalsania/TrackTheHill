-- V2: Creates the bill table, representing a single bill or resolution in Congress.
-- id is constructed as "{congress}-{bill_type}-{bill_number}" (e.g. "119-hr-144"),
--
-- Fields are split into two groups:
--   1. List-level fields — cheap to fetch in bulk, populated on initial sync.
--   2. Detail-level fields — sponsor, policy area, introduced date — only fetched
--      for bills that pass "has progressed" filter, populated in a second pass.
--      Nullable since not every bill will be enriched.

CREATE TABLE bill (
    id                  VARCHAR(30) PRIMARY KEY,
    congress            INTEGER      NOT NULL,
    bill_type           VARCHAR(10)  NOT NULL,   -- e.g. 'hr', 's', 'hjres'
    bill_number         INTEGER      NOT NULL,
    title               TEXT         NOT NULL,
    origin_chamber      VARCHAR(10)  NOT NULL,   -- 'House' or 'Senate'
    latest_action_text  TEXT,
    latest_action_date  DATE,
    update_date         DATE,
    congress_gov_url    TEXT,

    -- Detail-level fields, populated during enrichment pass
    sponsor_bioguide_id VARCHAR(10) REFERENCES member(bioguide_id),
    introduced_date     DATE,
    policy_area         VARCHAR(100),
    summary             TEXT,

    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);