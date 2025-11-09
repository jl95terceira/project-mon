package jl95.tbb.pmon.moves;

import jl95.lang.I;
import jl95.lang.P;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.*;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.util.StrictList;
import jl95.util.StrictMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.MovesTest.*;
import static org.junit.Assert.*;

public class Gen1Test {

    private static class AfterTurnStandardEffects implements PmonStatusCondition.AfterTurnEffects {
        private StrictList<PmonStatusCondition.AfterTurnEffects> list = strict(List());
        @Override public StrictMap<MonId, Iterable<PmonEffects>> apply(MonId monId, PmonLocalContext context) {
            return list
                    .map(a -> a.apply(monId,context))
                    .reduce(strict(Map()), (map,a) -> {
                        for (var e: a.entrySet()) {
                            map.put(e.getKey(), !map.containsKey(e.getKey())
                                    ? e.getValue()
                                    : I.flat(map.get(e.getKey()), e.getValue()));
                        }
                        return map;
                    });
        }
        AfterTurnStandardEffects other(PmonStatusCondition.AfterTurnEffects e) {
            list.add(e);
            return this;
        }
        AfterTurnStandardEffects cureSelfIf(PmonStatusCondition.Id statusConditionId, Function0<Boolean> predicate) {
            list.add((monId,context) -> {
                if (!predicate.apply()) {
                    return strict(Map());
                }
                var effectsOnSelf = new PmonEffects();
                effectsOnSelf.status.statusConditionsCure.add(ctx -> Chanced.certain(statusConditionId));
                return strict(Map(tuple(monId,I(effectsOnSelf))));
            });
            return this;
        }
        AfterTurnStandardEffects cureSelf(PmonStatusCondition.Id statusConditionId) {
            return cureSelfIf(statusConditionId, constant(true));
        }
    }

    private static Function1<PmonEffects, PmonMove.EffectsContext> effectConstant(Method1<PmonEffects> effectsBuilder) {
        return ctx -> {
            var effects = new PmonEffects();
            effects.damage.pmonType = PMON_TYPE;
            effectsBuilder.accept(effects);
            return effects;
        };
    }

    private PmonMove attrs;
    private PmonMove attrs2;

    @Before public void setup() {
        attrs  = new PmonMove(new PmonMove.Id(), PMON_TYPE);
        attrs2 = new PmonMove(new PmonMove.Id(), PMON_TYPE);
    }
    @After public void teardown() {
        attrs = null;
        attrs2 = null;
    }

