package org.kamran.strategy;

import org.kamran.auction.AuctionState;

import java.util.Random;

/**
 * {@code ConservativeStrategy} focuses on preserving cash and bidding cautiously.
 * <p>
 * <p>
 * Bids a small percentage of remaining cash, or a small fixed value. Ideal for defensive play and
 * long-term sustainability.
 *
 * @see BiddingStrategy
 * @see AuctionState
 */
public class ConservativeStrategy implements BiddingStrategy {

    private final Random random = new Random();

    @Override
    public int calculateBid(AuctionState state) {
        if (state.getOwnCash() == 0) return 0;
        int bid = Math.min(state.getOwnCash() / 10, 5 + random.nextInt(5));
        return Math.clamp(bid, 0, state.getOwnCash());
    }

    @Override
    public String getStrategyName() {
        return "Conservative";
    }
}
