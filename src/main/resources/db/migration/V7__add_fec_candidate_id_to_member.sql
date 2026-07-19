-- V7: Adds fec_candidate_id to member, populated via the unitedstates/congress-legislators
-- crosswalk file. This links our member records to FEC's candidate ID system,
-- which is required for all campaign finance lookups (totals, committees, itemized donors).

ALTER TABLE member ADD COLUMN fec_candidate_id VARCHAR(20);