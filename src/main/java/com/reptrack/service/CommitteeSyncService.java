package com.reptrack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reptrack.*;
import com.reptrack.dto.CommitteeDto;
import com.reptrack.dto.CommitteeMemberDto;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Syncs committee and committee_membership data from
 * unitedstates/congress-legislators.
 * Re-running this for a new Congress requires no special logic, just calling it
 * again.
 */
@Service
public class CommitteeSyncService {

    private final CommitteeRepository committeeRepository;
    private final CommitteeMembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String COMMITTEES_URL = "https://raw.githubusercontent.com/unitedstates/congress-legislators/main/committees-current.yaml";
    private static final String MEMBERSHIP_URL = "https://raw.githubusercontent.com/unitedstates/congress-legislators/main/committee-membership-current.yaml";

    public CommitteeSyncService(CommitteeRepository committeeRepository,
            CommitteeMembershipRepository membershipRepository,
            MemberRepository memberRepository) {
        this.committeeRepository = committeeRepository;
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
    }

    public void syncCommittees() throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        // Step 1: sync committees and subcommittees
        HttpRequest committeesRequest = HttpRequest.newBuilder(URI.create(COMMITTEES_URL)).GET().build();
        HttpResponse<String> committeesResponse = httpClient.send(committeesRequest,
                HttpResponse.BodyHandlers.ofString());

        List<CommitteeDto> committees = yamlMapper.readValue(
                committeesResponse.body(),
                yamlMapper.getTypeFactory().constructCollectionType(List.class, CommitteeDto.class));

        int committeeCount = 0;
        for (CommitteeDto dto : committees) {
            String chamber = capitalize(dto.type);
            String parentId = (dto.type.equals("house") ? "HS" : dto.type.equals("senate") ? "SS" : "JS")
                    + dto.thomas_id;
            // Note: thomas_id already includes the 2-letter prefix in this file for
            // top-level committees (e.g. "HSAG")
            parentId = dto.thomas_id;

            Committee parent = committeeRepository.findById(parentId).orElse(new Committee());
            parent.setId(parentId);
            parent.setName(dto.name);
            parent.setChamber(chamber);
            parent.setParentCommittee(null);
            parent.setCreatedAt(parent.getCreatedAt() != null ? parent.getCreatedAt() : LocalDateTime.now());
            parent.setUpdatedAt(LocalDateTime.now());
            committeeRepository.save(parent);
            committeeCount++;

            if (dto.subcommittees != null) {
                for (CommitteeDto.SubcommitteeDto sub : dto.subcommittees) {
                    String subId = dto.thomas_id + sub.thomas_id;
                    Committee subcommittee = committeeRepository.findById(subId).orElse(new Committee());
                    subcommittee.setId(subId);
                    subcommittee.setName(sub.name);
                    subcommittee.setChamber(chamber);
                    subcommittee.setParentCommittee(parent);
                    subcommittee.setCreatedAt(
                            subcommittee.getCreatedAt() != null ? subcommittee.getCreatedAt() : LocalDateTime.now());
                    subcommittee.setUpdatedAt(LocalDateTime.now());
                    committeeRepository.save(subcommittee);
                    committeeCount++;
                }
            }
        }
        System.out.println("Committee sync: " + committeeCount + " committees/subcommittees saved.");

        // Step 2: clear and replace membership entirely (current only)
        membershipRepository.deleteAll();

        HttpRequest membershipRequest = HttpRequest.newBuilder(URI.create(MEMBERSHIP_URL)).GET().build();
        HttpResponse<String> membershipResponse = httpClient.send(membershipRequest,
                HttpResponse.BodyHandlers.ofString());

        var keyType = yamlMapper.getTypeFactory().constructType(String.class);
        var valueType = yamlMapper.getTypeFactory().constructCollectionType(List.class, CommitteeMemberDto.class);
        var mapType = yamlMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);

        Map<String, List<CommitteeMemberDto>> membershipMap = yamlMapper.readValue(
                membershipResponse.body(),
                mapType);

        int membershipCount = 0;
        for (Map.Entry<String, List<CommitteeMemberDto>> entry : membershipMap.entrySet()) {
            String committeeId = entry.getKey();
            var committeeOpt = committeeRepository.findById(committeeId);
            if (committeeOpt.isEmpty())
                continue; // committee code not in our committee table

            for (CommitteeMemberDto memberDto : entry.getValue()) {
                var memberOpt = memberRepository.findById(memberDto.bioguide);
                if (memberOpt.isEmpty())
                    continue; // member not in our roster

                CommitteeMembership membership = new CommitteeMembership();
                membership.setMember(memberOpt.get());
                membership.setCommittee(committeeOpt.get());
                membership.setTitle(memberDto.title);
                membership.setRank(memberDto.rank);
                membership.setCreatedAt(LocalDateTime.now());
                membershipRepository.save(membership);
                membershipCount++;
            }
        }
        System.out.println("Committee membership sync complete. Memberships saved: " + membershipCount);
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}