-- V5: Adds DW-NOMINATE ideological scores to member, sourced from Voteview.
-- nominate_dim1 is the primary liberal-conservative axis (~ -1 = most liberal,
-- ~ +1 = most conservative). nominate_dim2 is a secondary dimension capturing
-- cross-cutting issues, stored for completeness but not used for ranking display.

ALTER TABLE member ADD COLUMN nominate_dim1 DOUBLE PRECISION;
ALTER TABLE member ADD COLUMN nominate_dim2 DOUBLE PRECISION;