package com.trackthehill;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, String> {
    Optional<Vote> findByCongressAndSessionAndRollCallNumber(Integer congress, Integer session, Integer rollCallNumber);
}