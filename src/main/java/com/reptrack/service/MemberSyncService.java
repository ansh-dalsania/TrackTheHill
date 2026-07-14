package com.reptrack.service;

import com.reptrack.Member;
import com.reptrack.MemberRepository;
import com.reptrack.config.WebClientConfig;
import com.reptrack.dto.CongressMemberDto;
import com.reptrack.dto.CongressMemberListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Pulls member data from the Congress.gov API and syncs it into our database.
 * Handles translating raw formats (full state names, full party
 * names, "Last, First" name format) into new format.
 */
@Service
public class MemberSyncService {

    private final WebClient congressApiClient;
    private final WebClientConfig webClientConfig;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberSyncService(WebClient congressApiClient,
            WebClientConfig webClientConfig,
            MemberRepository memberRepository) {
        this.congressApiClient = congressApiClient;
        this.webClientConfig = webClientConfig;
        this.memberRepository = memberRepository;
    }

    // Maps full state names to two letter abbreviations
    private static final Map<String, String> STATE_CODES = new HashMap<>();
    static {
        STATE_CODES.put("Alabama", "AL");
        STATE_CODES.put("Alaska", "AK");
        STATE_CODES.put("Arizona", "AZ");
        STATE_CODES.put("Arkansas", "AR");
        STATE_CODES.put("California", "CA");
        STATE_CODES.put("Colorado", "CO");
        STATE_CODES.put("Connecticut", "CT");
        STATE_CODES.put("Delaware", "DE");
        STATE_CODES.put("Florida", "FL");
        STATE_CODES.put("Georgia", "GA");
        STATE_CODES.put("Hawaii", "HI");
        STATE_CODES.put("Idaho", "ID");
        STATE_CODES.put("Illinois", "IL");
        STATE_CODES.put("Indiana", "IN");
        STATE_CODES.put("Iowa", "IA");
        STATE_CODES.put("Kansas", "KS");
        STATE_CODES.put("Kentucky", "KY");
        STATE_CODES.put("Louisiana", "LA");
        STATE_CODES.put("Maine", "ME");
        STATE_CODES.put("Maryland", "MD");
        STATE_CODES.put("Massachusetts", "MA");
        STATE_CODES.put("Michigan", "MI");
        STATE_CODES.put("Minnesota", "MN");
        STATE_CODES.put("Mississippi", "MS");
        STATE_CODES.put("Missouri", "MO");
        STATE_CODES.put("Montana", "MT");
        STATE_CODES.put("Nebraska", "NE");
        STATE_CODES.put("Nevada", "NV");
        STATE_CODES.put("New Hampshire", "NH");
        STATE_CODES.put("New Jersey", "NJ");
        STATE_CODES.put("New Mexico", "NM");
        STATE_CODES.put("New York", "NY");
        STATE_CODES.put("North Carolina", "NC");
        STATE_CODES.put("North Dakota", "ND");
        STATE_CODES.put("Ohio", "OH");
        STATE_CODES.put("Oklahoma", "OK");
        STATE_CODES.put("Oregon", "OR");
        STATE_CODES.put("Pennsylvania", "PA");
        STATE_CODES.put("Rhode Island", "RI");
        STATE_CODES.put("South Carolina", "SC");
        STATE_CODES.put("South Dakota", "SD");
        STATE_CODES.put("Tennessee", "TN");
        STATE_CODES.put("Texas", "TX");
        STATE_CODES.put("Utah", "UT");
        STATE_CODES.put("Vermont", "VT");
        STATE_CODES.put("Virginia", "VA");
        STATE_CODES.put("Washington", "WA");
        STATE_CODES.put("West Virginia", "WV");
        STATE_CODES.put("Wisconsin", "WI");
        STATE_CODES.put("Wyoming", "WY");
        STATE_CODES.put("District of Columbia", "DC");
        STATE_CODES.put("Puerto Rico", "PR");
        STATE_CODES.put("Guam", "GU");
        STATE_CODES.put("American Samoa", "AS");
        STATE_CODES.put("Virgin Islands", "VI");
        STATE_CODES.put("Northern Mariana Islands", "MP");
    }

    // Maps full party names to single letter codes
    private static final Map<String, String> PARTY_CODES = new HashMap<>();
    static {
        PARTY_CODES.put("Democratic", "D");
        PARTY_CODES.put("Republican", "R");
        PARTY_CODES.put("Independent", "I");
        PARTY_CODES.put("Independent Democrat", "ID");
        PARTY_CODES.put("Libertarian", "L");
    }

    /**
     * Fetches current members of a given Congress and upserts them
     * into the member table.
     */
    public void syncMembers(int congressNumber) {
        int limit = 250;
        int offset = 0;
        int totalCount = Integer.MAX_VALUE; // unknown until first response

        while (offset < totalCount) {
            final int currentOffset = offset;

            CongressMemberListResponse response = congressApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/member/congress/{congress}")
                            .queryParam("currentMember", "true")
                            .queryParam("limit", limit)
                            .queryParam("offset", currentOffset)
                            .queryParam("api_key", webClientConfig.getApiKey())
                            .build(congressNumber))
                    .retrieve()
                    .bodyToMono(CongressMemberListResponse.class)
                    .block();

            if (response == null || response.members == null || response.members.isEmpty()) {
                break;
            }

            for (CongressMemberDto dto : response.members) {
                Member member = mapToMember(dto);
                memberRepository.save(member);
            }

            if (response.pagination != null && response.pagination.count != null) {
                totalCount = response.pagination.count;
            } else {
                break; // no pagination info, stop after this page
            }

            offset += limit;
        }
    }

    private Member mapToMember(CongressMemberDto dto) {
        // Split "Last, First" name format
        String[] nameParts = dto.name.split(",", 2);
        String lastName = nameParts[0].trim();
        String firstName = nameParts.length > 1 ? nameParts[1].trim().split("\\s+")[0] : "";

        // Grab most recent term for chamber and start year
        String chamber = null;
        LocalDate termStartDate = null;
        if (dto.terms != null && dto.terms.item != null && !dto.terms.item.isEmpty()) {
            CongressMemberDto.TermItem latestTerm = dto.terms.item.get(dto.terms.item.size() - 1);
            chamber = latestTerm.chamber.contains("Senate") ? "Senate" : "House";
            if (latestTerm.startYear != null) {
                termStartDate = LocalDate.of(latestTerm.startYear, 1, 1);
            }
        }

        Member member = memberRepository.findById(dto.bioguideId).orElse(new Member());
        member.setBioguideId(dto.bioguideId);
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setParty(PARTY_CODES.getOrDefault(dto.partyName, dto.partyName));
        member.setState(STATE_CODES.getOrDefault(dto.state, dto.state));
        member.setDistrict(dto.district != null ? String.format("%02d", dto.district) : null);
        member.setChamber(chamber);
        member.setHeadshotUrl(dto.depiction != null ? dto.depiction.imageUrl : null);
        member.setTermStartDate(termStartDate);
        member.setInOffice(true);
        member.setCreatedAt(member.getCreatedAt() != null ? member.getCreatedAt() : LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        return member;
    }
}