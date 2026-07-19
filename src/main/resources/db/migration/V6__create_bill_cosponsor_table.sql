-- V6: Creates the bill_cosponsor join table, representing the many-to-many
-- relationship between bills and their cosponsors

CREATE TABLE bill_cosponsor (
    id                     BIGSERIAL PRIMARY KEY,
    bill_id                VARCHAR(30)  NOT NULL REFERENCES bill(id),
    member_bioguide_id     VARCHAR(10)  NOT NULL REFERENCES member(bioguide_id),
    sponsorship_date       DATE,
    is_original_cosponsor  BOOLEAN,
    created_at             TIMESTAMP    NOT NULL DEFAULT now(),

    UNIQUE (bill_id, member_bioguide_id)
);