-- V14: Adds donor_type to top_donor, distinguishing individual donors from PAC
-- contributors so both can be tracked in the same table.

ALTER TABLE top_donor ADD COLUMN donor_type VARCHAR(20) NOT NULL DEFAULT 'Individual';