package jl95.tbb.pmon.moves;

import jl95.lang.I;
import jl95.lang.P;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.MovesTest.*;
import static org.junit.Assert.*;

public class Gen1Test {

    private PmonMoveAttributes attrs;
    private PmonMoveAttributes attrs2;

    @Before
    public void setup() {
        attrs  = new PmonMoveAttributes(PMON_TYPE);
        attrs2 = new PmonMoveAttributes(PMON_TYPE);
    }
    @After
    public void teardown() {
        attrs = null;
        attrs2 = null;
    }

    @Test
    public void testDamage() {
        attrs.effects.damage.power = PmonMovePower.typed(20);
        run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertEquals(c.pmon1().status.hp, HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test
    public void testDamageMoveFirst() {
        attrs.effects.damage.power = PmonMovePower.typed(20);
        attrs2.effects.damage.power = PmonMovePower.typed(20);
        var pmon1WasHit = new P<>(false);
        run1v1(
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
    @Test
    public void testDrain() {
        attrs.effects.damage.power = PmonMovePower.typed(20);
        attrs.effects.damage.healbackFactor = .5;
        run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertTrue(c.pmon1().status.hp > HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test
    public void testDamageAndReduceStat() {
        attrs.effects.damage.power = PmonMovePower.typed(20);
        attrs.effects.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 10))));
        run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass())),
                ignoreUpdates(),
                c -> {
                    assertTrue(c.pmon2().status.hp < HP0);
                    assertEquals(Integer.valueOf(-1), c.pmon2().status.statModifiers.getOrDefault(PmonStatModifierType.DEFENSE, 0));
                });
    }
    @Test
    public void testRaiseStat() {
        attrs.effects.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.ATTACK, Chanced.certain(2))));
        run1v1(
                pmon1HasMove(attrs),
                I(
                        tuple(useMove(TARGET.SELF), pass())),
                ignoreUpdates(),
                c -> {
                    assertEquals(Integer.valueOf(2), c.pmon1().status.statModifiers.getOrDefault(PmonStatModifierType.ATTACK, 0));
                });
    }
    @Test
    public void testRaiseStatTwice() {
        attrs.effects.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.ATTACK, Chanced.certain(2))));
        run1v1(
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
        attrs.effects.damage.power = PmonMovePower.typed(15);
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
    @Test
    public void testMultiHitsMin() {
        testMultiHits(0.0, 2);
    }
    @Test
    public void testMultiHitsMean() {
        testMultiHits(0.5, 3);
    }
    @Test
    public void testMultiHitsMax() {
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
                onDamage = (partyId, monFieldPosition, damage) -> {
                    damageAccum.set(damageAccum.get() + damage);
                    foe.set(tuple(partyId, monFieldPosition));
                };
                afterTurnEffects = (partyId,monId,context) -> {
                    if (turnNr.get() < 3) {
                        return strict(Map());
                    }
                    var effectsOnFoe = new PmonEffects();
                    effectsOnFoe.damage.power = PmonMovePower.constant(2 * damageAccum.get());
                    var effectsOnSelf = new PmonEffects();
                    effectsOnSelf.status.statusConditionsCure.add(Chanced.certain(id)); // cure self
                    return strict(I.flat(
                            foe.get() != null?
                            I(tuple(tuple(foe.get().a1, foe.get().a2),effectsOnFoe)): I(),
                            I(tuple(tuple(partyId     , monId       ),effectsOnSelf))
                    ).toMap(t -> t.a1, t -> t.a2));
                };
            }
            public final P<Integer> turnNr = new P<>(0);
            public final P<Tuple2<PartyId, MonFieldPosition>> foe = new P<>(null);
            public final P<Integer> damageAccum = new P<>(0);
        }
        attrs.effects.status.statusConditionsInflict = strict(List(Chanced.certain(BideStatus::new)));
        attrs2.effects.damage.power = PmonMovePower.typed(20);
        var nrHitsOnPmon1 = new P<>(0);
        var nrHitsOnPmon2 = new P<>(0);
        run1v1(
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
    @Test
    public void testBide() {
        _testBide(null);
    }
    @Test
    public void testBideStopEarly() {
        _testBide(BIDE_TEST_FLAG.STOP_EARLY);
    }
    @Test
    public void testBideStopLate() {
        _testBide(BIDE_TEST_FLAG.STOP_LATE);
    }
    @Test
    public void testFlinch() {
        attrs.effects.damage.power = PmonMovePower.typed(10);
        class FlinchableStatus extends PmonStatusCondition {

            private P<Integer> turnNr = new P<>(0);
            public FlinchableStatus() {
                super(new Id());
                immobiliseChanceOnMove = constant(50);
                afterTurn = () -> turnNr.set(turnNr.get()+1);
                afterTurnEffects = (partyId, monId, context) -> {
                    var effectsOnSelf = new PmonEffects();
                    effectsOnSelf.status.statusConditionsCure.add(Chanced.certain(id)); // cure self
                    return strict(Map(tuple(tuple(partyId,monId),effectsOnSelf)));
                };
            }
        }
        attrs.effects.status.statusConditionsInflict = strict(List(Chanced.certain(FlinchableStatus::new)));
        attrs2.effects.damage.power = PmonMovePower.typed(50);
        for (var toFlinch: I(true)) {
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
    @Test
    public void testSleep() {
        class SleepStatus extends PmonStatusCondition {

            private P<Integer> nrTurns = new P<>(0);
            public SleepStatus() {
                super(new Id());
                afterTurn = () -> nrTurns.set(nrTurns.get()+1);
                immobiliseChanceOnMove = constant(100);
                afterTurnEffects = (partyId,monId,context) -> {
                    if (nrTurns.get() < 3) {
                        return strict(Map());
                    }
                    var effectsOnSelf = new PmonEffects();
                    effectsOnSelf.status.statusConditionsCure.add(Chanced.certain(id));
                    return strict(Map(tuple(tuple(partyId,monId),effectsOnSelf)));
                };
            }
        }
        attrs.effects.status.statusConditionsInflict = strict(List(Chanced.certain(SleepStatus::new)));
        attrs2.effects.damage.power = PmonMovePower.typed(20);
        var nrHits1 = new P<>(0);
        var nrHits2 = new P<>(0);
        run1v1(
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
                    System.out.println(nrHits1.get());
                    assertEquals(Integer.valueOf(2), nrHits1.get());
                    System.out.println(nrHits2.get());
                    assertEquals(Integer.valueOf(0), nrHits2.get());
                });
    }
}
