package jl95.tbb.pmon.status;

import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatus {

    public interface MoveCharge {

        interface Handler {
            void halfTurn(int index);
            void nrTurns(int index, int remaining);
        }

        void get(Handler handler);

        static MoveCharge halfTurn(int index) {
            return h -> h.halfTurn(index);
        }
        static MoveCharge nrTurns(int index, int remaining) {
            return h -> h.nrTurns(index, remaining);
        }
    }

    public int hp = 0;
    public Integer lastMoveUsedIndex = null; //TODO: use this
    public Tuple2<PartyId, MonFieldPosition> lastFoeByDamageOnSelf = null;
    public Integer damageByLastFoe = 0; // TODO:use this
    public Integer damageAccumulatedForTheTurn = 0;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public StrictMap<PmonStatusCondition.Id, PmonStatusCondition> statusConditions = strict(Map());
    public MoveCharge moveCharge = null; //TODO: use this
}
