package com.pdv.parties;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PartyEntryRepository extends JpaRepository<PartyEntry, Long> {
    @Query("select coalesce(sum(e.delta), 0) from PartyEntry e where e.party = :p and e.partyId = :id")
    BigDecimal balance(@Param("p") PartyEntry.Party party, @Param("id") Long partyId);

    /** [partyId, saldo] de cada tercero: sirve para "saldo a cobrar" y "saldo adeudado". */
    @Query("select e.partyId, sum(e.delta) from PartyEntry e where e.party = :p group by e.partyId")
    List<Object[]> balances(@Param("p") PartyEntry.Party party);

    List<PartyEntry> findByPartyAndPartyIdOrderByOccurredAtAscIdAsc(PartyEntry.Party party, Long partyId);
}
