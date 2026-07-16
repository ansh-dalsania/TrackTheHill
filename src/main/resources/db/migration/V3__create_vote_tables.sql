-- V3: Creates the vote and member_vote tables for House roll call votes.
-- vote represents a single roll call event (e.g. "House vote #240 in the 1st
-- session of the 119th Congress"). member_vote represents one member's
-- recorded position on that vote.
--
-- bill_id is nullable because not every vote ties to a bill

CREATE TABLE vote (
    id                 VARCHAR(20) PRIMARY KEY,  -- "{congress}-{session}-{rollCallNumber}"
    congress           INTEGER      NOT NULL,
    session            INTEGER      NOT NULL,
    roll_call_number   INTEGER      NOT NULL,
    bill_id            VARCHAR(30) REFERENCES bill(id),
    vote_question      TEXT,
    vote_type          VARCHAR(50),
    result             VARCHAR(50),
    vote_date          TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE member_vote (
    id                  BIGSERIAL PRIMARY KEY,
    vote_id             VARCHAR(20)  NOT NULL REFERENCES vote(id),
    member_bioguide_id  VARCHAR(10)  NOT NULL REFERENCES member(bioguide_id),
    position            VARCHAR(20)  NOT NULL,   -- 'Yea', 'Nay', 'Present', 'Not Voting'
    party_at_vote       VARCHAR(20),
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),

    -- Prevents duplicate vote records for the same member on the same roll call
    UNIQUE (vote_id, member_bioguide_id)
);