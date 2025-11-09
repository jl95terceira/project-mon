package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.variadic.Method1;
import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.*;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictList;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchOut;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineUpdatesByDecisions {

    public static class DecisionSorting {
        public record MoveInfo(MonId monId, Integer moveIndex, Integer speed, Integer priorityModifier, StrictMap<PartyId, ? extends Iterable<MonFieldPosition>> targets, Boolean interceptsSwitch) {}
        public record SwitchInfo(MonId monId, Integer monSwitchInIndex) {}
        public StrictList<SwitchInfo> switchList = strict(List());
        public StrictList<DecisionSorting.MoveInfo> moveNormalList    = strict(List());
        public StrictList<DecisionSorting.MoveInfo> moveInterceptList = strict(List());
        public StrictMap<MonId,Integer> switchMap = strict(Map());
    }

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdatesByDecisions(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void handleUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap, Method1<PmonUpdate> updateHandler) {
            // group decisions and calculate speeds + priorities
            var s = new DecisionSorting();
            StrictList<DecisionSorting.MoveInfo> moveList = strict(List());
            for (var e: decisionsMap.entrySet()) {

                var partyId = e.getKey();
                var partyDecision = e.getValue();
                for (var f: partyDecision.monDecisions.entrySet()) {

                    var monId = f.getKey();
                    var monDecision = f.getValue();
                    monDecision.get(new PmonDecision.Handler() {

                        @Override
                        public void pass(PmonDecisionToPass passDecision) {
                            // pass the turn - ignore
                        }
                        @Override
                        public void switchOut(PmonDecisionToSwitchOut switchInDecision) {

                            s.switchList.add(new DecisionSorting.SwitchInfo(new MonId(partyId, monId), switchInDecision.monSwitchInIndex));
                        }
                        @Override
                        public void useMove(PmonDecisionToUseMove useMoveDecision) {

                            var mon = context.parties.get(partyId).monsOnField.get(monId);
                            var monSpeed = mon.baseStats.speed;
                            var move = context.parties.get(partyId).monsOnField.get(monId).moves.get(useMoveDecision.moveIndex);
                            StrictList<Integer> speedModifiers = strict(List());
                            StrictList<Double> speedFactors = strict(List());
                            if (mon.status.statModifiers.containsKey(PmonStatModifierType.SPEED)) {

                                speedModifiers.add(mon.status.statModifiers.get(PmonStatModifierType.SPEED));
                            }
                            for (var statusCondition: mon.status.statusConditions.values()) {

                                if (statusCondition.statFactorsOnSelf.containsKey(PmonStatModifierType.SPEED)) {

                                    speedFactors.add(statusCondition.statFactorsOnSelf.get(PmonStatModifierType.SPEED));
                                }
                            }
                            for (var speedFactor: speedFactors) {

                                monSpeed = (int) (monSpeed * speedFactor);
                            }
                            for (var speedModifier: speedModifiers) {

                                monSpeed = (int) (monSpeed * ruleset.constants.STAT_MODIFIER_FACTOR.apply(PmonStatModifierType.SPEED, speedModifier));
                            }
                            StrictMap<PartyId,StrictList<MonFieldPosition>> targetMap = strict(Map());
                            useMoveDecision.target.get(new PmonDecisionToUseMove.Target.Handler() {
                                @Override public void mon(MonId monId) {
                                    targetMap.put(monId.partyId(), strict(List(monId.position())));
                                }
                                @Override public void party(PartyId partyId) {
                                    targetMap.put(partyId, strict(I
                                            .of(context.parties.get(partyId).monsOnField.keySet())
                                            .toList()));
                                }
                                @Override public void all() {
                                    targetMap.putAll(strict(I
                                            .of(context.parties.entrySet())
                                            .toMap(e -> e.getKey(), e -> strict(I
                                                    .of(e.getValue().monsOnField.keySet())
                                                    .toList()))));
                                }
                                @Override public void none() {
                                }
                            });
                            moveList.add(new DecisionSorting.MoveInfo(new MonId(partyId, monId), useMoveDecision.moveIndex, monSpeed, move.priorityModifier, targetMap, move.interceptsSwitch));
                        }
                    });
                }
            }
            s.switchMap = strict(I.of(s.switchList).enumer(0).toMap(t -> t.a2.monId(), t -> t.a1));
            var sortMove = method((DecisionSorting.MoveInfo move) -> {

                for (var targetMons: move.targets.entrySet()) {

                    var targetPartyId = targetMons.getKey();
                    for (var targetMonId: targetMons.getValue()) {

                        var targetMonAbsId = new MonId(targetPartyId, targetMonId);
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
                var monId = moveInfo.monId();
                updateByMove.monId = monId;
                updateByMove.moveIndex = moveInfo.moveIndex;
                var monDecision = decisionsMap.get(monId.partyId()).monDecisions.get(monId.position());
                monDecision.get(new PmonDecision.Handler() {

                    @Override
                    public void pass(PmonDecisionToPass passDecision) {throw new AssertionError();}
                    @Override
                    public void switchOut(PmonDecisionToSwitchOut switchInDecision) {throw new AssertionError();}
                    @Override
                    public void useMove(PmonDecisionToUseMove useMoveDecision) {

                        var mon = context.parties.get(monId.partyId()).monsOnField.get(monId.position());
                        var move = mon.moves.get(useMoveDecision.moveIndex);
                        var nrTargets = I.of(moveInfo.targets.values()).flatmap(x -> x).reduce(0, (a,b) -> (a+1));
                        for (var statusCondition: mon.status.statusConditions.values()) {
                            if (ruleset.rngImmobilise.roll(statusCondition.immobiliseChanceOnMove.apply())) {
                                updateByMove.usageResults.add(tuple(monId.partyId(), monId.position(),
                                        PmonUpdateByMove.UsageResult.immobilised(statusCondition.id)));
                                updateHandler.accept(PmonUpdate.by(updateByMove));
                                var onImmobilisedEffectsOnSelf = statusCondition.onImmobilisedEffectsOnSelf.apply();
                                if (onImmobilisedEffectsOnSelf != null) {
                                    // immobilised
                                    var updateByEffectsOnSelf = new PmonUpdateByOther();
                                    updateByEffectsOnSelf.origin = monId;
                                    StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
                                    updateByEffectsOnSelf.atomicUpdates.put(monId, atomicUpdates);
                                    new PmonRuleToDetermineUpdateByEffects(ruleset)
                                            .detUpdates(context, monId, monId, onImmobilisedEffectsOnSelf, 1, false, atomicUpdates::add);
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
                                // TODO: change logic below - charging moves shall not miss on their charging turn, even if there is no target
                                if (!ruleset.isAlive(targetMon)) {
                                    updateByMove.usageResults.add(tuple(targetPartyId, targetMonId,
                                            PmonUpdateByMove.UsageResult.miss(PmonUpdateByMove.UsageResult.MissType.NO_TARGET)));
                                }
                                else if (isTargetable(targetMon) && ruleset.rngAccuracy.roll(move.accuracy)) {
                                    StrictList<PmonUpdateOnTarget> atomicUpdatesOnFoe = strict(List());
                                    StrictList<PmonUpdateOnTarget> atomicUpdatesOnSelf = strict(List());
                                    var localContext = new PmonRuleToDetermineLocalContext(ruleset).detLocalContext(context, moveInfo.monId().partyId());
                                    var monId = moveInfo.monId();
                                    Integer nrHits = ruleset.rngHitNrTimes.betweenInclusive(move.hitNrTimesRange);
                                    var effectsOnTarget = move.effectsOnTarget.apply(new PmonMove.EffectsContext(localContext, monId.position(), mon, useMoveDecision.target));
                                    if (effectsOnTarget != null) {
                                        for (var i : I.range(nrHits)) {
                                            new PmonRuleToDetermineUpdateByEffects(ruleset).detUpdates(context, monId, new MonId(targetPartyId, targetMonId), effectsOnTarget, nrTargets, true, atomicUpdatesOnFoe::add);
                                        }
                                    }
                                    var effectsOnSelf = move.effectsOnSelf.apply(new PmonMove.EffectsContext(localContext, monId.position(), mon, useMoveDecision.target));
                                    if (effectsOnSelf != null) {
                                        new PmonRuleToDetermineUpdateByEffects(ruleset).detUpdates(context, monId, monId, effectsOnSelf, 1, true, atomicUpdatesOnSelf::add);
                                    }
                                    updateByMove.usageResults.add(tuple(targetPartyId, targetMonId,
                                            PmonUpdateByMove.UsageResult.hit(atomicUpdatesOnFoe)));
                                    updateByMove.usageResults.add(tuple(monId.partyId(), monId.position(),
                                            PmonUpdateByMove.UsageResult.hit(atomicUpdatesOnSelf)));
                                }
                                else {
                                    updateByMove.usageResults.add(tuple(targetPartyId, targetMonId,
                                            PmonUpdateByMove.UsageResult.miss(PmonUpdateByMove.UsageResult.MissType.INACCURATE)));
                                }
                            }
                        }
                        updateHandler.accept(PmonUpdate.by(updateByMove));
                        mon.moveLastUsed = useMoveDecision;
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
                switchInUpdate.partyId = switchInInfo.monId().partyId();
                switchInUpdate.monFieldPosition = switchInInfo.monId().position();
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
                    var position = e2.getKey();
                    var mon = e2.getValue();
                    for (var statusCondition: mon.status.statusConditions.values()) {
                        statusCondition.afterTurn.accept();
                        var afterTurnUpdate = new PmonUpdateByOther();
                        var monId = new MonId(partyId, position);
                        afterTurnUpdate.origin = monId;
                        for (var e3: statusCondition.afterTurnEffects.apply(monId,localContext).entrySet()) {
                            var target = e3.getKey();
                            var effects = e3.getValue();
                            StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
                            afterTurnUpdate.atomicUpdates.put(target, atomicUpdates);
                            effects.forEach(e -> new PmonRuleToDetermineUpdateByEffects(ruleset)
                                    .detUpdates(context, monId, target, e, 1, false, atomicUpdates::add));
                        }
                        if (!afterTurnUpdate.atomicUpdates.isEmpty()) {
                            updateHandler.accept(PmonUpdate.by(afterTurnUpdate));
                        }
                    }
                }
            }

            // after everything
            for (var condition: context.fieldConditions.values()) {
                condition.turnNr += 1;
            }
            for (var party: context.parties.values()) {
                for (var condition: party.fieldConditions.values()) {
                    condition.turnNr += 1;
                }
                for (var condition: I.of(party.fieldConditionsByMon.values()).flatmap(StrictMap::values)) {
                    condition.turnNr += 1;
                }
                for (var mon: party.mons) {
                    mon.status.damageAccumulatedForTheTurn = 0;
                    mon.status.damageByLastFoe = 0;
                }
            }
        }

    private boolean isTargetable(Pmon mon) {

        return I.of(mon.status.statusConditions.values()).all(sc -> !sc.untargetable);
    }
    private int speedDiffWithRng(Integer speedDiff) {

        return ((int)(ruleset.constants.SPEED_RNG_BREADTH * (ruleset.rngSpeed.get() - ruleset.rngSpeed.get()))) + speedDiff;
    }
}
