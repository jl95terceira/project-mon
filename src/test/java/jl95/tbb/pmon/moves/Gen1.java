package jl95.tbb.pmon.moves;

import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.status.PmonStatModifierType;
import org.junit.Test;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.MovesTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Gen1 {

    @Test
    public void testDamage() {
        var attrs = new PmonMoveAttributes(PMON_TYPE);
        attrs.effects.damage.power = PmonMovePower.typed(20);
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
                c -> {
                    assertEquals(c.pmon1().status.hp, HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test
    public void testDrain() {
        var attrs = new PmonMoveAttributes(PMON_TYPE);
        attrs.effects.damage.power = PmonMovePower.typed(20);
        attrs.effects.damage.healbackFactor = .5;
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
                c -> {
                    assertTrue(c.pmon1().status.hp > HP0);
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
    @Test
    public void testDamageAndReduceStat() {
        var attrs = new PmonMoveAttributes(PMON_TYPE);
        attrs.effects.damage.power = PmonMovePower.typed(20);
        attrs.effects.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 10))));
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
                c -> {
                    assertTrue(c.pmon2().status.hp < HP0);
                    assertEquals(Integer.valueOf(-1), c.pmon2().status.statModifiers.getOrDefault(PmonStatModifierType.DEFENSE, 0));
                });
    }
    @Test
    public void testRaiseStat() {
        var attrs = new PmonMoveAttributes(PMON_TYPE);
        attrs.effects.stats.statModifiers = strict(Map(tuple(PmonStatModifierType.ATTACK, Chanced.certain(2))));
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveOnItself(),
                c -> {
                    assertEquals(Integer.valueOf(2), c.pmon1().status.statModifiers.getOrDefault(PmonStatModifierType.ATTACK, 0));
                });
    }
    @Test
    public void testMultiHits() {
        var attrs = new PmonMoveAttributes(PMON_TYPE);
        attrs.effects.damage.power = PmonMovePower.typed(15);
        attrs.hitNrTimesRange = tuple(2,5);
        run1v1(
                onlyPmon1HasMove(attrs),
                onlyPmon1UsesMoveAgainstPmon2(),
                c -> {
                    assertTrue(c.pmon2().status.hp < HP0);
                });
    }
}
