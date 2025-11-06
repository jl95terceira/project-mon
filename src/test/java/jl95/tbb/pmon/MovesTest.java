package jl95.tbb.pmon;

import jl95.lang.P;
import jl95.lang.variadic.*;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.tbb.pmon.update.*;

import static jl95.lang.SuperPowers.*;

public class MovesTest {

    public static final Pmon.Id PMON_ID = new Pmon.Id();
    public static final PmonType PMON_TYPE = new PmonType(new PmonType.Id()) {
        @Override
        public PmonMove.EffectivenessType effectivenessAgainst(PmonType other) {
            return PmonMove.EffectivenessType.NORMAL;
        }
    };
    public static final int HP_MAX = 95;
    public static final int HP0    = 50;
    public static class TestPartyId extends PartyId {
        public final String name;
        public TestPartyId(String name) {this.name = name;}
    }
    public static final PartyId PARTY_1_ID = new TestPartyId("party 1");
    public static final PartyId PARTY_2_ID = new TestPartyId("party 2");

    public record Context(PmonGlobalContext gc, Pmon pmon1, Pmon pmon2) {}

    private static void setBaseStats(Pmon pmon) {
        pmon.baseStats.hp             = HP_MAX;
        pmon.baseStats.attack         = 55;
        pmon.baseStats.defense        = 40;
        pmon.baseStats.specialAttack  = 50;
        pmon.baseStats.specialDefense = 50;
        pmon.baseStats.speed          = 90;
    }
    private static PmonPartyEntry makePartyEntry(Iterable<PmonMove> moves) {
        var party = new PmonPartyEntry();
        var pmon = new Pmon(PMON_ID);
        setBaseStats(pmon);
        pmon.status.hp = HP0;
        pmon.moves.addAll(moves);
        party.mons.add(pmon);
        return party;
    }
    public static PmonMove makeMove(Method1<PmonMove> attrs) {
        var move = new PmonMove(new PmonMove.Id(), new PmonType(PMON_TYPE.id) {

            @Override public PmonMove.EffectivenessType effectivenessAgainst(PmonType other) {
                return PmonMove.EffectivenessType.NORMAL;
            }
        });
        move.status.pp = 99;
        attrs.accept(move);
        return move;
    }

    public static class Runner {

        public static PmonRuleset rulesDefaults() {
            var rules  = new PmonRuleset();
            var RNG_0 = new PmonRuleset.Rng(constant(0.0));
            var RNG_1 = new PmonRuleset.Rng(constant(1.0));
            rules.rngSpeed           = RNG_1; // no speed variability
            rules.rngCritical        = RNG_1; // no critical strikes
            rules.rngHitNrTimes      = RNG_0; // least hits
            rules.rngStatModify      = RNG_0; // always stat modify
            rules.rngStatusCondition = RNG_0; // always apply status condition
            return rules;
        }

        private final PmonRuleset _rules;

        public Runner(PmonRuleset rules) {
            this._rules = rules;
        }
        public Runner() {this(rulesDefaults());}

        public void run1v1(
                Tuple2<Iterable<PmonMove>,Iterable<PmonMove>> moves,
                Iterable<Tuple2<
                        Function2<PmonDecision, Tuple2<PartyId, MonFieldPosition>, Tuple2<PartyId, MonFieldPosition>>,
                        Function2<PmonDecision, Tuple2<PartyId, MonFieldPosition>, Tuple2<PartyId, MonFieldPosition>>>> decisions,
                PmonBattle.Handler handler,
                Method1<Context> after) {

            var battle = new PmonBattle(_rules);
            var party1 = makePartyEntry(moves.a1);
            var party2 = makePartyEntry(moves.a2);
            party1.mons.iterator().next().baseStats.speed += 1; // so that pmon 1 always moves first, to ensure reproducibility when testing
            var decisionsIterator = decisions.iterator();
            P<PmonGlobalContext> gcRef = new P<>(null);
            var parentHandler = new PmonBattle.Handler.Extendable();
            parentHandler.add(handler);
            parentHandler.onGlobalContext.add(gcRef::set);
            try {
                battle.spawn(
                        strict(Map(tuple(PARTY_1_ID, party1),
                                tuple(PARTY_2_ID, party2))),
                        new PmonInitialConditions(),
                        partiesAllowedToDecide -> {
                            var decisionsForThisTurn = decisionsIterator.next();
                            var mon1FieldPosition = gcRef.get().parties.get(PARTY_1_ID).monsOnField.keySet().iterator().next();
                            var mon2FieldPosition = gcRef.get().parties.get(PARTY_2_ID).monsOnField.keySet().iterator().next();
                            return strict(Map(
                                    tuple(PARTY_1_ID, strict(Map(tuple(mon1FieldPosition, decisionsForThisTurn.a1.apply(tuple(PARTY_1_ID, mon1FieldPosition), tuple(PARTY_2_ID, mon2FieldPosition)))))),
                                    tuple(PARTY_2_ID, strict(Map(tuple(mon2FieldPosition, decisionsForThisTurn.a2.apply(tuple(PARTY_2_ID, mon2FieldPosition), tuple(PARTY_1_ID, mon1FieldPosition))))))
                            ));
                        },
                        parentHandler,
                        not(decisionsIterator::hasNext)
                );
            }
            catch (Battle.InterruptedException ex) {/* continue */}
            after.accept(new Context(gcRef.get(), party1.mons.get(0), party2.mons.get(0)));
        }
    }

