package org.kamran.provider;

import org.kamran.auction.AuctionState;
import org.kamran.strategy.AdaptiveStrategy;
import org.kamran.strategy.AggressiveStrategy;
import org.kamran.strategy.BiddingStrategy;
import org.kamran.strategy.ConservativeStrategy;
import org.kamran.strategy.ZeroBidStrategy;

/**
 * {@code BiddingStrategyProvider} is a factory utility responsible for selecting the most suitable
 * {@link BiddingStrategy} based on the current {@link AuctionState}.
 * <p>
 * This enables dynamic strategy switching during the auction, allowing the bot to adapt to changing
 * game conditions such as trailing in quantity or having a cash advantage.
 * <p>
 */
public class BiddingStrategyProvider {

    private BiddingStrategyProvider() {
    }

    private static final BiddingStrategy zeroBidStrategy = new ZeroBidStrategy();
    private static final BiddingStrategy aggressiveStrategy = new AggressiveStrategy();
    private static final BiddingStrategy conservativeStrategy = new ConservativeStrategy();
    private static final BiddingStrategy adaptiveStrategy = new AdaptiveStrategy();

    /**
     * Determines and returns the appropriate bidding strategy based on the current auction state.
     * This method encapsulates the selection logic.
     *
     * @param state The current state of the auction.
     * @return {@link BiddingStrategy}
     */
    public static BiddingStrategy determineStrategy(AuctionState state) {
        if (state.getOwnQuantityWon() >= state.getTargetQuantity()) {
            return zeroBidStrategy;
        }

        int maxOpponentCanWinTotal = state.getOpponentQuantityWon() + state.getRemainingQuantity();
        if (maxOpponentCanWinTotal < state.getOwnQuantityWon() && state.getOwnQuantityWon() > 0) {
            return zeroBidStrategy;
        }

        int remainingRounds = state.getRemainingRounds();

        if (remainingRounds > 0 && remainingRounds <= state.getNeededQuantityToWin()) {
            if (state.getOwnCash() > state.getOpponentCash() / 2 || state.getOwnCash() > state.getInitialCash() / 4) {
                return aggressiveStrategy;
            } else {
                return adaptiveStrategy;
            }
        }

        if (state.getOwnCash() < state.getInitialCash() * 0.1 && state.getOwnCash() < 10) {
            return conservativeStrategy;
        }

        // Default strategy
        return adaptiveStrategy;
    }

}
