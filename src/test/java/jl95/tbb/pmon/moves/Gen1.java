package jl95.tbb.pmon.moves;

import jl95.lang.P;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.status.PmonStatModifierType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.MovesTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Gen1 {

    private PmonMoveAttributes attrs;

    @Before
    public void setup() {
        attrs = new PmonMoveAttributes(PMON_TYPE);
    }
    @After
    public void teardown() {
        attrs = null;
    }

    @Test
    public void testDamage() {
        attrs.effects.damage.power = PmonMovePower.typed(20);
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
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
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
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
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
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
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveOnItself(),
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
                        onlyPmon1HasMove(attrs),
                        onlyPmon1UsesMoveAgainstPmon2(),
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
}
