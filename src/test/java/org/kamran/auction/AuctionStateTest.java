package org.kamran.auction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class AuctionStateTest {

    private AuctionState state;
    private final int initialQuantity = 10;
    private final int initialCash = 100;

    @BeforeEach
    void setUp() {
        state = new AuctionState(initialQuantity, initialCash);
    }

    @Test
    void testInitialization() {
        assertEquals(initialQuantity, state.getRemainingQuantity());
        assertEquals(initialCash, state.getInitialCash());
        assertEquals(initialCash, state.getOwnCash());
        assertEquals(initialCash, state.getOpponentCash());
        assertEquals(0, state.getOwnQuantityWon());
        assertEquals(0, state.getOpponentQuantityWon());
        assertTrue(state.getOpponentBidsHistory().isEmpty());
        assertFalse(state.isAuctionOver());
    }

    @Test
    void testRecordOpponentBid() {
        state.recordOpponentBid(10);
        state.recordOpponentBid(20);
        List<Integer> history = state.getOpponentBidsHistory();
        assertEquals(2, history.size());
        assertEquals(10, history.get(0));
        assertEquals(20, history.get(1));
    }

    @Test
    void testUpdateCash() {
        state.updateCash(10, 15);
        assertEquals(initialCash - 10, state.getOwnCash());
        assertEquals(initialCash - 15, state.getOpponentCash());
    }

    @Test
    void testUpdateCash_NoNegativeCash() {
        state.updateCash(initialCash + 10, initialCash + 20);
        assertEquals(0, state.getOwnCash());
        assertEquals(0, state.getOpponentCash());
    }

    @Test
    void testUpdateQuantities_OwnWins() {
        state.updateQuantities(2, 0);
        assertEquals(2, state.getOwnQuantityWon());
        assertEquals(0, state.getOpponentQuantityWon());
        assertEquals(initialQuantity - 2, state.getRemainingQuantity());
    }

    @Test
    void testUpdateQuantities_OpponentWins() {
        state.updateQuantities(0, 2);
        assertEquals(0, state.getOwnQuantityWon());
        assertEquals(2, state.getOpponentQuantityWon());
        assertEquals(initialQuantity - 2, state.getRemainingQuantity());
    }

    @Test
    void testUpdateQuantities_Tie() {
        state.updateQuantities(1, 1);
        assertEquals(1, state.getOwnQuantityWon());
        assertEquals(1, state.getOpponentQuantityWon());
        assertEquals(initialQuantity - 2, state.getRemainingQuantity());
    }

    @Test
    void testUpdateQuantities_AuctionEnds() {
        state.updateQuantities(initialQuantity / 2, initialQuantity / 2); // Assuming even quantity
        assertEquals(0, state.getRemainingQuantity());
        assertTrue(state.isAuctionOver());
    }

     @Test
    void testUpdateQuantities_NoNegativeRemainingQuantity() {
        state.updateQuantities(initialQuantity, initialQuantity); // Bid more than available
        assertEquals(0, state.getRemainingQuantity());
        assertTrue(state.isAuctionOver());
    }


    @Test
    void testIsAuctionOver_True() {
        // Simulate auction ending by updating quantities until remaining is 0
        int rounds = initialQuantity / 2;
        for(int i=0; i<rounds; i++) {
            state.updateQuantities(1,1);
        }
        assertTrue(state.isAuctionOver());
    }

    @Test
    void testIsAuctionOver_False() {
        assertFalse(state.isAuctionOver());
    }

    @Test
    void testGetRemainingRounds() {
        assertEquals((initialQuantity + 1) / 2, state.getRemainingRounds());
        state.updateQuantities(1, 1); // 2 QU auctioned
        assertEquals((initialQuantity - 2 + 1) / 2, state.getRemainingRounds());
        AuctionState stateSingle = new AuctionState(1,100);
        assertEquals(1, stateSingle.getRemainingRounds());
        stateSingle.updateQuantities(1,0);
        assertEquals(0, stateSingle.getRemainingRounds());

    }

    @Test
    void testGetTargetQuantity() {
        assertEquals(initialQuantity / 2 + 1, state.getTargetQuantity());
        AuctionState stateOdd = new AuctionState(11, 100);
        assertEquals(11 / 2 + 1, stateOdd.getTargetQuantity());
    }

    @Test
    void testGetNeededQuantityToWin() {
        int target = initialQuantity / 2 + 1;
        assertEquals(target, state.getNeededQuantityToWin());
        state.updateQuantities(2, 0); // Won 2 QU
        assertEquals(target - 2, state.getNeededQuantityToWin());
    }
}