// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "./Auction.sol";

contract VickreyAuction is Auction {

    uint public minimumPrice;
    uint public biddingDeadline;
    uint public revealDeadline;
    uint public bidDepositAmount;

    uint balance;
    uint highestBid;
    uint secondHighest;
    address currentWinner;

    // constructor
    constructor(address _sellerAddress,
                            address _judgeAddress,
                            address _timerAddress,
                            uint _minimumPrice,
                            uint _biddingPeriod,
                            uint _revealPeriod,
                            uint _bidDepositAmount)
             Auction (_sellerAddress, _judgeAddress, _timerAddress) {

        minimumPrice = _minimumPrice;
        bidDepositAmount = _bidDepositAmount;
        biddingDeadline = time() + _biddingPeriod;
        revealDeadline = time() + _biddingPeriod + _revealPeriod;

        secondHighest = minimumPrice;
        vickrey = true;
    }

    // Record the player's bid commitment
    // Make sure exactly bidDepositAmount is provided (for new commitments)
    // Bidders can update their previous bid for free if desired.
    // Only allow commitments before biddingDeadline
    function commitBid(bytes32 bidCommitment) public payable {

        require(time() < biddingDeadline);

        uint balanceCheck = (commitments[msg.sender] != 0) ? balance : balance + bidDepositAmount;
        require(address(this).balance == balanceCheck); 

        commitments[msg.sender] = bidCommitment;
        balance = address(this).balance;
    }

    // Check that the bid (msg.value) matches the commitment.
    // If the bid is correctly opened, the bidder can withdraw their deposit.
    function revealBid(bytes32 nonce) public payable{

        require(time() >= biddingDeadline && time() < revealDeadline);
        require(keccak256(abi.encodePacked(msg.value, nonce)) == commitments[msg.sender]);
        
		uint bid = address(this).balance - balance;
        bids[msg.sender] = bid;

        if (bid > highestBid) {

            if (highestBid != 0) 
                secondHighest = highestBid;

            highestBid = bid;
            currentWinner = msg.sender;

        }
        else if (bid > secondHighest) {
          secondHighest = bid;
        }
        
        balance = address(this).balance;
        delete commitments[msg.sender];
    }

    // Need to override the default implementation
    function getWinner() public override view returns (address winner){

        require(time() >= revealDeadline);
        return currentWinner;
    }

    // finalize() must be extended here to provide a refund to the winner
    // based on the final sale price (the second highest bid, or reserve price).
    function finalize() public override {

        require(time() >= revealDeadline);
        bids[currentWinner] = highestBid - secondHighest;
        bidDeposit = bidDepositAmount;
    }
}
