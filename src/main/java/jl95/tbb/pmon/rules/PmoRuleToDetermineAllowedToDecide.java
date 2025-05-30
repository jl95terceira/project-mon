package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmoRuleToDetermineAllowedToDecide {

    public final PmonRuleset ruleset;

    public PmoRuleToDetermineAllowedToDecide(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public StrictMap<PartyId, StrictSet<MonFieldPosition>> allowedToDecide(MonGlobalContext<Pmon> context) {

        // If all mons are alive, everyone gets to decide - use move, switch-in, etc.
        // If there are any fainted mons, only switch-ins are allowed and only for the fainted mons.

        StrictMap<PartyId, StrictSet<MonFieldPosition>> fainted = strict(Map());
        for (var e: context.parties.entrySet()) {

            PartyId partyId = e.getKey();
            MonParty<Pmon> party = e.getValue();
            for (var e2: party.monsOnField.entrySet()) {

                MonFieldPosition monFieldPosition = e2.getKey();
                Pmon mon = e2.getValue();
                if (!ruleset.isAlive(mon)) {

                    StrictSet<MonFieldPosition> faintedOfParty;
                    if (!fainted.containsKey(partyId)) {

                        faintedOfParty = strict(Set());
                        fainted.put(partyId, faintedOfParty);
                    }
                    else {

                        faintedOfParty = fainted.get(partyId);
                    }
                    faintedOfParty.add(monFieldPosition);
                }
            }
        }
        if (fainted.isEmpty()) {

            return strict(I.of(context.parties.entrySet()).toMap(e -> e.getKey(), e -> strict(e.getValue().monsOnField.keySet())));

            //TODO: should be based also on move lock-in, charging / recharging, etc
        }
        else {

            return fainted;
        }
    }
}
