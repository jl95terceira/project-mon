package jl95.tbb.pmon.rules;

import jl95.lang.Ref;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonMove;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineDamage {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineDamage(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Integer detDamage(Pmon mon, PmonMove move, Pmon targetMon) {

        var v = new Ref<>(0);
        move.attrs.power.call(new PmonMovePower.Handlers() {
            @Override
            public void typed(Integer power) {
                var sourceAttack = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.attack
                        : stats.specialAttack).apply(mon.attrs.baseStats);
                var targetDefense = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.defense
                        : stats.specialDefense).apply(targetMon.attrs.baseStats);
            }
            @Override
            public void constant(Integer damage) { v.set(damage); }
            @Override
            public void byHp(Double percent) {
                v.set((int) (percent * targetMon.status.hp));
            }
            @Override
            public void byMaxHp(Double percent) { v.set((int) (percent * targetMon.attrs.baseStats.hp)); }
        });
        return 10; //TODO: actually calculate the damage; consider abilities, status conditions, etc.
    }
}
