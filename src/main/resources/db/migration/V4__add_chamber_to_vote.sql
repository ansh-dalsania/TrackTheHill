-- V4: Adds a chamber column to vote and re-keys existing IDs with a chamber
-- prefix ("H-" or "S-"). This is necessary because House and Senate roll call
-- numbers are independent numbering systems and can cause issues if they are the same.

ALTER TABLE vote ADD COLUMN chamber VARCHAR(10);
UPDATE vote SET chamber = 'House';
ALTER TABLE vote ALTER COLUMN chamber SET NOT NULL;

-- Temporarily drop the FK constraint so both tables' keys can be updated
-- without Postgres rejecting the intermediate mismatched state
ALTER TABLE member_vote DROP CONSTRAINT member_vote_vote_id_fkey;

UPDATE vote SET id = 'H-' || id;
UPDATE member_vote SET vote_id = 'H-' || vote_id;

-- Recreate the FK constraint now that both sides match again
ALTER TABLE member_vote ADD CONSTRAINT member_vote_vote_id_fkey FOREIGN KEY (vote_id) REFERENCES vote(id);