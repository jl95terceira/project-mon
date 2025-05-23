package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.Function0;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.util.StrictMap;

import java.util.*;

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public static class DecisionSorting {
        public record MoveInfo(PartyId partyId, MonPartyMonId monId, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonPartyMonId>> targets, Boolean pursuit) {}
        public record SwitchInInfo(PartyId partyId, MonPartyMonId monId) {}
        public List<SwitchInInfo> switchInList    = List();
        public List<MoveInfo>     moveNormalList  = List();
        public List<MoveInfo>     movePursuitList = List();
        public StrictMap<SwitchInInfo, Integer> switchInMap = strict(Map());
    }

    public static PartyId NO_VICTOR = new PartyId();

    public final PmonRulesetConstants constants = new PmonRulesetConstants();
    public Function0<Double> rng = new Random()::nextDouble;

    private Double rng() { return rng.apply(); }

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
    public Iterable<PmonUpdate> detUpdates(MonGlobalContext<Pmon> context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {
        var s = new DecisionSorting();
        List<DecisionSorting.MoveInfo> moveList = List();
        for (var e: decisionsMap.entrySet()) {
            var partyId = e.getKey();
            var partyDecision = e.getValue();
            for (var f: partyDecision.monDecisions.entrySet()) {
                var monId = f.getKey();
                if (!allowedToMove(context, partyId, monId)) {
                    continue;
                }
                var monDecision = f.getValue();
                monDecision.call(new PmonDecision.Callbacks() {
                    @Override
                    public void switchIn(Integer monSwitchInIndex) {
                        s.switchInList.add(new DecisionSorting.SwitchInInfo(partyId, monId));
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
                            monSpeed = (int) (monSpeed * constants.STAT_MODIFIER_MULTIPLIER.apply(PmonStatModifierType.SPEED, speedModifier));
                        }
                        moveList.add(new DecisionSorting.MoveInfo(partyId, monId, monSpeed, move.attrs.priorityModifier, targets, move.attrs.pursuit));
                    }
                });
            }
        }
        s.switchInMap = strict(I.of(s.switchInList).enumer(0).toMap(t -> t.a2, t -> t.a1));
        var sortMove = method((DecisionSorting.MoveInfo move) -> {
            for (var targetMons: move.targets.entrySet()) {
                var targetPartyId = targetMons.getKey();
                for (var targetMonId: targetMons.getValue()) {
                    var targetMonAbsId = new DecisionSorting.SwitchInInfo(targetPartyId, targetMonId);
                    if (move.pursuit && s.switchInMap.containsKey(targetMonAbsId)) {
                        s.movePursuitList.add(move);
                        return;
                    }
                }
            }
            s.moveNormalList.add(move);
        });
        for (var move: moveList) {
            sortMove.accept(move);
        }
        for (var list: I(s.moveNormalList, s.movePursuitList)) {
            list.sort((m1, m2) -> m1.priorityModifier > m2.priorityModifier? 1
                                : m1.priorityModifier < m2.priorityModifier? -1
                                : speedDiffWithRng(m1.speed - m2.speed));
        }

        throw new UnsupportedOperationException(); //TODO
    }

    private Integer speedDiffWithRng(Integer speedDiff) {
        return ((int)((2*constants.SPEED_RNG_BREADTH) * rng() - constants.SPEED_RNG_BREADTH)) + speedDiff;
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
    public Boolean allowedToMove(MonGlobalContext<Pmon> context, PartyId partyId, MonPartyMonId monId) {
        throw new UnsupportedOperationException(); //TODO
    }
}
