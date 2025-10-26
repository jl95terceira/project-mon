package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.variadic.Method1;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictList;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.PmonDecision;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchOut;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineUpdatesByDecisions {

    public static class DecisionSorting {
        public record MoveInfo(PartyId partyId, MonFieldPosition monId, Integer moveIndex, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonFieldPosition>> targets, Boolean interceptsSwitch) {}
        public record SwitchInfo(PartyId partyId, MonFieldPosition monId, Integer monSwitchInIndex) {}
        public StrictList<SwitchInfo> switchList = strict(List());
        public StrictList<DecisionSorting.MoveInfo> moveNormalList    = strict(List());
        public StrictList<DecisionSorting.MoveInfo> moveInterceptList = strict(List());
        public StrictMap<Tuple2<PartyId, MonFieldPosition>, Integer> switchMap = strict(Map());
    }

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdatesByDecisions(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void handleUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap, Method1<PmonUpdate> updateHandler) {
            // group decisions and calculate speeds + priorities
            var s = new DecisionSorting();
            StrictList<DecisionSorting.MoveInfo> moveList = strict(List());
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
                    monDecision.call(new PmonDecision.Handler() {

                        @Override
                        public void pass(PmonDecisionToPass passDecision) {
                            // pass the turn - ignore
                        }
                        @Override
                        public void switchOut(PmonDecisionToSwitchOut switchInDecision) {

                            s.switchList.add(new DecisionSorting.SwitchInfo(partyId, monId, switchInDecision.monSwitchInIndex));
                        }
                        @Override
                        public void useMove(PmonDecisionToUseMove useMoveDecision) {

                            var mon = context.parties.get(partyId).monsOnField.get(monId);
                            var monSpeed = mon.attrs.baseStats.speed;
                            var move = context.parties.get(partyId).monsOnField.get(monId).moves.get(useMoveDecision.moveIndex);
                            StrictList<Integer> speedModifiers = strict(List());
                            StrictList<Double> speedFactors = strict(List());
                            if (mon.status.statModifiers.containsKey(PmonStatModifierType.SPEED)) {

                                speedModifiers.add(mon.status.statModifiers.get(PmonStatModifierType.SPEED));
                            }
                            for (var statusCondition: mon.status.statusConditions.values()) {

                                if (statusCondition.statFactors.containsKey(PmonStatModifierType.SPEED)) {

                                    speedFactors.add(statusCondition.statFactors.get(PmonStatModifierType.SPEED));
                                }
                            }
                            for (var speedFactor: speedFactors) {

                                monSpeed = (int) (monSpeed * speedFactor);
                            }
                            for (var speedModifier: speedModifiers) {

                                monSpeed = (int) (monSpeed * ruleset.constants.STAT_MODIFIER_FACTOR.apply(PmonStatModifierType.SPEED, speedModifier));
                            }
                            moveList.add(new DecisionSorting.MoveInfo(partyId, monId, useMoveDecision.moveIndex, monSpeed, move.attrs.priorityModifier, useMoveDecision.targets, move.attrs.interceptsSwitch));
                        }
                    });
                }
            }
            s.switchMap = strict(I.of(s.switchList).enumer(0).toMap(t -> tuple(t.a2.partyId(), t.a2.monId()), t -> t.a1));
            var sortMove = method((DecisionSorting.MoveInfo move) -> {

                for (var targetMons: move.targets.entrySet()) {

                    var targetPartyId = targetMons.getKey();
                    for (var targetMonId: targetMons.getValue()) {

                        var targetMonAbsId = tuple(targetPartyId, targetMonId);
                        if (move.interceptsSwitch && s.switchMap.containsKey(targetMonAbsId)) {

                            s.moveInterceptList.add(move);
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
            for (var list: I(s.moveNormalList, s.moveInterceptList)) {

                list.sort((m1, m2) -> m1.priorityModifier > m2.priorityModifier? -1
                                    : m1.priorityModifier < m2.priorityModifier? 1
                                    : speedDiffWithRng(m2.speed - m1.speed));
            }
            // evaluate decisions into updates - where THE GOOD STUFF happens
            var moveInfoToUpdate = method((DecisionSorting.MoveInfo moveInfo) -> {

                var updateByMove = new PmonUpdateByMove();
                updateByMove.partyId = moveInfo.partyId;
                updateByMove.monId = moveInfo.monId;
                updateByMove.moveIndex = moveInfo.moveIndex;
                var monDecision = decisionsMap.get(moveInfo.partyId()).monDecisions.get(moveInfo.monId());
                monDecision.call(new PmonDecision.Handler() {

                    @Override
                    public void pass(PmonDecisionToPass passDecision) {throw new AssertionError();}
                    @Override
                    public void switchOut(PmonDecisionToSwitchOut switchInDecision) {throw new AssertionError();}
                    @Override
                    public void useMove(PmonDecisionToUseMove useMoveDecision) {

                        var origin = tuple(moveInfo.partyId, moveInfo.monId);
                        var mon = context.parties.get(moveInfo.partyId()).monsOnField.get(moveInfo.monId());
                        var move = mon.moves.get(useMoveDecision.moveIndex);
                        var nrTargets = I.of(moveInfo.targets.values()).flatmap(x -> x).reduce(0, (a,b) -> (a+1));
                        for (var statusCondition: mon.status.statusConditions.values()) {
                            if (ruleset.rngImmobilise.roll(statusCondition.immobiliseChanceOnMove.apply())) {
                                updateByMove.statuses.add(tuple(moveInfo.partyId, moveInfo.monId, PmonUpdateByMove.UsageResult.immobilised(statusCondition.id)));
                                updateHandler.accept(PmonUpdate.by(updateByMove));
                                var onImmobilisedEffectsOnSelf = statusCondition.onImmobilisedEffectsOnSelf.apply();
                                if (onImmobilisedEffectsOnSelf != null) {
                                    var updateByEffectsOnSelf = new PmonUpdateByOther();
                                    updateByEffectsOnSelf.origin = origin;
                                    StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
                                    updateByEffectsOnSelf.atomicUpdates.put(origin, atomicUpdates);
                                    new PmonRuleToDetermineUpdateByEffects(ruleset)
                                            .detUpdates(context, origin, origin, onImmobilisedEffectsOnSelf, 1, false, atomicUpdates::add);
                                    updateHandler.accept(PmonUpdate.by(updateByEffectsOnSelf));
                                }
                                return; // no other effects
                            }
                        }
                        // move (no immobilise)
                        for (var x: moveInfo.targets().entrySet()) {

                            var targetPartyId = x.getKey();
                            for (var targetMonId: x.getValue()) {

                                var targetMon = context.parties.get(targetPartyId).monsOnField.get(targetMonId);
                                PmonUpdateByMove.UsageResult usageResult;
                                if (!ruleset.isAlive(targetMon)) {

                                    usageResult = PmonUpdateByMove.UsageResult.miss(PmonUpdateByMove.UsageResult.MissType.NO_TARGET);
                                }
                                else if (ruleset.rngAccuracy.roll(move.attrs.accuracy)) {

                                    StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
                                    Integer nrHits = ruleset.rngHitNrTimes.betweenInclusive(move.attrs.hitNrTimesRange);
                                    for (var i: I.range(nrHits)) {

                                        new PmonRuleToDetermineUpdateByEffects(ruleset).detUpdates(context, origin, tuple(targetPartyId, targetMonId), move.attrs.effects, nrTargets, true, atomicUpdates::add);
                                    }
                                    usageResult = PmonUpdateByMove.UsageResult.hit(atomicUpdates);
                                }
                                else {

                                    usageResult = PmonUpdateByMove.UsageResult.miss(PmonUpdateByMove.UsageResult.MissType.INACCURATE);
                                }
                                updateByMove.statuses.add(tuple(targetPartyId, targetMonId, usageResult));
                            }
                        }
                        updateHandler.accept(PmonUpdate.by(updateByMove));
                    }
                });
            });

            // interceptsSwitch-switch-in moves
            for (var moveInfo: s.moveInterceptList) {
                moveInfoToUpdate.accept(moveInfo);
            }

            // switch-in moves
            for (var switchInInfo: s.switchList) {
                var switchInUpdate = new PmonUpdateBySwitchOut();
                switchInUpdate.partyId = switchInInfo.partyId();
                switchInUpdate.monFieldPosition = switchInInfo.monId();
                switchInUpdate.monToSwitchInPartyPosition = switchInInfo.monSwitchInIndex();
                updateHandler.accept(PmonUpdate.by(switchInUpdate));
            }

            // normal moves
            for (var moveInfo: s.moveNormalList) {
                moveInfoToUpdate.accept(moveInfo);
            }

            // after-turn effects
            for (var e1: context.parties.entrySet()) {
                var partyId = e1.getKey();
                var party = e1.getValue();
                var localContext = new PmonRuleToDetermineLocalContext(ruleset).detLocalContext(context,partyId);
                for (var e2: party.monsOnField.entrySet()) {
                    var monId = e2.getKey();
                    var mon = e2.getValue();
                    for (var statusCondition: mon.status.statusConditions.values()) {
                        statusCondition.afterTurn.accept();
                        var afterTurnUpdate = new PmonUpdateByOther();
                        afterTurnUpdate.origin = tuple(partyId, monId);
                        for (var e3: statusCondition.afterTurnEffects.apply(partyId,monId,localContext).entrySet()) {
                            var target = e3.getKey();
                            var effects = e3.getValue();
                            StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
                            afterTurnUpdate.atomicUpdates.put(target, atomicUpdates);
                            new PmonRuleToDetermineUpdateByEffects(ruleset)
                                    .detUpdates(context, tuple(partyId, monId), target, effects, 1, false, atomicUpdates::add);
                        }
                        if (!afterTurnUpdate.atomicUpdates.isEmpty()) {
                            updateHandler.accept(PmonUpdate.by(afterTurnUpdate));
                        }
                    }
                }
            }
        }

    public Integer speedDiffWithRng(Integer speedDiff) {

        return ((int)(ruleset.constants.SPEED_RNG_BREADTH * (ruleset.rngSpeed.get() - ruleset.rngSpeed.get()))) + speedDiff;
    }
}
