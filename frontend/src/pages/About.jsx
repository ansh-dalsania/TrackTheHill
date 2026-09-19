function About() {
  return (
    <div>
      <h1>About Track The Hill</h1>
      <p>
        Track The Hill is a nonpartisan tool for exploring the voting records,
        legislative activity, and campaign finance of members of the U.S. Congress.
      </p>

      <h2>Data Sources</h2>
      <ul>
        <li><strong>Congress.gov API</strong> — member profiles, bills, cosponsors, official bill summaries, and House roll call votes.</li>
        <li><strong>Voteview (UCLA)</strong> — Senate roll call votes and DW-NOMINATE ideological scores.</li>
        <li><strong>unitedstates/congress-legislators</strong> — committee assignments and leadership roles.</li>
      </ul>

      <h2>Methodology Notes</h2>
      <ul>
        <li>Voting history and "issue" filters use Congress.gov's own official policy area classifications — no editorial judgment is applied to categorize bills.</li>
        <li>Partisan score reflects how often a member's vote matched the majority position of their own party on a given roll call.</li>
        <li>Bill summaries are official, nonpartisan summaries written by the Congressional Research Service (CRS), not AI-generated.</li>
        <li>Committee membership reflects current assignments only; historical committee data is not tracked.</li>
        <li>Bill and voting data is filtered to legislation that has shown genuine progress
            (passed a chamber, reported out of committee, signed into law, etc.) — bills that
            were introduced and never acted on further are intentionally excluded. This means
            "bills sponsored" counts reflect impact, not raw volume, and may be lower than a
            member's total number of introduced bills.
        </li>
        <li>
            PAC contribution lists attempt to exclude a member's own leadership PAC or
            joint fundraising committee (e.g., "Team Smith," "Friends of Smith") based
            on common naming conventions, since these represent a member's own
            fundraising vehicle rather than outside special-interest support. This
            filtering relies on name patterns and is not guaranteed to catch every case.
        </li>
      </ul>
    </div>
  )
}

export default About