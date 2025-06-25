// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "./Auction.sol";

contract DutchAuction is Auction {

    uint public initialPrice;
    uint public biddingPeriod;
    uint public offerPriceDecrement;

    uint public startTime;
    uint public endTime;
    bool auctionComplete;

    address public contractAddress;

    // constructor
    constructor(address _sellerAddress,
                          address _judgeAddress,
                          address _timerAddress,
                          uint _initialPrice,
                          uint _biddingPeriod,
                          uint _offerPriceDecrement)
             Auction (_sellerAddress, _judgeAddress, _timerAddress) {

        initialPrice = _initialPrice;
        biddingPeriod = _biddingPeriod;
        offerPriceDecrement = _offerPriceDecrement;

        startTime = time();
        endTime = startTime + biddingPeriod;     
    }

    function bid() public payable{

        require(time() < endTime && auctionComplete == false);

        uint currentPrice = initialPrice - ((time() - startTime) * offerPriceDecrement);
        require(msg.value >= currentPrice);

        winnerAddress = msg.sender;
        auctionComplete = true;
        payable(msg.sender).transfer(address(this).balance - currentPrice);
    }
}
