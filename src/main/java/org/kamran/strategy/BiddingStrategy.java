package org.kamran.strategy;

import org.kamran.auction.AuctionState;

/**
 * {@code BiddingStrategy} defines the contract for implementing auction bidding strategies.
 * <p>
 * A bidding strategy determines how a bid is calculated based on the auction state and bidder
 * algorithm.
 * <p>
 *
 * @see AuctionState
 */
public interface BiddingStrategy {
    /**
     * Calculates the bid amount based on the current auction state.
     *
     * @param state The current state of the auction.
     * @return The bid amount.
     */
    int calculateBid(AuctionState state);

    /**
     * @return A descriptive name for the strategy.
     */
    String getStrategyName();
}
