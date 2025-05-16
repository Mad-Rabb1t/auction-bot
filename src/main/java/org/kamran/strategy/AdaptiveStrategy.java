package org.kamran.strategy;

import org.kamran.auction.AuctionState;

import java.util.Random;

/**
 * {@code AdaptiveStrategy} dynamically adjusts its bidding behavior based on the current state in
 * the auction.
 * <p>
 * Key behaviors:
 * <ul>
 *   <li>Increases bid slightly if trailing in quantity and has more cash</li>
 *   <li>Decreases bid slightly if leading, to conserve cash</li>
 *   <li>Maintains a balanced bid otherwise</li>
 * </ul>
 * <p>
 * This strategy attempts to balance risk and reward by reacting to both current score and
 * the opponent’s estimated behavior based on previous bids.
 *
 * @see BiddingStrategy
 * @see AuctionState
 */
public class AdaptiveStrategy implements BiddingStrategy {

    private final Random random = new Random();

    @Override
    public int calculateBid(AuctionState state) {
        if (state.getOwnCash() == 0) return 0;
        if (state.getRemainingRounds() <= 0) return 0;

        int neededQU = state.getNeededQuantityToWin();
        int remainingRounds = state.getRemainingRounds();

        // Basic opponent modeling
        double avgOpponentBid = state.getOpponentBidsHistory().stream()
                .mapToInt(Integer::intValue)
                .average().orElseGet(() -> (double) state.getOpponentCash() / Math.max(1, remainingRounds));
        int estimatedOpponentBid = (int) Math.ceil(avgOpponentBid);
        estimatedOpponentBid = Math.min(estimatedOpponentBid, state.getOpponentCash()); // Cap at opponent's current cash

        int bid;

        // If critically need quantity and few rounds left, bid more aggressively
        if (remainingRounds <= neededQU && remainingRounds > 0) {
            // Try to win this round, bid above estimated opponent bid
            bid = estimatedOpponentBid + 1 + random.nextInt(state.getOwnCash() / Math.max(1, remainingRounds * 2) + 1);
            bid = Math.min(bid, state.getOwnCash() / remainingRounds); // Don't blow entire budget in one go if multiple critical rounds
            bid = Math.max(1, bid); // Must bid at least 1 if trying to win
            System.out.println("Adaptive: Critical round, bidding: " + bid);
        } else {
            // Standard bid: Aim for a calculated value, potentially slightly above average opponent bid
            // Or bid a fraction of what we can afford per round.
            int affordableBid = state.getOwnCash() / Math.max(1, remainingRounds);
            bid = Math.min(affordableBid / 2, estimatedOpponentBid + 1 + random.nextInt(3));
            System.out.println("Adaptive: Standard round, bidding: " + bid);
        }

        return Math.clamp(bid, 0, state.getOwnCash());

    }

    @Override
    public String getStrategyName() {
        return "Adaptive";
    }
}
