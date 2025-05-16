package org.kamran.auction;

import org.kamran.provider.BiddingStrategyProvider;
import org.kamran.strategy.BiddingStrategy;

/**
 * {@code KamransBidder} is an implementation of the {@link Bidder} interface for auction where two
 * parties bid for units of a product using limited monetary resources.
 * <p>
 * This bot utilizes an {@link AuctionState} object to maintain internal state, including:
 * <ul>
 *   <li>Total and remaining quantity</li>
 *   <li>Available cash for both bidders</li>
 *   <li>Opponent's bidding history</li>
 *   <li>Quantity won by each party</li>
 * </ul>
 * <p>
 * For each round of bidding, {@code KamransBidder} consults a dynamically chosen {@link BiddingStrategy}
 * via the {@link BiddingStrategyProvider} to determine the most appropriate bidding logic
 * based on the current state of the auction.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Logging of internal state and decisions for debugging and transparency</li>
 *   <li>Post-round state updates based on the outcome of each auction cycle</li>
 *   <li>End-of-auction outcome evaluation for winner determination</li>
 * </ul>
 * <p>
 * The logic ensures that:
 * <ul>
 *   <li>Bids do not exceed the available budget</li>
 *   <li>Bids are never negative</li>
 *   <li>Strategic switching can occur based on the real-time dynamics</li>
 * </ul>
 * <p>
 * {@link BiddingStrategyProvider} can be customized to support more {@link BiddingStrategy} implementations if required.
 *
 * @see AuctionState
 * @see BiddingStrategy
 * @see BiddingStrategyProvider
 */
public class KamransBidder implements Bidder {

    private AuctionState auctionState;

    @Override
    public void init(int quantity, int cash) {
        this.auctionState = new AuctionState(quantity, cash);
    }

    @Override
    public int placeBid() {
        if (auctionState.isAuctionOver()) {
            return 0;
        }

        BiddingStrategy currentStrategy = BiddingStrategyProvider.determineStrategy(this.auctionState);

        System.out.println("Current State: " + auctionState.toString());
        System.out.println("Using Strategy: " + currentStrategy.getStrategyName());

        int bid = Math.clamp(currentStrategy.calculateBid(auctionState), 0, auctionState.getOwnCash());

        System.out.println("Bot places bid: " + bid);
        return bid;
    }

    @Override
    public void bids(int ownBid, int otherBid) {
        System.out.println("Round Result - Own Bid: " + ownBid + ", Opponent Bid: " + otherBid);
        auctionState.updateCash(ownBid, otherBid);
        auctionState.recordOpponentBid(otherBid);

        int ownWonThisRound = 0;
        int opponentWonThisRound = 0;

        if (ownBid > otherBid) {
            ownWonThisRound = 2;
            System.out.println("Result: Won " + ownWonThisRound + " QU.");
        } else if (otherBid > ownBid) {
            opponentWonThisRound = 2;
            System.out.println("Result: Lost, opponent won " + opponentWonThisRound + " QU.");
        } else {
            // Tie
            ownWonThisRound = 1;
            opponentWonThisRound = 1;
            System.out.println("Result: Tied. Own won " + ownWonThisRound + " QU, Opponent won " + opponentWonThisRound + " QU.");
        }

        auctionState.updateQuantities(ownWonThisRound, opponentWonThisRound);
        System.out.println("State After Round: " + auctionState.toString());
        System.out.println("-----------------------------------------------");
        if (auctionState.isAuctionOver()) {
            printFinalOutcome();
        }
    }

    private void printFinalOutcome() {
        System.out.println("Auction Finished!");
        System.out.println("Final Score - Own QU: " + auctionState.getOwnQuantityWon() + " (Cash Left: " + auctionState.getOwnCash() +
                           "), Opponent QU: " + auctionState.getOpponentQuantityWon() + " (Cash Left: " + auctionState.getOpponentCash() + ")");
        if (auctionState.getOwnQuantityWon() > auctionState.getOpponentQuantityWon()) {
            System.out.println("Outcome: Won on Quantity.");
        } else if (auctionState.getOpponentQuantityWon() > auctionState.getOwnQuantityWon()) {
            System.out.println("Outcome: Lost on Quantity.");
        } else {
            if (auctionState.getOwnCash() > auctionState.getOpponentCash()) {
                System.out.println("Outcome: Tied on Quantity, Won on Cash.");
            } else if (auctionState.getOpponentCash() > auctionState.getOwnCash()) {
                System.out.println("Outcome: Tied on Quantity, Lost on Cash.");
            } else {
                System.out.println("Outcome: Exact Tie on Quantity and Cash.");
            }
        }
    }
}