    // moves
    public static Tuple2<Iterable<PmonMove>,Iterable<PmonMove>> pmon1HasMove(PmonMove move) {
        return tuple(
                I(move),
                I());
    }
    public static Tuple2<Iterable<PmonMove>,Iterable<PmonMove>> pmon1And2HaveMoves(PmonMove move1, PmonMove move2) {
        return tuple(
                I(move1),
                I(move2));
    }
    // decisions
    public enum TARGET {
        SELF,FOE;
    }
    public static Function2<PmonDecision, Tuple2<PartyId, MonFieldPosition>, Tuple2<PartyId, MonFieldPosition>> useMove(TARGET t) {
        return function((Tuple2<PartyId, MonFieldPosition> selfPosition,
                         Tuple2<PartyId, MonFieldPosition> foePosition) -> {
            var useMove = new PmonDecisionToUseMove();
            useMove.moveIndex = 0;
            useMove.target = t == TARGET.FOE?
                    PmonDecisionToUseMove.Target.mon(foePosition.a1, foePosition.a2):
                    PmonDecisionToUseMove.Target.mon(selfPosition.a1, selfPosition.a2);
            return PmonDecision.from(useMove);
        });
    }
    public static Function2<PmonDecision, Tuple2<PartyId, MonFieldPosition>, Tuple2<PartyId, MonFieldPosition>> pass() {
        return function((Tuple2<PartyId, MonFieldPosition> selfPosition,
                         Tuple2<PartyId, MonFieldPosition> foePosition) -> PmonDecision.from(new PmonDecisionToPass()));
    }
    // handlers
    public static PmonBattle.Handler multiple(Iterable<PmonBattle.Handler> handlers) {
        var multipleHandler = new PmonBattle.Handler.Extendable();
        handlers.forEach(multipleHandler::add);
        return multipleHandler;
    }
    public static PmonBattle.Handler ignoreUpdates() {
        return new PmonBattle.Handler.Editable();
    }
    public static PmonBattle.Handler checkHitsOnPmonOfGivenParty(Method0 onHit, PartyId partyId) {
        var handler = new PmonBattle.Handler.Extendable();
        handler.onLocalUpdate.add((partyId_, pmonUpdate) -> {
            if (partyId_ != PARTY_1_ID) return;
            pmonUpdate.get(new PmonUpdate.Handler() {
                @Override public void pass(PmonUpdateByPass update) {}
                @Override public void switchOut(PmonUpdateBySwitchOut update) {}
                @Override public void move(PmonUpdateByMove update) {
                    for (var updateOnTarget: update.usageResults) {
                        if (updateOnTarget.a1 != partyId) return;
                        updateOnTarget.a3.get(new PmonUpdateByMove.UsageResult.Handler() {
                            @Override public void hit(Iterable<PmonUpdateOnTarget> atomicUpdates) {
                                for (var atomicUpdate: atomicUpdates) {
                                    atomicUpdate.get(new PmonUpdateOnTarget.Handler() {
                                        @Override public void damage(PmonUpdateOnTargetByDamage update) {
                                            onHit.accept();
                                        }
                                        @Override public void statModify(PmonUpdateOnTargetByStatModifier update) {}
                                        @Override public void statusCondition(PmonUpdateOnTargetByStatusCondition update) {}
                                        @Override public void switchOut(PmonUpdateOnTargetBySwitchOut update) {}
                                    });
                                }
                            }
                            @Override public void miss(PmonUpdateByMove.UsageResult.MissType type) {}
                            @Override public void immobilised(PmonStatusCondition.Id id) {}
                        });
                    }
                }
                @Override public void other(PmonUpdateByOther update) {}
            });
        });
        return handler;
    }
    public static PmonBattle.Handler checkHitsOnPmon1(Method0 onHit) {
        return checkHitsOnPmonOfGivenParty(onHit, PARTY_1_ID);
    }
    public static PmonBattle.Handler checkHitsOnPmon2(Method0 onHit) {
        return checkHitsOnPmonOfGivenParty(onHit, PARTY_2_ID);
    }
    // after
    public static Method1<Context> ignoreAfter() {
        return c -> {};
    }
}
