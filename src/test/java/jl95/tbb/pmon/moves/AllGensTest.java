package jl95.tbb.pmon.moves;

import jl95.lang.I;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;
import jl95.tbb.mon.MonPartyFieldPosition;
import jl95.tbb.pmon.*;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictList;
import jl95.util.StrictMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.MovesTest.*;
import static org.junit.Assert.*;

public class AllGensTest {

    private static class AfterTurnStandardEffects implements PmonStatusCondition.AfterTurnEffects {
        private StrictList<PmonStatusCondition.AfterTurnEffects> list = strict(List());
        @Override public StrictMap<MonPartyFieldPosition, Iterable<PmonEffects>> apply(MonPartyFieldPosition monPartyFieldPosition, PmonLocalContext context) {
            return list
                    .map(a -> a.apply(monPartyFieldPosition,context))
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

    @Test public void testResetStatsOnSwitchOut() {
        attrs.effectsOnTarget = effectConstant(e -> {
            e.stats.statModifiers = strict(Map(
                    tuple(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 10))));
        });
        new Runner().run1vN(
                pmon1HasMoveVs2NoMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), pass())
                ),
                ignoreUpdates(),
                c -> {
                    assertEquals(Integer.valueOf(-2), c.pmon2().status.statModifiers.get(PmonStatModifierType.DEFENSE));
                });
        new Runner().run1vN(
                pmon1HasMoveVs2NoMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(pass(), switchOut(1))
                ),
                ignoreUpdates(),
                c -> {
                    assertEquals(0, c.pmon2().status.statModifiers.size());
                });
        new Runner().run1vN(
                pmon1HasMoveVs2NoMove(attrs),
                I(
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), pass()),
                        tuple(useMove(TARGET.FOE), switchOut(1)),
                        tuple(pass(), switchOut(0))
                ),
                ignoreUpdates(),
                c -> {
                    assertEquals(0, c.pmon2().status.statModifiers.size());
                });
    }
}
