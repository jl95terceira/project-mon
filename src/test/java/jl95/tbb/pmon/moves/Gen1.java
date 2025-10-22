package jl95.tbb.pmon.moves;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Gen1 {

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

            public BideStatus() {super(new Id());}
            public final P<Integer> turnNr = new P<>(0);
            public final P<Tuple2<PartyId, MonFieldPosition>> foe = new P<>(null);
            public final P<Integer> damage = new P<>(0);
        }
        var bideStatus = new BideStatus();
        bideStatus.allowDecide = false;
        bideStatus.afterTurn = () -> bideStatus.turnNr.set(bideStatus.turnNr.get()+1);
        bideStatus.onDamage = (partyId, monFieldPosition, damage) -> {
            bideStatus.damage.set(bideStatus.damage.get() + damage);
            bideStatus.foe.set(tuple(partyId, monFieldPosition));
        };
        bideStatus.afterTurnEffects = () -> {
            if (bideStatus.turnNr.get() < 3) {
                return strict(Map());
            }
            if (bideStatus.foe.get() == null) {
                return strict(Map());
            }
            var effects = new PmonEffects();
            effects.damage.power = PmonMovePower.constant(2 * bideStatus.damage.get());
            return strict(Map(tuple(
                    tuple(bideStatus.foe.get().a1, bideStatus.foe.get().a2),
                    effects)));
        };
        bideStatus.cureChanceAfterTurn = () -> bideStatus.turnNr.get().equals(3)? 100: 0;
        attrs.effects.status.statusConditions = strict(List(Chanced.certain(constant(bideStatus))));
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
}
