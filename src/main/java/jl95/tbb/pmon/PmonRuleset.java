package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.rules.PmonContextUpdateRule;
import jl95.tbb.pmon.rules.PmonUpdateDetRule;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;

import java.util.*;

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public static PartyId NO_VICTOR = new PartyId();

    public final PmonRulesetConstants constants = new PmonRulesetConstants();
    public Function0<Double> rng = new Random()::nextDouble;

    public Double rng() { return rng.apply(); }

    public Integer detDamage(Pmon mon, PmonMove move, Pmon targetMon) {

        var v = new Ref<>(0);
        move.attrs.power.call(new PmonMovePower.Callbacks() {
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

    @Override
    public MonGlobalContext<Pmon> init(StrictMap<PartyId, MonPartyEntry<Pmon>> parties, PmonInitialConditions pmonInitialConditions) {
        var context = new MonGlobalContext<Pmon>();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var partyEntry = e.getValue();
            context.parties.put(partyId, MonParty.fromEntry(partyEntry));
        }
        return context;
    }

    @Override
    public Iterable<PmonUpdate> detInitialUpdates(MonGlobalContext<Pmon> context, PmonInitialConditions pmonInitialConditions) {
        return I();
    }

    @Override
    public MonLocalContext<Pmon, PmonFoeView> detLocalContext(MonGlobalContext<Pmon> context, PartyId ownPartyId) {
        var foePartiesView = strict(I
            .of(context.parties.keySet())
            .filter(id -> !id.equals(ownPartyId))
            .toMap(id -> id, id -> strict(I
                .of(context.parties.get(id).monsOnField.entrySet())
                .toMap(Map.Entry::getKey, e -> {
                    var foeMon = e.getValue();
                    var foeMonView = new PmonFoeView(foeMon.id);
                    foeMonView.types = foeMon.attrs.types;
                    foeMonView.status = foeMon.status;
                    return foeMonView;
                }))));
        return new MonLocalContext<>(context.parties.get(ownPartyId), foePartiesView);
    }

    @Override
    public Iterable<PmonUpdate> detUpdates(MonGlobalContext<Pmon> context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {

        return new PmonUpdateDetRule(this).detUpdates(context, decisionsMap);
    }

    @Override
    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {

        new PmonContextUpdateRule(this).update(context, pmonUpdate);
    }

    @Override
    public Iterable<PmonUpdate> detLocalUpdates(PmonUpdate pmonUpdate, PartyId partyId) {
        return List.of(pmonUpdate);
    }

    @Override
    public Optional<PartyId> detVictory(MonGlobalContext<Pmon> context) {
        Set<PartyId> partiesRemaining = Set();
        for (var e: context.parties.entrySet()) {
            var partyId = e.getKey();
            var party = e.getValue();
            for (var mon: party.mons) {
                if (mon.status.hp > 0) {
                    partiesRemaining.add(partyId);
                    break;
                }
            }
        }
        if (partiesRemaining.size() == 1) {
            return Optional.of(partiesRemaining.iterator().next());
        }
        else if (partiesRemaining.isEmpty()) {
            return Optional.of(NO_VICTOR);
        }
        return Optional.empty();
    }

    @Override
    public Boolean allowDecide(MonGlobalContext<Pmon> context, PartyId partyId, MonParty.MonId monId) {

        var mon = context.parties.get(partyId).monsOnField.get(monId);
        return true; //TODO: should be based on move lock-in, charging / recharging, etc
    }
}
