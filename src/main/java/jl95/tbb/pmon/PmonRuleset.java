package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;

import java.util.*;

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public static class DecisionSorting {
        public record MoveInfo(PartyId partyId, MonParty.MonId monId, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets, Boolean pursuit) {}
        public record SwitchInInfo(PartyId partyId, MonParty.MonId monId) {}
        public List<SwitchInInfo> switchInList    = List();
        public List<MoveInfo>     moveNormalList  = List();
        public List<MoveInfo>     movePursuitList = List();
        public StrictMap<SwitchInInfo, Integer> switchInMap = strict(Map());
    }

    public static PartyId NO_VICTOR = new PartyId();

    public final PmonRulesetConstants constants = new PmonRulesetConstants();
    public Function0<Double> rng = new Random()::nextDouble;

    private Double rng() { return rng.apply(); }

    public Integer detDamage(Pmon mon, PmonMove move, Pmon targetMon) {

        var v = new Ref<>(0);
        move.attrs.power.call(new PmonMovePower.Callbacks() {
            @Override
            public void typed(Integer power) {
                var sourceAttack = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.attack
                        : stats.specialAttack).apply(mon.attrs.baseStats);
                var targetDefense = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.defense
                        : stats.specialDefense).apply(targetMon.attrs.baseStats);
            }
            @Override
            public void constant(Integer damage) { v.set(damage); }
            @Override
            public void byHp(Double percent) {
                v.set((int) (percent * targetMon.status.hp));
            }
            @Override
            public void byMaxHp(Double percent) { v.set((int) (percent * targetMon.attrs.baseStats.hp)); }
        });
        return 10; //TODO: actually calculate the damage; consider abilities, status conditions, etc.
    }

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
        // group decisions and calculate speeds + priorities
        var s = new DecisionSorting();
        List<DecisionSorting.MoveInfo> moveList = List();
        for (var e: decisionsMap.entrySet()) {
            var partyId = e.getKey();
            var partyDecision = e.getValue();
            for (var f: partyDecision.monDecisions.entrySet()) {
                var monId = f.getKey();
                if (!allowDecide(context, partyId, monId)) {
                    continue;
                }
                var monDecision = f.getValue();
                monDecision.call(new PmonDecision.Callbacks() {
                    @Override
                    public void pass() {
                        // pass the turn - ignore
                    }
                    @Override
                    public void switchIn(Integer monSwitchInIndex) {
                        s.switchInList.add(new DecisionSorting.SwitchInInfo(partyId, monId));
                    }
                    @Override
                    public void useMove(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets) {
                        var mon = context.parties.get(partyId).monsOnField.get(monId);
                        var monSpeed = mon.attrs.baseStats.speed;
                        var move = context.parties.get(partyId).monsOnField.get(monId).moves.get(moveIndex);
                        List<Integer> speedModifiers = List();
                        if (mon.status.statModifiers.containsKey(PmonStatModifierType.SPEED)) {
                            speedModifiers.add(mon.status.statModifiers.get(PmonStatModifierType.SPEED));
                        }
                        for (var statusProblem: mon.status.statusConditions.values()) {
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
        // sort decisions
        for (var list: I(s.moveNormalList, s.movePursuitList)) {
            list.sort((m1, m2) -> m1.priorityModifier > m2.priorityModifier? 1
                                : m1.priorityModifier < m2.priorityModifier? -1
                                : speedDiffWithRng(m1.speed - m2.speed));
        }
        // evaluate decisions into updates - where THE GOOD STUFF happens
        List<PmonUpdate> updates = List();
        // pursuit-switch-in moves
        // switch-in moves
        // normal moves
        for (var moveInfo: s.moveNormalList) {
            var updateByMove = new PmonUpdateByDamage();
            var monDecision = decisionsMap.get(moveInfo.partyId()).monDecisions.get(moveInfo.monId());
            monDecision.call(new PmonDecision.Callbacks() {
                @Override
                public void pass() {throw new AssertionError();}
                @Override
                public void switchIn(Integer monSwitchInIndex) {throw new AssertionError();}
                @Override
                public void useMove(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets) {
                    var mon = context.parties.get(moveInfo.partyId()).monsOnField.get(moveInfo.monId());
                    var move = mon.moves.get(moveIndex);
                    for (var x: moveInfo.targets().entrySet()) {
                        var targetPartyId = x.getKey();
                        for (var targetMonId: x.getValue()) {
                            var targetMon = context.parties.get(targetPartyId).monsOnField.get(targetMonId);
                            var updateOnTarget = new PmonUpdateByDamage.UpdateOnTarget();
                            updateByMove.updatesOnTargets.add(tuple(targetPartyId, targetMonId, updateOnTarget));
                            updateOnTarget.damage = detDamage(mon, move, targetMon);
                            //TODO: the rest - calculate updates of all applicable types, according to move effects
                        }
                    }
                }
            });
        }
        return updates;
    }

    private Integer speedDiffWithRng(Integer speedDiff) {
        return ((int)((2*constants.SPEED_RNG_BREADTH) * rng() - constants.SPEED_RNG_BREADTH)) + speedDiff;
    }

    @Override
    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {
        pmonUpdate.call(new PmonUpdate.Callbacks() {
            @Override
            public void move(PmonUpdateByDamage update) {
                for (var t: update.updatesOnTargets) {
                    var mon = context.parties.get(t.a1).monsOnField.get(t.a2);
                    var u = t.a3;
                    mon.status.hp = function((Integer hpRemaining) -> hpRemaining > 0? hpRemaining: 0).apply(mon.status.hp - u.damage);
                }
            }
            @Override
            public void statModify(PmonUpdateByStatModify update) {
                for (var t: update.updatesOnTargets) {
                    var mon = context.parties.get(t.a1).monsOnField.get(t.a2);
                    var u = t.a3;
                    for (var t2: I(
                            tuple(u.statRaises, function(Integer::sum)),
                            tuple(u.statFalls , function((Integer a, Integer b) -> (a - b))))) {

                        for (var e: t2.a1.entrySet()) {
                            PmonStatModifierType type = e.getKey();
                            Integer stages = e.getValue();
                            if (mon.status.statModifiers.containsKey(type)) {
                                mon.status.statModifiers.put(type, t2.a2.apply(mon.status.statModifiers.get(type), stages));
                            }
                        }
                    }
                }
            }
            @Override
            public void statusCondition(PmonUpdateByStatusCondition update) {
                for (var t: update.updatesOnTargets) {
                    var mon = context.parties.get(t.a1).monsOnField.get(t.a2);
                    var u = t.a3;
                    for (var condition: u.statusConditions) {
                        if (mon.status.statusConditions.containsKey(condition.id)) {
                            continue;
                        }
                        mon.status.statusConditions.put(condition.id, condition);
                        //TODO: there may be exclusive status conditions (where, if one already is in place, the incoming condition is discarded), etc
                    }
                }
            }
            @Override
            public void switchIn(PmonUpdateBySwitchIn update) {
                var party = context.parties.get(update.partyId);
                party.monsOnField.remove(update.monId);
                party.monsOnField.put(new MonParty.MonId(), party.mons.get(update.monToSwitchInIndex));
            }
        });
    }

    @Override
    public Iterable<PmonUpdate> detLocalUpdates(PmonUpdate pmonUpdate, PartyId partyId) {
        return List.of(pmonUpdate);
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
    public Boolean allowDecide(MonGlobalContext<Pmon> context, PartyId partyId, MonParty.MonId monId) {

        var mon = context.parties.get(partyId).monsOnField.get(monId);
        return true; //TODO: should be based on move lock-in, charging / recharging, etc
    }
}
