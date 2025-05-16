package org.kamran.strategy;


import org.kamran.auction.AuctionState;

/**
 * {@code ZeroBidStrategy} is used when the bidder has either clinched a majority of the quantity
 *  * or wishes to conserve all remaining cash
 */
public class ZeroBidStrategy implements BiddingStrategy {
    /**
     * @param state The current state of the auction.
     * @return {@code 0}, indicating no bid
     */
    @Override
    public int calculateBid(AuctionState state) {
        return 0;
    }

    @Override
    public String getStrategyName() {
        return "Zero bid";
    }

}
