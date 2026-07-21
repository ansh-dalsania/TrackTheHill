-- V11: Creates committee and committee_membership tables, sourced from
-- unitedstates/congress-legislators (Congress.gov's own API has no
-- committee-membership endpoint).

CREATE TABLE committee (
    id                  VARCHAR(20) PRIMARY KEY,  -- e.g. "hsgo00"
    name                VARCHAR(255) NOT NULL,
    chamber             VARCHAR(10)  NOT NULL,     -- 'House', 'Senate', 'Joint'
    parent_committee_id VARCHAR(20) REFERENCES committee(id),  -- null for top-level committees
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE committee_membership (
    id                  BIGSERIAL PRIMARY KEY,
    member_bioguide_id  VARCHAR(10)  NOT NULL REFERENCES member(bioguide_id),
    committee_id        VARCHAR(20)  NOT NULL REFERENCES committee(id),
    title               VARCHAR(50),   -- 'Chair', 'Ranking Member', null for regular member
    rank                INTEGER,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),

    UNIQUE (member_bioguide_id, committee_id)
);