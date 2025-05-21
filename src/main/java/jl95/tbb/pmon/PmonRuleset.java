package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import java.util.*;

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public static PartyId NO_VICTOR = new PartyId();

    @Override
    public MonGlobalContext<Pmon> init(StrictMap<PartyId, MonPartyEntry<Pmon>> parties, PmonInitialConditions pmonInitialConditions) {
        var context = new MonGlobalContext<Pmon>();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var partyEntry = e.getValue();
            context.parties.put(partyId, MonParty.fromEntry(partyEntry));
        }
        return context;
    }

    @Override
    public Iterable<PmonUpdate> detInitialUpdates(MonGlobalContext<Pmon> context, PmonInitialConditions pmonInitialConditions) {
        return I();
    }

    @Override
    public MonLocalContext<Pmon, PmonFoeView> detLocalContext(MonGlobalContext<Pmon> context, PartyId ownPartyId) {
        var foePartiesView = strict(I
            .of(context.parties.keySet())
            .filter(id -> !id.equals(ownPartyId))
            .toMap(id -> id, id -> strict(I
                .of(context.parties.get(id).monsOnField.entrySet())
                .toMap(Map.Entry::getKey, e -> {
                    var foeMon = e.getValue();
                    var foeMonView = new PmonFoeView(foeMon.id);
                    foeMonView.types = foeMon.attrs.types;
                    foeMonView.status = foeMon.status;
                    return foeMonView;
                }))));
        return new MonLocalContext<>(context.parties.get(ownPartyId), foePartiesView);
    }

    @Override
    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public PmonUpdate detLocalUpdate(PmonUpdate pmonUpdate, PartyId partyId) {
        return pmonUpdate;
    }

    @Override
    public Optional<PartyId> detVictory(MonGlobalContext<Pmon> context) {
        Set<PartyId> partiesRemaining = Set();
        for (var e: context.parties.entrySet()) {
            var partyId = e.getKey();
            var party = e.getValue();
            for (var mon: party.mons) {
                if (mon.status.hp > 0) {
                    partiesRemaining.add(partyId);
                    break;
                }
            }
        }
        if (partiesRemaining.size() == 1) {
            return Optional.of(partiesRemaining.iterator().next());
        }
        else if (partiesRemaining.isEmpty()) {
            return Optional.of(NO_VICTOR);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Tuple2<PartyId, MonPartyMonId>> prioritised(MonGlobalContext<Pmon> context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {
        List<Tuple5<PartyId, MonPartyMonId, PmonDecisionPriorityType, Integer, Boolean>> priorityListBeforePursuit = List();
        for (var e: decisionsMap.entrySet()) {
            var partyId = e.getKey();
            var partyDecision = e.getValue();
            for (var f: partyDecision.monDecisions.entrySet()) {
                var monId = f.getKey();
                var monDecision = f.getValue();
                var addToPriorityList = method((PmonDecisionPriorityType pt, Integer speed, Boolean pursuit) -> { priorityListBeforePursuit.add(tuple(partyId, monId, pt, speed, pursuit)); });
                monDecision.call(new PmonDecisionCallbacks() {
                    @Override
                    public void switchIn(Integer monSwitchInIndex) {
                        addToPriorityList.accept(PmonDecisionPriorityType.SWITCH_IN, 0, false);
                    }
                    @Override
                    public void useMove(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonPartyMonId>> targets) {
                        var mon = context.parties.get(partyId).monsOnField.get(monId);
                        var monSpeed = mon.attrs.baseStats.speed;
                        var move = context.parties.get(partyId).monsOnField.get(monId).moves.get(moveIndex);
                        List<Integer> speedModifiers = List();
                        if (mon.status.statModifiers.containsKey(PmonStatModifierType.SPEED)) {
                            speedModifiers.add(mon.status.statModifiers.get(PmonStatModifierType.SPEED));
                        }
                        for (var statusProblem: mon.status.statusProblems.values()) {
                            if (statusProblem.statModifiers.containsKey(PmonStatModifierType.SPEED)) {
                                speedModifiers.add(statusProblem.statModifiers.get(PmonStatModifierType.SPEED));
                            }
                        }
                        for (var speedModifier: speedModifiers) {
                            monSpeed = (int) (monSpeed * PmonRulesetConstants.STAT_MODIFIER_MULTIPLIER.apply(PmonStatModifierType.SPEED, speedModifier));
                        }
                        addToPriorityList.accept(PmonDecisionPriorityType.MOVE, monSpeed, move.attrs.pursuit);
                    }
                });
            }
        }
        priorityListBeforePursuit.sort((o1, o2) -> (o1.a3 == PmonDecisionPriorityType.SWITCH_IN)? (!o2.a5? 1: -1)
                                                 : (o1.a3 != o2.a3)
                                                 ? (o1.a3.value - o2.a3.value)
                                                 : (o1.a4 - o2.a4)
        );
        return I.of(priorityListBeforePursuit).map(t -> tuple(t.a1, t.a2));
    }

    @Override
    public Iterable<PmonUpdate> detUpdatesPerMon(MonGlobalContext<Pmon> context, PartyId partyId, MonPartyMonId monId, PmonDecision pmonDecision) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Boolean allowedToMove(MonGlobalContext<Pmon> context, PartyId partyId, MonPartyMonId monId) {
        throw new UnsupportedOperationException(); //TODO
    }
}