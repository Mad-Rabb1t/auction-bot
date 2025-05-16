package org.kamran.strategy;

import org.kamran.auction.AuctionState;

import java.util.Random;

/**
 * {@code AggressiveStrategy} prioritizes winning quantity, even at the cost of higher spending.
 * <p>
 * <p>
 * Bids significantly higher than estimated opponent to secure quantity.
 *
 * @see BiddingStrategy
 * @see AuctionState
 */
public class AggressiveStrategy implements BiddingStrategy {

    private final Random random = new Random();

    @Override
    public int calculateBid(AuctionState state) {
        if (state.getOwnCash() == 0) return 0;

        int estimatedOpponentBid;
        if (!state.getOpponentBidsHistory().isEmpty()) {
            estimatedOpponentBid = (int) state.getOpponentBidsHistory().stream()
                    .mapToInt(Integer::intValue)
                    .average().orElse(0.0);
        } else {
            estimatedOpponentBid = state.getOpponentCash() / Math.max(1, state.getRemainingRounds()) / 2;
        }
        estimatedOpponentBid = Math.min(estimatedOpponentBid, state.getOpponentCash()); // Cannot bid more than they have

        int bid = estimatedOpponentBid + 1 + random.nextInt(3); // Bid slightly more + small random factor

        // If very few rounds left and need quantity, be more aggressive
        if (state.getRemainingRounds() <= 2 && state.getNeededQuantityToWin() > 0) {
            bid = Math.max(bid, state.getOpponentCash() / Math.max(1, state.getRemainingRounds()) + 1);
            bid = Math.max(bid, state.getOwnCash() / Math.max(1, state.getRemainingRounds())); // Spend more if needed
        }


        return Math.clamp(bid, 0, state.getOwnCash()); // Ensure bid is within own cash and non-negative
    }

    @Override
    public String getStrategyName() {
        return "Aggressive";
    }
}
