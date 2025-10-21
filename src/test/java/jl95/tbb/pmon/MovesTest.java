package jl95.tbb.pmon;

import jl95.lang.P;
import jl95.lang.StrictList;
import jl95.lang.variadic.Function2;
import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.attrs.PmonAttributes;
import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.update.*;

import static jl95.lang.SuperPowers.*;

public class MovesTest {

    public static final Pmon.Id PMON_ID = new Pmon.Id();
    public static final PmonType PMON_TYPE = new PmonType(new PmonType.Id()) {
        @Override
        public PmonMoveEffectivenessType effectivenessAgainst(PmonType other) {
            return PmonMoveEffectivenessType.NORMAL;
        }
    };
    public static final int HP_MAX = 95;
    public static final int HP0    = 50;
    public static final PartyId PARTY_1_ID = new PartyId();
    public static final PartyId PARTY_2_ID = new PartyId();

    public record Context(PmonGlobalContext gc, Pmon pmon1, Pmon pmon2) {}

    private static void set(PmonAttributes pmonAttributes) {
        pmonAttributes.baseStats.hp             = HP_MAX;
        pmonAttributes.baseStats.attack         = 55;
        pmonAttributes.baseStats.defense        = 40;
        pmonAttributes.baseStats.specialAttack  = 50;
        pmonAttributes.baseStats.specialDefense = 50;
        pmonAttributes.baseStats.speed          = 90;
    }
    private static PmonPartyEntry makePartyEntry(StrictList<PmonMove> moves) {
        var party = new PmonPartyEntry();
        var pmon = new Pmon(PMON_ID);
        set(pmon.attrs);
        pmon.status.hp = HP0;
        pmon.moves.addAll(moves);
        party.mons.add(pmon);
        return party;
    }

    public static class Runner {

        public static Runner defaults() {
            return new Runner(rulesDefaults());
        }
        public static PmonRuleset rulesDefaults() {
            var rules  = new PmonRuleset();
            rules.rngSpeed           = new PmonRuleset.Rng(constant(0.0));
            rules.rngAccuracy        = new PmonRuleset.Rng(constant(0.0)); // never miss
            rules.rngCritical        = new PmonRuleset.Rng(constant(1.0)); // no critical strikes
            rules.rngHitNrTimes      = new PmonRuleset.Rng(constant(0.0)); // least hits
            rules.rngStatModify      = new PmonRuleset.Rng(constant(0.0)); // always stat modify
            rules.rngStatusCondition = new PmonRuleset.Rng(constant(0.0)); // always apply status condition
            return rules;
        }

        private final PmonRuleset _rules;

        public Runner(PmonRuleset rules) {
            this._rules = rules;
        }

        public void run1v1(
                Tuple2<StrictList<PmonMove>,StrictList<PmonMove>> moves,
                StrictList<Tuple2<
                        Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
                        Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> decisions,
                PmonBattle.Listeners listeners,
                Method1<Context> after) {

            var battle = new PmonBattle(_rules);
            var party1 = makePartyEntry(moves.a1);
            var party2 = makePartyEntry(moves.a2);
            var decisionsIterator = decisions.iterator();
            P<PmonGlobalContext> gcRef = new P<>(null);
            var parentListeners = new PmonBattle.Listeners.Extendable();
            parentListeners.add(listeners);
            parentListeners.onGlobalContext.add(gcRef::set);
            try {
                battle.spawn(
                        strict(Map(tuple(PARTY_1_ID, party1),
                                tuple(PARTY_2_ID, party2))),
                        new PmonInitialConditions(),
                        parties -> {
                            var decisionsForThisTurn = decisionsIterator.next();
                            var mon1FieldPosition = parties.get(PARTY_1_ID).iterator().next();
                            var mon2FieldPosition = parties.get(PARTY_2_ID).iterator().next();
                            return strict(Map(
                                    tuple(PARTY_1_ID, strict(Map(tuple(mon1FieldPosition, decisionsForThisTurn.a1.apply(mon1FieldPosition, mon2FieldPosition))))),
                                    tuple(PARTY_2_ID, strict(Map(tuple(mon2FieldPosition, decisionsForThisTurn.a2.apply(mon2FieldPosition, mon1FieldPosition)))))
                            ));
                        },
                        listeners,
                        not(decisionsIterator::hasNext)
                );
            }
            catch (Battle.InterruptedException ex) {/* continue */}
            after.accept(new Context(gcRef.get(), party1.mons.get(0), party2.mons.get(0)));
        }
    }

