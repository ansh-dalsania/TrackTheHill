package com.reptrack;

import com.reptrack.service.MemberSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Exposes Member data over HTTP.
 */
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSyncService memberSyncService;

    public MemberController(MemberRepository memberRepository, MemberSyncService memberSyncService) {
        this.memberRepository = memberRepository;
        this.memberSyncService = memberSyncService;
    }

    // Returns all members currently in the database
    @GetMapping("/api/members")
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Returns a single member by their bioguide ID
    @GetMapping("/api/members/{id}")
    public Member getMember(@PathVariable String id) {
        return memberRepository.findById(id).orElse(null);
    }

    // TEMPORARY: manually triggers a sync from Congress.gov.
    // Will be replaced by a scheduled job later.
    @GetMapping("/api/sync/members/{congress}")
    public String triggerSync(@PathVariable int congress) {
        memberSyncService.syncMembers(congress);
        return "Sync triggered for Congress " + congress;
    }
}