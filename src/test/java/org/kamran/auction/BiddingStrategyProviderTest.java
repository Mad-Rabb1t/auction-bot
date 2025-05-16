package org.kamran.auction;

import org.junit.jupiter.api.Test;
import org.kamran.provider.BiddingStrategyProvider;
import org.kamran.strategy.AdaptiveStrategy;
import org.kamran.strategy.AggressiveStrategy;
import org.kamran.strategy.BiddingStrategy;
import org.kamran.strategy.ConservativeStrategy;
import org.kamran.strategy.ZeroBidStrategy;

import static org.junit.jupiter.api.Assertions.*;

class BiddingStrategyProviderTest {

    @Test
    void testDetermineStrategy_TargetAchieved_ReturnsZeroBidStrategy() {
        AuctionState state = new AuctionState(10, 100);
        // Set ownQuantityWon to meet or exceed target
        int target = state.getTargetQuantity(); // 10/2 + 1 = 6
        state.updateQuantities(target, 0); // Own wins 'target' QU

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(ZeroBidStrategy.class, strategy, "Should be ZeroBidStrategy when target is achieved");
    }

    @Test
    void testDetermineStrategy_OpponentCannotWin_ReturnsZeroBidStrategy() {
        AuctionState state = new AuctionState(10, 100);
        state.updateQuantities(6, 0);

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(ZeroBidStrategy.class, strategy, "Should be ZeroBidStrategy if opponent cannot mathematically win and we have some QU");

        AuctionState stateNoWin = new AuctionState(10, 100); // Target 6
        BiddingStrategy s0 = BiddingStrategyProvider.determineStrategy(stateNoWin);
        assertFalse(s0 instanceof ZeroBidStrategy);


        AuctionState stateEdge = new AuctionState(2, 100); // Target 2
        stateEdge.updateQuantities(1, 0); // Own=1, Opp=0, RemQ=1. Max opp can get is 0+1=1. Own has 1. Tie possible. Not Zero.
        BiddingStrategy sEdge = BiddingStrategyProvider.determineStrategy(stateEdge);
        assertFalse(sEdge instanceof ZeroBidStrategy, "Tie possible, shouldn't be ZeroBidStrategy");

        AuctionState stateEdgeWin = new AuctionState(4, 100); // Target 3
        stateEdgeWin.updateQuantities(2, 0); // Own=2, Opp=0, RemQ=2. Max opp can get is 0+2=2. Own has 2. Tie possible.
        BiddingStrategy sEdgeWin = BiddingStrategyProvider.determineStrategy(stateEdgeWin);
        assertFalse(sEdgeWin instanceof ZeroBidStrategy);
    }

    @Test
    void testDetermineStrategy_CriticalRounds_CashRich_ReturnsAggressiveStrategy() {
        AuctionState state = new AuctionState(10, 100); // Target = 6
        // OwnQW=0, OppQW=0, RemQ=10, OwnC=100, OppC=100. Needed=6, RemRounds=5. Not critical.
        // To make it critical: NeededQU > 0 AND RemRounds <= NeededQU
        // Let's say OwnQW=2, RemQ=6. Needed=4, RemRounds=3. Critical.
        state.updateQuantities(2, 0); // Own has 2 QU, RemQ = 8. Target=6, Needed=4
        state.updateQuantities(0, 0); // RemQ = 6. Needed=4, RemRounds=3. Critical.
        state.updateCash(0, 50); // ownCash=100, oppCash=50. Own is cash rich.

        assertTrue(state.getOwnCash() > state.getOpponentCash() / 2, "Own cash should be rich for this test");
        assertTrue(state.getRemainingRounds() > 0);
        assertTrue(state.getRemainingRounds() <= state.getNeededQuantityToWin());

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(AggressiveStrategy.class, strategy, "Should be AggressiveStrategy in critical rounds with cash advantage");
    }

    @Test
    void testDetermineStrategy_CriticalRounds_CashPoor_ReturnsAdaptiveStrategy() {
        AuctionState state = new AuctionState(10, 100); // Target = 6
        // OwnQW=2, RemQ=6. Needed=4, RemRounds=3. Critical.
        state.updateQuantities(2, 0); // Own has 2 QU, RemQ = 8
        state.updateQuantities(0, 0); // RemQ = 6. Needed=4, RemRounds=3. Critical
        state.updateCash(70, 0); // ownCash=30, oppCash=100. Own is cash poor relative to opponent / initial.
        // Condition: state.getOwnCash() > state.getOpponentCash() / 2 || state.getOwnCash() > state.getInitialCash() / 4
        // 30 > 50 (false) || 30 > 25 (true) -> this will still be aggressive due to second part of OR
        // Let's make it fail both parts of OR for aggressive
        state = new AuctionState(10, 100);
        state.updateQuantities(2, 0); // Own: 2, Opp: 0, RemQ: 8
        state.updateQuantities(0, 0); // Own: 2, Opp: 0, RemQ: 6 (Rounds: 3, Needed: 4) -> Critical
        state.updateCash(80, 0);      // OwnCash: 20, OppCash: 100. (20 > 50 F || 20 > 25 F) -> Adaptive

        assertTrue(state.getRemainingRounds() > 0);
        assertTrue(state.getRemainingRounds() <= state.getNeededQuantityToWin());
        assertFalse(state.getOwnCash() > state.getOpponentCash() / 2 || state.getOwnCash() > state.getInitialCash() / 4);

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(AdaptiveStrategy.class, strategy, "Should be AdaptiveStrategy in critical rounds when not clearly cash rich for aggressive");
    }


    @Test
    void testDetermineStrategy_VeryLowCash_ReturnsConservativeStrategy() {
        // Goal:
        // 1. Target NOT achieved.
        // 2. Opponent CAN still mathematically win (or this condition isn't met for other reasons).
        // 3. NOT a critical round (remainingRounds > neededQuantityToWin).
        // 4. Very low cash IS true.

        // Setup state to ensure it's NOT a critical round:
        // TargetQ = (21/2) + 1 = 11.
        AuctionState state = new AuctionState(21, 100); // Initial Quantity = 21, Initial Cash = 100

        // Own 2 QU, so target not met.
        state.updateQuantities(2, 0); // OwnQW=2, OppQW=0, RemQ=19.

        // Set cash to be very low:
        state.updateCash(95, 0); // Own cash becomes 100 - 95 = 5.

        // Verify conditions for "Very Low Cash":
        // state.getOwnCash() < state.getInitialCash() * 0.1  => 5 < (100 * 0.1) => 5 < 10.0 is TRUE
        // state.getOwnCash() < 10                             => 5 < 10 is TRUE
        // Both are true, so the condition for ConservativeStrategy should be met.

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(ConservativeStrategy.class, strategy,
                "Should be ConservativeStrategy with very low cash and not in a critical round.");
    }

    @Test
    void testDetermineStrategy_Default_ReturnsAdaptiveStrategy() {
        // Goal:
        // 1. Target NOT achieved.
        // 2. Opponent CAN still mathematically win (or this condition isn't met).
        // 3. NOT a critical round.
        // 4. NOT very low cash.

        // Setup: InitialQ=30, Target=16. InitialCash=500.
        AuctionState state = new AuctionState(30, 500);

        // Own some QUs, but not target, and not making it critical.
        state.updateQuantities(5, 0); // OwnQW=5, OppQW=0, RemQ=25.

        BiddingStrategy strategy = BiddingStrategyProvider.determineStrategy(state);
        assertInstanceOf(AdaptiveStrategy.class, strategy,
                "Should default to AdaptiveStrategy when no other specific conditions are met.");
    }

}