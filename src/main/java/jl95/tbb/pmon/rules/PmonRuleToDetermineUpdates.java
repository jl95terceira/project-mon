package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.PmonDecision;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchIn;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.tbb.pmon.update.PmonUpdateByMove;
import jl95.tbb.pmon.update.PmonUpdateBySwitchIn;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffect;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffectByDamage;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffectByStatModifier;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffectByStatusCondition;
import jl95.util.StrictMap;

import java.util.List;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineUpdates {

    public static class DecisionSorting {
        public record MoveInfo(PartyId partyId, MonFieldPosition monId, Integer moveIndex, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonFieldPosition>> targets, Boolean pursuit) {}
        public record SwitchInInfo(PartyId partyId, MonFieldPosition monId, Integer monSwitchInIndex) {}
        public List<DecisionSorting.SwitchInInfo> switchInList    = List();
        public List<DecisionSorting.MoveInfo>     moveNormalList  = List();
        public List<DecisionSorting.MoveInfo>     movePursuitList = List();
        public StrictMap<Tuple2<PartyId, MonFieldPosition>, Integer> switchInMap = strict(Map());
    }

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdates(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Iterable<PmonUpdate> detUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {
            // group decisions and calculate speeds + priorities
            var s = new DecisionSorting();
            List<DecisionSorting.MoveInfo> moveList = List();
            var allowedToDecide = ruleset.allowedToDecide(context);
            for (var e: decisionsMap.entrySet()) {

                var partyId = e.getKey();
                var partyDecision = e.getValue();
                for (var f: partyDecision.monDecisions.entrySet()) {

                    var monId = f.getKey();
                    if (!allowedToDecide.containsKey(partyId) || !allowedToDecide.get(partyId).contains(monId)) {

                        continue;
                    }
                    var monDecision = f.getValue();
                    monDecision.call(new PmonDecision.Handlers() {

                        @Override
                        public void pass(PmonDecisionToPass passDecision) {
                            // pass the turn - ignore
                        }
                        @Override
                        public void switchIn(PmonDecisionToSwitchIn switchInDecision) {

                            s.switchInList.add(new DecisionSorting.SwitchInInfo(partyId, monId, switchInDecision.monSwitchInIndex));
                        }
                        @Override
                        public void useMove(PmonDecisionToUseMove useMoveDecision) {

                            var mon = context.parties.get(partyId).monsOnField.get(monId);
                            var monSpeed = mon.attrs.baseStats.speed;
                            var move = context.parties.get(partyId).monsOnField.get(monId).moves.get(useMoveDecision.moveIndex);
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

                                monSpeed = (int) (monSpeed * ruleset.constants.STAT_MODIFIER_FACTOR.apply(PmonStatModifierType.SPEED, speedModifier));
                            }
                            moveList.add(new DecisionSorting.MoveInfo(partyId, monId, useMoveDecision.moveIndex, monSpeed, move.attrs.priorityModifier, useMoveDecision.targets, move.attrs.pursuit));
                        }
                    });
                }
            }
            s.switchInMap = strict(I.of(s.switchInList).enumer(0).toMap(t -> tuple(t.a2.partyId(), t.a2.monId()), t -> t.a1));
            var sortMove = method((DecisionSorting.MoveInfo move) -> {

                for (var targetMons: move.targets.entrySet()) {

                    var targetPartyId = targetMons.getKey();
                    for (var targetMonId: targetMons.getValue()) {

                        var targetMonAbsId = tuple(targetPartyId, targetMonId);
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

                list.sort((m1, m2) -> m1.priorityModifier > m2.priorityModifier?  1
                                    : m1.priorityModifier < m2.priorityModifier? -1
                                    : speedDiffWithRng(m1.speed - m2.speed));
            }
            // evaluate decisions into updates - where THE GOOD STUFF happens
            List<PmonUpdate> updates = List();
            var moveInfoToUpdate = method((DecisionSorting.MoveInfo moveInfo) -> {

                var updateByMove = new PmonUpdateByMove();
                updateByMove.partyId = moveInfo.partyId;
                updateByMove.monId = moveInfo.monId;
                updateByMove.moveIndex = moveInfo.moveIndex;
                var monDecision = decisionsMap.get(moveInfo.partyId()).monDecisions.get(moveInfo.monId());
                monDecision.call(new PmonDecision.Handlers() {

                    @Override
                    public void pass(PmonDecisionToPass passDecision) {throw new AssertionError();}
                    @Override
                    public void switchIn(PmonDecisionToSwitchIn switchInDecision) {throw new AssertionError();}
                    @Override
                    public void useMove(PmonDecisionToUseMove useMoveDecision) {

                        var mon = context.parties.get(moveInfo.partyId()).monsOnField.get(moveInfo.monId());
                        var move = mon.moves.get(useMoveDecision.moveIndex);
                        var nrTargets = I.of(moveInfo.targets.values()).flatmap(x -> x).reduce(0, (a,b) -> (a+1));
                        for (var x: moveInfo.targets().entrySet()) {

                            var targetPartyId = x.getKey();
                            for (var targetMonId: x.getValue()) {

                                var targetMon = context.parties.get(targetPartyId).monsOnField.get(targetMonId);
                                PmonUpdateByMove.UpdateOnTarget updateOnTarget;
                                if (!ruleset.isAlive(targetMon)) {

                                    updateOnTarget = PmonUpdateByMove.UpdateOnTarget.noTarget();
                                }
                                else if (ruleset.roll100(move.attrs.accuracy)) {

                                    List<PmonAtomicEffect> atomicEffects = List();
                                    for (var n: I.range(ruleset.rngBetweenInclusive(move.attrs.hitNrTimesRange))) {

                                        // damage
                                        var damageUpdate = new PmonAtomicEffectByDamage();
                                        var damageAndEffectiveness = ruleset.detDamage(mon, useMoveDecision.moveIndex, ruleset.constants.CRITICAL_HIT_CHANCE >= ruleset.rng(), targetMon);
                                        if (damageAndEffectiveness.a1) {
                                            damageUpdate.damage = (int) floor(move.attrs.powerReductionFactorByNrTargets.apply(nrTargets) * damageAndEffectiveness.a2);
                                            damageUpdate.effectivenessFactor = damageAndEffectiveness.a3;
                                            atomicEffects.add(PmonAtomicEffect.by(damageUpdate));
                                        }
                                        // stat modify
                                        var statUpdate = new PmonAtomicEffectByStatModifier();
                                        for (var e: move.attrs.statModifiers.entrySet()) {
                                            PmonStatModifierType type = e.getKey();
                                            Chanced<Integer> chancedStatModify = e.getValue();
                                            if (ruleset.roll100(chancedStatModify.chance)) {
                                                if (chancedStatModify.value > 0) {
                                                    statUpdate.statRaises.put(type, chancedStatModify.value);
                                                }
                                                else if (chancedStatModify.value < 0) {
                                                    statUpdate.statFalls.put(type, chancedStatModify.value);
                                                }
                                            }
                                        }
                                        if (!(statUpdate.statRaises.isEmpty() && statUpdate.statFalls.isEmpty() && statUpdate.statResets.isEmpty())) {
                                            atomicEffects.add(PmonAtomicEffect.by(statUpdate));
                                        }
                                        // status conditions
                                        var conditionUpdate = new PmonAtomicEffectByStatusCondition();
                                        for (var chancedStatusConditionSupplier: move.attrs.statusConditions) {
                                            if (ruleset.roll100(chancedStatusConditionSupplier.chance)) {
                                                conditionUpdate.statusConditionsApply.add(chancedStatusConditionSupplier.value.apply());
                                            }
                                        }
                                        if (!(conditionUpdate.statusConditionsApply.isEmpty() && conditionUpdate.statusConditionsRemove.isEmpty())) {
                                            atomicEffects.add(PmonAtomicEffect.by(conditionUpdate));
                                        }
                                    }
                                    updateOnTarget = PmonUpdateByMove.UpdateOnTarget.hit(atomicEffects);
                                }
                                else {

                                    updateOnTarget = PmonUpdateByMove.UpdateOnTarget.miss();
                                }
                                updateByMove.updatesOnTargets.add(tuple(targetPartyId, targetMonId, updateOnTarget));
                            }
                        }
                    }
                });
                updates.add(PmonUpdate.by(updateByMove));
            });

            // pursuit-switch-in moves
            for (var moveInfo: s.movePursuitList) {
                moveInfoToUpdate.accept(moveInfo);
            }

            // switch-in moves
            for (var switchInInfo: s.switchInList) {
                var switchInUpdate = new PmonUpdateBySwitchIn();
                switchInUpdate.partyId = switchInInfo.partyId();
                switchInUpdate.monFieldPosition = switchInInfo.monId();
                switchInUpdate.monToSwitchInPartyPosition = switchInInfo.monSwitchInIndex();
                updates.add(PmonUpdate.by(switchInUpdate));
            }

            // normal moves
            for (var moveInfo: s.moveNormalList) {
                moveInfoToUpdate.accept(moveInfo);
            }
            return updates;
        }

    public Integer speedDiffWithRng(Integer speedDiff) {

        return ((int)((2*ruleset.constants.SPEED_RNG_BREADTH) * ruleset.rng() - ruleset.constants.SPEED_RNG_BREADTH)) + speedDiff;
    }
}
