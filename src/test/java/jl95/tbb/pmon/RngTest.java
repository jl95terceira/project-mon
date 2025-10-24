package jl95.tbb.pmon;

import jl95.lang.I;
import org.junit.Test;

import static jl95.lang.SuperPowers.constant;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RngTest {

    @Test
    public void testRng() {
        var rng = new PmonRuleset.Rng();
        for (var i : I.range(1000)) {
            assertTrue(rng.roll(1.));
            assertTrue(rng.roll(100));
            assertFalse(rng.roll(0.));
            assertFalse(rng.roll(0));
        }
    }
    @Test
    public void testRngMock() {
        PmonRuleset.Rng rng;
        rng = new PmonRuleset.Rng(constant(0.));
        for (var i : I.range(1000)) {
            assertTrue(rng.roll(.5));
        }
        rng = new PmonRuleset.Rng(constant(1.));
        for (var i : I.range(1000)) {
            assertFalse(rng.roll(.5));
        }
    }
}
