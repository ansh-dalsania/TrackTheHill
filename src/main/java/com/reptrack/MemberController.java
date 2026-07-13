package com.reptrack;

import org.springframework.beans.factory.annotation.Autowired;
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

    MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Returns all members currently in database
    @GetMapping("/api/members")
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Returns a single member by bioguide ID
    @GetMapping("/api/members/{id}")
    public Member getMember(@PathVariable String id) {
        return memberRepository.findById(id).orElse(null);
    }
}