package org.kamran.auction;

import org.kamran.provider.BiddingStrategyProvider;
import org.kamran.strategy.BiddingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code AuctionState} represents the current state of the auction. This object is passed to
 * bidding strategies to help them make decisions.
 *
 * @see BiddingStrategy
 * @see BiddingStrategyProvider
 */
public class AuctionState {
    private final int initialQuantity;
    private int remainingQuantity;
    private final int initialCash;
    private int ownCash;
    private int opponentCash;
    private int ownQuantityWon;
    private int opponentQuantityWon;
    private final List<Integer> opponentBidsHistory; // To observe opponent's behavior

    public AuctionState(int quantity, int cash) {
        this.initialQuantity = quantity;
        this.remainingQuantity = quantity;
        this.initialCash = cash;
        this.ownCash = cash;
        this.opponentCash = cash;
        this.ownQuantityWon = 0;
        this.opponentQuantityWon = 0;
        this.opponentBidsHistory = new ArrayList<>();
    }

    // --- Getters ---

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getInitialCash() {
        return initialCash;
    }

    public int getOwnCash() {
        return ownCash;
    }

    public int getOpponentCash() {
        return opponentCash;
    }

    public int getOwnQuantityWon() {
        return ownQuantityWon;
    }

    public int getOpponentQuantityWon() {
        return opponentQuantityWon;
    }

    public List<Integer> getOpponentBidsHistory() {
        return new ArrayList<>(opponentBidsHistory);
    }

    public int getRemainingRounds() {
        return (remainingQuantity + 1) / 2; // Each round auctions 2 QU
    }

    public int getTargetQuantity() {
        return (initialQuantity / 2) + 1;
    }

    public int getNeededQuantityToWin() {
        return getTargetQuantity() - ownQuantityWon;
    }

    public void recordOpponentBid(int bid) {
        this.opponentBidsHistory.add(bid);
    }

    public void updateCash(int ownBidPaid, int opponentBidPaid) {
        this.ownCash -= ownBidPaid;
        this.opponentCash -= opponentBidPaid;
        this.ownCash = Math.max(0, this.ownCash); // Ensure non-negative
        this.opponentCash = Math.max(0, this.opponentCash); // Ensure non-negative
    }

    public void updateQuantities(int ownWonThisRound, int opponentWonThisRound) {
        this.ownQuantityWon += ownWonThisRound;
        this.opponentQuantityWon += opponentWonThisRound;
        this.remainingQuantity -= (ownWonThisRound + opponentWonThisRound);
        this.remainingQuantity = Math.max(0, this.remainingQuantity); // Ensure non-negative
    }

    public boolean isAuctionOver() {
        return remainingQuantity <= 0;
    }

    @Override
    public String toString() {
        return "AuctionState{" +
               "remQU=" + remainingQuantity +
               ", ownMU=" + ownCash +
               ", oppMU=" + opponentCash +
               ", ownQuWon=" + ownQuantityWon +
               ", oppQuWon=" + opponentQuantityWon +
               ", roundsLeft=" + getRemainingRounds() +
               '}';
    }
}
