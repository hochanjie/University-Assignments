// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "./Auction.sol";

contract EnglishAuction is Auction {

    uint public initialPrice;
    uint public biddingPeriod;
    uint public minimumPriceIncrement;

    uint public balance;
    uint public bidDeadline;
    uint public currentPrice;
    uint public highestBid;
    address currentWinner;

    // constructor
    constructor(address _sellerAddress,
                          address _judgeAddress,
                          address _timerAddress,
                          uint _initialPrice,
                          uint _biddingPeriod,
                          uint _minimumPriceIncrement)
             Auction (_sellerAddress, _judgeAddress, _timerAddress) {

        initialPrice = _initialPrice;
        biddingPeriod = _biddingPeriod;
        minimumPriceIncrement = _minimumPriceIncrement;

        bidDeadline = time() + biddingPeriod;
        currentPrice = initialPrice - minimumPriceIncrement;
        balance = address(this).balance;
        english = true;
    }

    function bid() public payable{

        require(time() < bidDeadline);
        require(msg.value >= currentPrice + minimumPriceIncrement);

        currentPrice = msg.value;
        bidDeadline = time() + biddingPeriod;

        EnglishBids[currentWinner] += highestBid;
        highestBid = address(this).balance - balance;
        balance = address(this).balance;      
        currentWinner = msg.sender;
    }

    // Need to override the default implementation
    function getWinner() public override view returns (address winner){

        if(bidDeadline > time())
            return address(0);
        else
            return currentWinner;
    }
}
