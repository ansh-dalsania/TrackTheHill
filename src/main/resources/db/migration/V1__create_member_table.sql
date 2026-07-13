CREATE TABLE member (
    bioguide_id       VARCHAR(10) PRIMARY KEY,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    party             VARCHAR(20)  NOT NULL,
    state             VARCHAR(2)   NOT NULL,
    district          VARCHAR(10),
    chamber           VARCHAR(10)  NOT NULL,
    headshot_url      TEXT,
    term_start_date   DATE,
    official_website_url TEXT,
    office_email      VARCHAR(255),
    in_office         BOOLEAN      NOT NULL DEFAULT TRUE,
    twitter_handle    VARCHAR(50),
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now()
);