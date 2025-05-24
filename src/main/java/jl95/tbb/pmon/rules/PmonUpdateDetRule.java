package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonDecision;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.tbb.pmon.update.PmonUpdateByDamage;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateDetRule {

    public static class DecisionSorting {
        public record MoveInfo(PartyId partyId, MonParty.MonId monId, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets, Boolean pursuit) {}
        public record SwitchInInfo(PartyId partyId, MonParty.MonId monId) {}
        public List<DecisionSorting.SwitchInInfo> switchInList    = List();
        public List<DecisionSorting.MoveInfo>     moveNormalList  = List();
        public List<DecisionSorting.MoveInfo>     movePursuitList = List();
        public StrictMap<DecisionSorting.SwitchInInfo, Integer> switchInMap = strict(Map());
    }

    public final PmonRuleset ruleset;

    public PmonUpdateDetRule(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Iterable<PmonUpdate> detUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {
            // group decisions and calculate speeds + priorities
            var s = new DecisionSorting();
            List<DecisionSorting.MoveInfo> moveList = List();
            for (var e: decisionsMap.entrySet()) {
                var partyId = e.getKey();
                var partyDecision = e.getValue();
                for (var f: partyDecision.monDecisions.entrySet()) {
                    var monId = f.getKey();
                    if (!ruleset.allowDecide(context, partyId, monId)) {
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
                                monSpeed = (int) (monSpeed * ruleset.constants.STAT_MODIFIER_MULTIPLIER.apply(PmonStatModifierType.SPEED, speedModifier));
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
                                updateOnTarget.damage = ruleset.detDamage(mon, move, targetMon);
                                //TODO: the rest - calculate updates of all applicable types, according to move effects
                            }
                        }
                    }
                });
            }
            return updates;
        }

    public Integer speedDiffWithRng(Integer speedDiff) {
        return ((int)((2*ruleset.constants.SPEED_RNG_BREADTH) * ruleset.rng() - ruleset.constants.SPEED_RNG_BREADTH)) + speedDiff;
    }
}
