package jl95.tbb.pmon._demo;

import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.mon.MonPosition;
import jl95.tbb.pmon.*;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.tbb.pmon.update.PmonUpdateByMove;
import jl95.tbb.pmon.update.PmonUpdateBySwitchIn;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.Set;

import static jl95.lang.SuperPowers.*;

public class PmonBattleDemo {

    public static class PartyIds {
        public static StrictMap<PartyId, String> namesMap = strict(Map());
        public static PartyId named(String name) {
            var id = new PartyId();
            namesMap.put(id, name);
            return id;
        }
        public static PartyId PLAYER = named("PLAYER");
        public static PartyId NPC    = named("RIVAL");
    }
    public static class Pmons {
        public static StrictMap<Pmon.Id, String> namesMap = strict(Map());
        public static Pmon.Id named(String name) {
            var id = new Pmon.Id();
            namesMap.put(id, name);
            return id;
        }
        public static Pmon pmon1 = new Pmon(named("RED"));
        public static Pmon pmon2 = new Pmon(named("BLUE"));
        public static Pmon pmon3 = new Pmon(named("GREEN"));
        public static Pmon pmon4 = new Pmon(named("YELLOW"));
    }
    public static class PmonTypes {
        public static PmonType NORMAL = new PmonType(new PmonType.Id()) {
            @Override
            public PmonMoveEffectivenessType effectivenessAgainst(PmonType other) {
                return PmonMoveEffectivenessType.NORMAL;
            }
        };
    }
    public static class MoveFactories {
        public static StrictMap<PmonMove.Id, String> namesMap = strict(Map());
        public static PmonMove.Id named(String name) {
            var id = new PmonMove.Id();
            namesMap.put(id, name);
            return id;
        }
        public static Function0<PmonMove> tackle = () -> {
            var move = new PmonMove(named("Tackle"), PmonTypes.NORMAL);
            move.attrs.power = PmonMovePower.typed(40);
            move.attrs.accuracy = 80;
            return move;
        };
        public static Function0<PmonMove> growl  = () -> {
            var move = new PmonMove(named("Growl"), PmonTypes.NORMAL);
            move.attrs.statModifiers.put(PmonStatModifierType.ATTACK, new Chanced<>(-1, 100));
            return move;
        };
        public static Function0<PmonMove> leer   = () -> {
            var move = new PmonMove(named("Leer"), PmonTypes.NORMAL);
            move.attrs.statModifiers.put(PmonStatModifierType.DEFENSE, new Chanced<>(-1, 100));
            return move;
        };
    }

    static {
        for (var pmon: I(Pmons.pmon1, Pmons.pmon2, Pmons.pmon3, Pmons.pmon4)) {
            pmon.attrs.baseStats.hp = 20;
            pmon.attrs.baseStats.attack = 5;
            pmon.attrs.baseStats.defense = 5;
            pmon.attrs.baseStats.speed = 10;
            pmon.restoreHp();
        }
        Pmons.pmon1.moves.add(MoveFactories.tackle.apply());
        Pmons.pmon1.moves.add(MoveFactories.growl .apply());
        Pmons.pmon2.moves.add(MoveFactories.tackle.apply());
        Pmons.pmon2.moves.add(MoveFactories.leer  .apply());
        Pmons.pmon3.moves.add(MoveFactories.tackle.apply());
        Pmons.pmon3.moves.add(MoveFactories.growl .apply());
        Pmons.pmon4.moves.add(MoveFactories.tackle.apply());
        Pmons.pmon4.moves.add(MoveFactories.leer  .apply());
    }

    public static void main(String[] args) {

        var battle = new PmonBattle(new PmonRuleset());
        var playerEntry = new PmonPartyEntry();
        playerEntry.mons.addAll(List(Pmons.pmon1, Pmons.pmon4));
        var npcEntry = new PmonPartyEntry();
        npcEntry.mons.addAll(List(Pmons.pmon2, Pmons.pmon3));
        Ref<MonGlobalContext<Pmon>> globalContextRef = new Ref<>();
        Ref<MonLocalContext<Pmon, PmonFoeView>> localContextRef = new Ref<>();
        battle.spawn(
                strict(Map(
                        tuple(PartyIds.PLAYER, playerEntry),
                        tuple(PartyIds.NPC, npcEntry))),
                new PmonInitialConditions(),
                function((PartyId p, StrictSet<MonPosition> monPositionsAble) -> {
                    throw new UnsupportedOperationException();
                }),
                new Battle.Listeners<>() {
                    @Override
                    public void onGlobalContext(MonGlobalContext<Pmon> context) {
                        globalContextRef.set(context);
                    }

                    @Override
                    public void onLocalContext(PartyId id, MonLocalContext<Pmon, PmonFoeView> localContext) {
                        if (id == PartyIds.PLAYER) {
                            localContextRef.set(localContext);
                        }
                    }

                    @Override
                    public void onLocalUpdate(PartyId id, PmonUpdate pmonUpdate) {
                        var party = globalContextRef.get().parties.get(id);
                        pmonUpdate.call(new PmonUpdate.Handlers() {
                            @Override
                            public void switchIn(PmonUpdateBySwitchIn update) {
                                System.out.printf("%s withdraws %s and switches in %s!\n",
                                        PartyIds.namesMap.get(id),
                                        Pmons.namesMap.get(party.monsOnField.get(update.monId).id),
                                        Pmons.namesMap.get(party.mons.get(update.monToSwitchInIndex).id));
                            }

                            @Override
                            public void move(PmonUpdateByMove update) {
                                System.out.printf("%s's %s used %s!\n",
                                        PartyIds.namesMap.get(id),
                                        Pmons.namesMap.get(party.monsOnField.get(update.monId).id),
                                        MoveFactories.namesMap.get(party.monsOnField.get(update.monId).moves.get(update.moveIndex).id)
                                );
                            }
                        });
                    }
                },
                constant(false)
        );
    }
}
