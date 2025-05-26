package jl95.tbb.pmon.rules;

import jl95.lang.Ref;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonMove;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.update.PmonUpdateByMoveDamage;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineDamage {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineDamage(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public PmonUpdateByMoveDamage detDamage(Pmon mon, PmonMove move, Pmon targetMon) {

        var v = new PmonUpdateByMoveDamage();
        move.attrs.power.call(new PmonMovePower.Handlers() {
            @Override
            public void typed(Integer power) {
                var sourceAttack = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.attack
                        : stats.specialAttack).apply(mon.attrs.baseStats);
                var targetDefense = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.defense
                        : stats.specialDefense).apply(targetMon.attrs.baseStats);
                for (var targetMonType: targetMon.attrs.types.values()) {
                    v.effectivenessFactor *= ruleset.constants.POWER_FACTOR_MAP.get(move.attrs.pmonType.effectivenessAgainst(targetMonType));
                }
                //TODO: consider abilities, status conditions, etc.
                v.damage = 10;
            }
            @Override
            public void constant(Integer damage) { v.damage = damage; }
            @Override
            public void byHp(Double percent) {
                v.damage = (int) (percent * targetMon.status.hp);
            }
            @Override
            public void byMaxHp(Double percent) { v.damage = (int) (percent * targetMon.attrs.baseStats.hp); }
        });
        return v;
    }
}
