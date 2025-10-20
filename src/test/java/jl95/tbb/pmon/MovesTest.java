package jl95.tbb.pmon;

import jl95.lang.P;
import jl95.lang.StrictList;
import jl95.lang.variadic.Function2;
import jl95.lang.variadic.Method1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.attrs.PmonAttributes;
import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;

import static jl95.lang.SuperPowers.*;

public class MovesTest {

    public static Pmon.Id PMON_ID = new Pmon.Id();
    public static PmonType PMON_TYPE = new PmonType(new PmonType.Id()) {
        @Override
        public PmonMoveEffectivenessType effectivenessAgainst(PmonType other) {
            return PmonMoveEffectivenessType.NORMAL;
        }
    };
    public static int HP_MAX = 95;
    public static int HP0    = 50;
    public static PartyId PARTY_1_ID = new PartyId();
    public static PartyId PARTY_2_ID = new PartyId();

    public record Context(PmonGlobalContext gc, Pmon pmon1, Pmon pmon2) {}

    private static void set(PmonAttributes pmonAttributes) {
        pmonAttributes.baseStats.hp             = HP_MAX;
        pmonAttributes.baseStats.attack         = 55;
        pmonAttributes.baseStats.defense        = 40;
        pmonAttributes.baseStats.specialAttack  = 50;
        pmonAttributes.baseStats.specialDefense = 50;
        pmonAttributes.baseStats.speed          = 90;
    }
    private static PmonPartyEntry makePartyEntry(StrictList<PmonMove> moves) {
        var party = new PmonPartyEntry();
        var pmon = new Pmon(PMON_ID);
        set(pmon.attrs);
        pmon.status.hp = HP0;
        pmon.moves.addAll(moves);
        party.mons.add(pmon);
        return party;
    }

    public static void run1v1(
            Tuple2<StrictList<PmonMove>,StrictList<PmonMove>> moves,
            StrictList<Tuple2<
                    Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
                    Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> decisions,
            Method1<Context> after) {

        var rules  = new PmonRuleset();
        rules.rngCritical   = new PmonRuleset.Rng(constant(1.0));
        rules.rngStatModify = new PmonRuleset.Rng(constant(0.0));
        rules.rngHitNrTimes = new PmonRuleset.Rng(constant(0.0));
        var battle = new PmonBattle(rules);
        var party1 = makePartyEntry(moves.a1);
        var party2 = makePartyEntry(moves.a2);
        var decisionsIterator = decisions.iterator();
        PmonBattle.Listeners.Editable listeners = new PmonBattle.Listeners.Editable();
        P<PmonGlobalContext> gcRef = new P<>(null);
        listeners.onGlobalContext = gcRef::set;
        try {
            battle.spawn(
                    strict(Map(tuple(PARTY_1_ID, party1),
                               tuple(PARTY_2_ID, party2))),
                    new PmonInitialConditions(),
                    parties -> {
                        var decisionsForThisTurn = decisionsIterator.next();
                        var mon1FieldPosition = parties.get(PARTY_1_ID).iterator().next();
                        var mon2FieldPosition = parties.get(PARTY_2_ID).iterator().next();
                        return strict(Map(
                            tuple(PARTY_1_ID, strict(Map(tuple(mon1FieldPosition, decisionsForThisTurn.a1.apply(mon1FieldPosition, mon2FieldPosition))))),
                            tuple(PARTY_2_ID, strict(Map(tuple(mon2FieldPosition, decisionsForThisTurn.a2.apply(mon2FieldPosition, mon1FieldPosition)))))
                        ));
                    },
                    Battle.Listeners.ignore(),
                    not(decisionsIterator::hasNext)
            );
        }
        catch (Battle.InterruptedException ex) {/* continue */}
        after.accept(new Context(gcRef.get(), party1.mons.get(0), party2.mons.get(0)));
    }

    public static Tuple2<StrictList<PmonMove>,StrictList<PmonMove>> onlyPmon1HasMove(PmonMoveAttributes moveAttrs) {
        var move = new PmonMove(new PmonMove.Id(), new PmonType(PMON_TYPE.id) {

            @Override public PmonMoveEffectivenessType effectivenessAgainst(PmonType other) {
                return PmonMoveEffectivenessType.NORMAL;
            }
        });
        move.status.pp = 99;
        move.attrs = moveAttrs;
        return tuple(strict(List(move)), strict(List()));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMove(Function2<Tuple2<PartyId,MonFieldPosition>,MonFieldPosition,MonFieldPosition> against) {
        return strict(List(tuple(
                (mon1FieldPosition,mon2FieldPosition) -> {
                    var useMove = new PmonDecisionToUseMove();
                    useMove.moveIndex = 0;
                    var a = against.apply(mon1FieldPosition,mon2FieldPosition);
                    useMove.targets = strict(Map(tuple(a.a1, I(a.a2))));
                    return PmonDecision.from(useMove);
                },
                (mon2FieldPosition,mon1FieldPosition) -> PmonDecision.from(new PmonDecisionToPass()))));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMoveAgainstPmon2() {
        return onlyPmon1UsesMove((mon1FieldPosition,mon2FieldPosition) -> tuple(PARTY_2_ID, mon2FieldPosition));
    }
    public static StrictList<Tuple2<
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>,
            Function2<PmonDecision, MonFieldPosition, MonFieldPosition>>> onlyPmon1UsesMoveOnItself() {
        return onlyPmon1UsesMove((mon1FieldPosition,mon2FieldPosition) -> tuple(PARTY_1_ID, mon1FieldPosition));
    }
}
