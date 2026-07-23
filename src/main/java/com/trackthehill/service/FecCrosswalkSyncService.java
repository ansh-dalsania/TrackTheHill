package com.trackthehill.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.trackthehill.Member;
import com.trackthehill.MemberRepository;
import com.trackthehill.dto.LegislatorCrosswalkDto;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Populates Member.fecCandidateId using the unitedstates/congress-legislators
 * project's bioguide-to-FEC crosswalk. A member may have multiple FEC IDs
 * (one per election they've run in) — we take the most recent one, assuming
 * the list is ordered oldest-to-newest as it typically is in this dataset.
 */
@Service
public class FecCrosswalkSyncService {

    private final MemberRepository memberRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String CROSSWALK_URL =
        "https://raw.githubusercontent.com/unitedstates/congress-legislators/main/legislators-current.yaml";

    public FecCrosswalkSyncService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void syncCrosswalk() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(CROSSWALK_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        List<LegislatorCrosswalkDto> legislators = yamlMapper.readValue(
                response.body(),
                yamlMapper.getTypeFactory().constructCollectionType(List.class, LegislatorCrosswalkDto.class)
        );

        int updatedCount = 0;
        for (LegislatorCrosswalkDto legislator : legislators) {
            if (legislator.id == null || legislator.id.bioguide == null) continue;
            if (legislator.id.fec == null || legislator.id.fec.isEmpty()) continue;

            var memberOpt = memberRepository.findById(legislator.id.bioguide);
            if (memberOpt.isEmpty()) continue;

            Member member = memberOpt.get();
            String mostRecentFecId = legislator.id.fec.get(legislator.id.fec.size() - 1);
            member.setFecCandidateId(mostRecentFecId);
            memberRepository.save(member);
            updatedCount++;
        }

        System.out.println("FEC crosswalk sync complete. Members updated: " + updatedCount);
    }
}