    @Test public void testDamage() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertEquals(c.pmon1().status.hp, HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test public void testDamageMoveFirst() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        var pmon1WasHit = new P<>(false);
        new Runner().run1v1(
                pmon1And2HaveMoves(attrs,attrs2),
                I(
                        tuple(useMove(TARGET.FOE), useMove(TARGET.FOE))),
                multiple(I(
                        checkHitsOnPmon1(() -> pmon1WasHit.set(true)),
                        checkHitsOnPmon2(() -> assertFalse(pmon1WasHit.get()) /* assert that pmon 1 hit first due to higher speed*/)
                )),
                c -> {
                    assertTrue(c.pmon1().status.hp < HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test public void testDrain() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
            e.damage.healbackFactor = .5;
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertTrue(c.pmon1().status.hp > HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test public void testLowerStat() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.stats.statModifiers = strict(Map(
                    tuple(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 10))));
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertTrue(c.pmon2().status.hp == HP0);
                    assertEquals(Integer.valueOf(-1), c.pmon2().status.statModifiers.getOrDefault(PmonStatModifierType.DEFENSE, 0));
                });
    }
    @Test public void testDamageAndLowerStat() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
            e.stats.statModifiers = strict(Map(
                    tuple(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 10))));
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertTrue(c.pmon2().status.hp < HP0);
                    assertEquals(Integer.valueOf(-1), c.pmon2().status.statModifiers.getOrDefault(PmonStatModifierType.DEFENSE, 0));
                });
    }
    @Test public void testRaiseStat() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.ATTACK, Chanced.certain(2))));
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.SELF), pass())),
                ignoreUpdates(),
                c -> {
                    assertEquals(Integer.valueOf(2), c.pmon1().status.statModifiers.getOrDefault(PmonStatModifierType.ATTACK, 0));
                });
    }
    @Test public void testRaiseStatTwice() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.ATTACK, Chanced.certain(2))));
        });
        new Runner().run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.SELF), pass()),
                        tuple(pass(), pass()),
                        tuple(useMove(TARGET.SELF), pass())),
                ignoreUpdates(),
                c -> {
                    assertEquals(Integer.valueOf(4), c.pmon1().status.statModifiers.getOrDefault(PmonStatModifierType.ATTACK, 0));
                });
    }
    private void testMultiHits(Double rng, Integer nrHitsExpected) {
        var rules = Runner.rulesDefaults();
        rules.rngHitNrTimes = new PmonRuleset.Rng(constant(rng));
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(15);
        });
        attrs.hitNrTimesRange = tuple(2,4);
        var nrHits = new P<>(0);
        new Runner(rules)
                .run1v1(
                        pmon1HasMove(attrs),
                        I(
                                tuple(useMove(TARGET.FOE), pass())),
                        checkHitsOnPmon2(() -> nrHits.set(nrHits.get() + 1)),
                        c -> {
                            assertTrue(c.pmon2().status.hp < HP0);
                        });
        assertEquals(nrHitsExpected, nrHits.get());
    }
    @Test public void testMultiHitsMin() {
        testMultiHits(0.0, 2);
    }
    @Test public void testMultiHitsMean() {
        testMultiHits(0.5, 3);
    }
    @Test public void testMultiHitsMax() {
        testMultiHits(1.0, 4);
    }
    enum BIDE_TEST_FLAG {
        STOP_EARLY, STOP_LATE;
    }
    private void _testBide(BIDE_TEST_FLAG t) {
        class BideStatus extends PmonStatusCondition {

            public BideStatus() {
                super(new Id());
                allowDecide = false;
                afterTurn = () -> turnNr.set(turnNr.get()+1);
                onDamageToSelf = (partyId, monFieldPosition, damage) -> {
                    damageAccum.set(damageAccum.get() + damage);
                };
                afterTurnEffects = new AfterTurnStandardEffects()
                        .other((monId,context) -> {
                            if (turnNr.get() < 3) {
                                return strict(Map());
                            }
                            var self = context.ownParty.monsOnField.get(monId.position());
                            var effectsOnFoe = new PmonEffects();
                            effectsOnFoe.damage.power = PmonMove.Power.constant(2 * damageAccum.get());
                            var foe = self.status.lastFoeByDamageOnSelf;
                            return strict(I.flat(
                                    foe != null?
                                            I(tuple(foe,effectsOnFoe)): I()
                            ).toMap(t -> t.a1, t -> I(t.a2)));
                        })
                        .cureSelfIf(id, () -> turnNr.get() >= 3);
            }
            public final P<Integer> turnNr = new P<>(0);
            public final P<Integer> damageAccum = new P<>(0);
        }
        attrs.effectsOnTarget = effectConstant(e -> {
            e.status.statusConditionsInflict = strict(List(ctx -> Chanced.certain(BideStatus::new)));
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        var nrHitsOnPmon1 = new P<>(0);
        var nrHitsOnPmon2 = new P<>(0);
        new Runner().run1v1(
                pmon1And2HaveMoves(attrs2, attrs),
                t == BIDE_TEST_FLAG.STOP_EARLY
                ? I(
                        tuple(pass(), useMove(TARGET.SELF)),
                        tuple(useMove(TARGET.FOE), pass()))
                :
                t == BIDE_TEST_FLAG.STOP_LATE
                ? I(
                        tuple(pass(), useMove(TARGET.SELF)),
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(pass(), pass()),
                        tuple(pass(), pass()))
                : I(
                        tuple(pass(), useMove(TARGET.SELF)),
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), pass())),
                multiple(I(
                        checkHitsOnPmon1(() -> nrHitsOnPmon1.set(nrHitsOnPmon1.get()+1)),
                        checkHitsOnPmon2(() -> nrHitsOnPmon2.set(nrHitsOnPmon2.get()+1))
                )),
                c -> {
                    assertTrue(c.pmon2().status.hp < HP0);
                    if (t == BIDE_TEST_FLAG.STOP_EARLY) {
                        assertEquals(Integer.valueOf(1), nrHitsOnPmon2.get());
                        assertTrue(c.pmon1().status.hp == HP0);
                    }
                    else {
                        assertEquals(Integer.valueOf(2), nrHitsOnPmon2.get());
                        assertTrue(c.pmon1().status.hp < HP0);
                        assertTrue(c.pmon2().status.statusConditions.isEmpty());
                    }
                });
    }
    @Test public void testBide() {
        _testBide(null);
    }
    @Test public void testBideStopEarly() {
        _testBide(BIDE_TEST_FLAG.STOP_EARLY);
    }
    @Test public void testBideStopLate() {
        _testBide(BIDE_TEST_FLAG.STOP_LATE);
    }
    @Test public void testFlinch() {
        class FlinchableStatus extends PmonStatusCondition {

            public FlinchableStatus() {
                super(new Id());
                immobiliseChanceOnMove = constant(50);
                afterTurnEffects = new AfterTurnStandardEffects()
                        .cureSelf(id);
            }
        }
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(10);
            e.status.statusConditionsInflict = strict(List(ctx -> Chanced.certain(FlinchableStatus::new)));
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(50);
        });
        for (var toFlinch: I(false,true)) {
            var rules = Runner.rulesDefaults();
            rules.rngImmobilise = new PmonRuleset.Rng(constant(!toFlinch? 1.: 0.));
            new Runner(rules).run1v1(
                    pmon1And2HaveMoves(attrs, attrs2),
                    I(
                            tuple(useMove(TARGET.FOE), useMove(TARGET.FOE))),
                    ignoreUpdates(),
                    c -> {
                        assertTrue(c.pmon2().status.hp < HP0);
                        if (!toFlinch) {
                            assertTrue(c.pmon1().status.hp < HP0);
                        }
                        else {
                            assertEquals(HP0, c.pmon1().status.hp);
                        }
                        assertTrue(c.pmon2().status.statusConditions.isEmpty());
                    });
        }
    }
    @Test public void testSleep() {
        class SleepStatus extends PmonStatusCondition {

            private P<Integer> nrTurns = new P<>(0);
            public SleepStatus() {
                super(new Id());
                afterTurn = () -> nrTurns.set(nrTurns.get()+1);
                immobiliseChanceOnMove = constant(100);
                afterTurnEffects = new AfterTurnStandardEffects()
                        .cureSelfIf(id, () -> nrTurns.get() >= 3);
            }
        }
        attrs.effectsOnTarget = effectConstant(e -> {
            e.status.statusConditionsInflict = strict(List(ctx -> Chanced.certain(SleepStatus::new)));
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        var nrHits1 = new P<>(0);
        var nrHits2 = new P<>(0);
        new Runner().run1v1(
                pmon1And2HaveMoves(attrs, attrs2),
                I(
                        tuple(useMove(TARGET.FOE), useMove(TARGET.FOE)),
                        tuple(pass(), useMove(TARGET.FOE)),
                        tuple(pass(), useMove(TARGET.FOE)),
                        tuple(pass(), useMove(TARGET.FOE)),
                        tuple(pass(), useMove(TARGET.FOE))),
                multiple(I(
                        checkHitsOnPmon1(() -> nrHits1.set(nrHits1.get()+1)),
                        checkHitsOnPmon2(() -> nrHits2.set(nrHits2.get()+1)))),
                c -> {
                    assertEquals(Integer.valueOf(2), nrHits1.get());
                    assertEquals(Integer.valueOf(0), nrHits2.get());
                });
    }
    @Test public void testConfusion() {
        class ConfusedStatus extends PmonStatusCondition {

            public ConfusedStatus() {
                super(new Id());
                immobiliseChanceOnMove = constant(50);
                onImmobilisedEffectsOnSelf = () -> {
                    var effects = new PmonEffects();
                    effects.damage.power = PmonMove.Power.typed(20);
                    effects.damage.pmonType = PMON_TYPE;
                    return effects;
                };
            }
        }
        attrs.effectsOnTarget = effectConstant(e -> {
            e.status.statusConditionsInflict = strict(List(ctx -> Chanced.certain(ConfusedStatus::new)));
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(50);
        });
        for (var confused: I(false,true)) {
            var rules = Runner.rulesDefaults();
            rules.rngImmobilise = new PmonRuleset.Rng(constant(!confused? 1.0: 0.0));
            new Runner(rules).run1v1(
                    pmon1And2HaveMoves(attrs, attrs2),
                    I(
                            tuple(useMove(TARGET.FOE), useMove(TARGET.FOE))
                    ),
                    ignoreUpdates(),
                    c -> {
                        assertFalse(c.pmon2().status.statusConditions.isEmpty());
                        if (!confused) {
                            assertTrue(c.pmon1().status.hp < HP0);
                            assertTrue(c.pmon2().status.hp == HP0);
                        } else {
                            assertTrue(c.pmon1().status.hp == HP0);
                            assertTrue(c.pmon2().status.hp < HP0);
                        }
                    });
        }
    }
    @Test public void testCounter() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(20);
        });
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.other(self -> 2*self.status.damageAccumulatedForTheTurn);
        });
        for (var attack: I(false,true)) {
            new Runner().run1v1(
                    pmon1And2HaveMoves(attrs, attrs2),
                    !attack? I(
                            tuple(pass(), useMove(TARGET.FOE))
                    ): I(
                            tuple(useMove(TARGET.FOE), useMove(TARGET.FOE))
                    ),
                    ignoreUpdates(),
                    c -> {
                        if (!attack) {
                            assertEquals(HP0, c.pmon1().status.hp);
                            assertEquals(HP0, c.pmon2().status.hp);
                        }
                        else {
                            assertTrue(c.pmon1().status.hp < HP0);
                            assertTrue(c.pmon2().status.hp < HP0);
                            assertTrue((HP0 - c.pmon1().status.hp) >= 2*(HP0 - c.pmon2().status.hp));
                        }
                    });
        }
    }
    enum CHARGE_TEST_FLAG {
        STOP_EARLY;
    }
    private void _testCharge(Boolean isUntargetable, CHARGE_TEST_FLAG t) {
        var CHARGE_STATUS_ID = new PmonStatusCondition.Id();
        class ChargeStatus extends PmonStatusCondition {
            public ChargeStatus() {
                super(CHARGE_STATUS_ID);
                this.untargetable = isUntargetable;
            }
        }
        attrs.effectsOnSelf = ctx -> {
            var e = new PmonEffects();
            if (!ctx.originMon().status.statusConditions.containsKey(CHARGE_STATUS_ID)) {
                e.status.statusConditionsInflict = strict(List(ctx_ -> Chanced.certain(ChargeStatus::new)));
                e.lockDecision.lock = true;
            }
            else {
                e.status.statusConditionsCure = strict(List(ctx_ -> Chanced.certain(CHARGE_STATUS_ID)));
            }
            return e;
        };
        attrs.effectsOnTarget = ctx -> {
            if (!ctx.originMon().status.statusConditions.containsKey(CHARGE_STATUS_ID)) {
                return new PmonEffects();
            }
            var chargedEffect = new PmonEffects();
            chargedEffect.damage.power = PmonMove.Power.typed(30);
            chargedEffect.damage.pmonType = PMON_TYPE;
            return chargedEffect;
        };
        attrs2.effectsOnTarget = effectConstant(e -> {
            e.damage.power = PmonMove.Power.typed(10);
        });
        for (var tryToForceDecision: I(true,false)) {
            var nrHits1 = new P<>(0);
            var nrHits2 = new P<>(0);
            new Runner().run1v1(
                    pmon1And2HaveMoves(attrs, attrs2),
                    I.flat(
                    I(
                            tuple(useMove(TARGET.FOE), useMove(TARGET.FOE)),
                            tuple(tryToForceDecision? useMove(TARGET.FOE): pass(), useMove(TARGET.FOE)),
                            tuple(useMove(TARGET.FOE), useMove(TARGET.FOE))),
                    t == CHARGE_TEST_FLAG.STOP_EARLY? 
                            I(): 
                            I(tuple(tryToForceDecision? useMove(TARGET.FOE): pass(), useMove(TARGET.FOE)))
                    ),
                    multiple(I(
                            checkHitsOnPmon1(() -> nrHits1.set(nrHits1.get() + 1)),
                            checkHitsOnPmon2(() -> nrHits2.set(nrHits2.get() + 1)),
                            new PmonBattle.Handler() {
                                @Override
                                public void onGlobalContext(PmonGlobalContext pmonGlobalContext) {
                                    var mon1Hp = pmonGlobalContext.parties.get(PARTY_1_ID).monsOnField.values().iterator().next().status.hp;
                                    var mon2Hp = pmonGlobalContext.parties.get(PARTY_2_ID).monsOnField.values().iterator().next().status.hp;
                                    return;
                                }

                                @Override
                                public void onLocalContext(PartyId id, PmonLocalContext pmonLocalContext) {
                                    return;
                                }

                                @Override
                                public void onLocalUpdate(PartyId id, PmonUpdate pmonUpdate) {
                                    return;
                                }
                            })),
                    c -> {
                        assertEquals(Integer.valueOf(!isUntargetable?
                                t == CHARGE_TEST_FLAG.STOP_EARLY? 3: 4:
                                t == CHARGE_TEST_FLAG.STOP_EARLY? 1: 2), nrHits1.get());
                        assertEquals(Integer.valueOf(
                                t == CHARGE_TEST_FLAG.STOP_EARLY? 1: 2), nrHits2.get());
                    });
        }
    }
    @Test public void testCharge() {
        _testCharge(false, null);
    }
    @Test public void testChargeStopEarly() {
        _testCharge(false, CHARGE_TEST_FLAG.STOP_EARLY);
    }
    @Test public void testChargeUntargetable()  {
        _testCharge(true, null);
    }
    @Test public void testChargeUntargetableStopEarly() {
        _testCharge(true, CHARGE_TEST_FLAG.STOP_EARLY);
    }
}
