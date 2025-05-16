package org.kamran.auction;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kamran.provider.BiddingStrategyProvider;
import org.kamran.strategy.BiddingStrategy;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KamransBidderTest {

    private KamransBidder bidder;

    @BeforeEach
    void setUp() {
        bidder = new KamransBidder();
    }

    @Test
    void testInit_InitializesAuctionState() {
        bidder.init(10, 100);
        assertNotNull(bidder, "Bidder should be instantiated.");

        try (MockedStatic<BiddingStrategyProvider> mockedProvider = Mockito.mockStatic(BiddingStrategyProvider.class)) {
            BiddingStrategy mockStrategy = mock(BiddingStrategy.class);
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(0); // Return a safe bid
            when(mockStrategy.getStrategyName()).thenReturn("InitialMockStrategy");
            mockedProvider.when(() -> BiddingStrategyProvider.determineStrategy(any(AuctionState.class)))
                    .thenReturn(mockStrategy);
            assertDoesNotThrow(() -> bidder.placeBid(), "placeBid should not throw after init");
        }
    }

    @Test
    void testPlaceBid_AuctionIsOver_ReturnsZero() {
        bidder.init(0, 100); // Initial quantity is 0, so auction is immediately over.
        int bid = bidder.placeBid();
        assertEquals(0, bid, "Bid should be 0 if the auction is over.");
    }

    /**
     * This test demonstrates the recommended use of mocks to test KamransBidder.placeBid() in
     * isolation.
     * <p>
     * - It mocks the static BiddingStrategyProvider.determineStrategy() to control which strategy
     * is "selected".
     * - The "selected" strategy is also a mock, allowing us to control the bid
     * amount it calculates.
     * - This setup lets us verify KamransBidder's logic for clamping the bid
     * to [0, ownCash].
     * </p>
     */
    @Test
    void testPlaceBid_DelegatesToStrategyAndClampsBidCorrectly() {
        bidder.init(10, 100);

        // 1. Create a mock BiddingStrategy
        BiddingStrategy mockStrategy = mock(BiddingStrategy.class);
        when(mockStrategy.getStrategyName()).thenReturn("TestMockStrategy");

        // 2. Use try-with-resources for mocking the static BiddingStrategyProvider
        try (MockedStatic<BiddingStrategyProvider> mockedProvider = Mockito.mockStatic(BiddingStrategyProvider.class)) {
            // 3. Stub BiddingStrategyProvider.determineStrategy() to return our mockStrategy
            mockedProvider.when(() -> BiddingStrategyProvider.determineStrategy(any(AuctionState.class)))
                    .thenReturn(mockStrategy);

            // Scenario A: Strategy calculates a valid bid within cash limits
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(50);
            int bidA = bidder.placeBid();
            assertEquals(50, bidA, "Bidder should return the strategy's valid bid.");

            // Scenario B: Strategy calculates a bid exceeding available cash
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(150); // More than ownCash (100)
            int bidB = bidder.placeBid();
            assertEquals(100, bidB, "Bid should be clamped to available cash (100).");

            // Scenario C: Strategy calculates a negative bid
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(-10);
            int bidC = bidder.placeBid();
            assertEquals(0, bidC, "Bid should be clamped to 0 if strategy returns negative.");

            // Scenario D: Strategy calculates a bid of 0
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(0);
            int bidD = bidder.placeBid();
            assertEquals(0, bidD, "Bidder should return 0 if strategy calculates 0.");

            // Scenario E: Strategy calculates a bid equal to available cash
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(100);
            int bidE = bidder.placeBid();
            assertEquals(100, bidE, "Bidder should allow bidding full available cash.");
        }
    }

    /**
     *  This test primarily checks that bids() executes and updates state influencing subsequent bids.
     */
    @Test
    void testBids_OwnWins_AuctionStateUpdated() {
        bidder.init(10, 100);
        int ownBid = 20;
        int otherBid = 10;

        bidder.bids(ownBid, otherBid);

        // Perform a subsequent bid to see if cash was reduced (influencing clamping)
        try (MockedStatic<BiddingStrategyProvider> mockedProvider = Mockito.mockStatic(BiddingStrategyProvider.class)) {
            BiddingStrategy mockStrategy = mock(BiddingStrategy.class);
            when(mockStrategy.getStrategyName()).thenReturn("PostWinStrategy");
            // Strategy suggests a bid higher than remaining cash after paying the first bid
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(90);
            mockedProvider.when(() -> BiddingStrategyProvider.determineStrategy(any(AuctionState.class)))
                    .thenReturn(mockStrategy);

            int nextBid = bidder.placeBid();
            assertEquals(80, nextBid, "Next bid should be clamped by cash remaining after winning previous round.");
        }
    }

    @Test
    void testBids_OpponentWins_AuctionStateUpdated() {
        bidder.init(10, 100);
        int ownBid = 10;
        int otherBid = 20;

        bidder.bids(ownBid, otherBid);

        try (MockedStatic<BiddingStrategyProvider> mockedProvider = Mockito.mockStatic(BiddingStrategyProvider.class)) {
            BiddingStrategy mockStrategy = mock(BiddingStrategy.class);
            when(mockStrategy.getStrategyName()).thenReturn("PostLossStrategy");
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(100);
            mockedProvider.when(() -> BiddingStrategyProvider.determineStrategy(any(AuctionState.class)))
                    .thenReturn(mockStrategy);

            int nextBid = bidder.placeBid();
            assertEquals(90, nextBid, "Next bid should be clamped by cash remaining after losing previous round.");
        }
    }

    @Test
    void testBids_Tie_AuctionStateUpdated() {
        bidder.init(10, 100);
        int ownBid = 15;
        int otherBid = 15;

        bidder.bids(ownBid, otherBid);

        try (MockedStatic<BiddingStrategyProvider> mockedProvider = Mockito.mockStatic(BiddingStrategyProvider.class)) {
            BiddingStrategy mockStrategy = mock(BiddingStrategy.class);
            when(mockStrategy.getStrategyName()).thenReturn("PostTieStrategy");
            when(mockStrategy.calculateBid(any(AuctionState.class))).thenReturn(90);
            mockedProvider.when(() -> BiddingStrategyProvider.determineStrategy(any(AuctionState.class)))
                    .thenReturn(mockStrategy);

            int nextBid = bidder.placeBid();
            assertEquals(85, nextBid, "Next bid should be clamped by cash remaining after a tie.");
        }
    }

    @Test
    void testBids_AuctionEnds_FurtherBidsAreZero() {
        bidder.init(2, 100); // Only 2 QU in total, one round
        bidder.bids(10, 5);  // Own wins 2 QU, auction ends.

        assertEquals(0, bidder.placeBid(), "Bid should be 0 after auction ends.");
    }


}