    public static void run1v1(
            Tuple2<StrictList<PmonMove>,StrictList<PmonMove>> moves,
            StrictList<Tuple2<
                    Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
                    Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> decisions,
            PmonBattle.Listeners listeners,
            Method1<Context> after) {

        Runner.defaults()
                .run1v1(moves, decisions, listeners, after);
    }
    public static Tuple2<StrictList<PmonMove>,StrictList<PmonMove>> onlyPmon1HasMove(PmonMoveAttributes moveAttrs) {
        var move = new PmonMove(new PmonMove.Id(), new PmonType(PMON_TYPE.id) {

            @Override public PmonMoveEffectivenessType effectivenessAgainst(PmonType other) {
                return PmonMoveEffectivenessType.NORMAL;
            }
        });
        move.status.pp = 99;
        move.attrs = moveAttrs;
        return tuple(strict(List(move)), strict(List()));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMove(Function2<Tuple2<PartyId,MonFieldPosition>,MonFieldPosition,MonFieldPosition> against) {
        return strict(List(tuple(
                (mon1FieldPosition,mon2FieldPosition) -> {
                    var useMove = new PmonDecisionToUseMove();
                    useMove.moveIndex = 0;
                    var a = against.apply(mon1FieldPosition,mon2FieldPosition);
                    useMove.targets = strict(Map(tuple(a.a1, I(a.a2))));
                    return PmonDecision.from(useMove);
                },
                (mon2FieldPosition,mon1FieldPosition) -> PmonDecision.from(new PmonDecisionToPass()))));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMoveAgainstPmon2() {
        return onlyPmon1UsesMove((mon1FieldPosition,mon2FieldPosition) -> tuple(PARTY_2_ID, mon2FieldPosition));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMoveOnItself() {
        return onlyPmon1UsesMove((mon1FieldPosition,mon2FieldPosition) -> tuple(PARTY_1_ID, mon1FieldPosition));
    }
    public static PmonBattle.Listeners ignoreUpdates() {
        return new PmonBattle.Listeners.Editable();
    }
    public static PmonBattle.Listeners checkHitsOnPmon2(Method0 onHit) {
        var listeners = new PmonBattle.Listeners.Extendable();
        listeners.onLocalUpdate.add((partyId, pmonUpdate) -> {
            if (partyId != PARTY_1_ID) return;
            pmonUpdate.call(new PmonUpdate.Handlers() {
                @Override public void pass(PmonUpdateByPass update) {}
                @Override public void switchOut(PmonUpdateBySwitchOut update) {}
                @Override public void move(PmonUpdateByMove update) {
                    for (var updateOnTarget: update.updatesOnTargets) {
                        if (updateOnTarget.a1 != PARTY_2_ID) return;
                        updateOnTarget.a3.call(new PmonUpdateByMove.UpdateOnTarget.Handlers() {
                            @Override public void miss() {}
                            @Override public void hit(Iterable<PmonUpdateOnTarget> atomicUpdates) {
                                for (var atomicUpdate: atomicUpdates) {
                                    atomicUpdate.call(new PmonUpdateOnTarget.Handlers() {
                                        @Override public void damage(PmonUpdateOnTargetByDamage update) {
                                            onHit.accept();
                                        }
                                        @Override public void statModify(PmonUpdateOnTargetByStatModifier update) {}
                                        @Override public void statusCondition(PmonUpdateOnTargetByStatusCondition update) {}
                                        @Override public void switchOut(PmonUpdateOnTargetBySwitchOut update) {}
                                    });
                                }
                            }
                            @Override public void noTarget() {}
                        });
                    }
                }
            });
        });
        return listeners;
    }
    public static Method1<Context> ignoreAfter() {
        return c -> {};
    }
}
