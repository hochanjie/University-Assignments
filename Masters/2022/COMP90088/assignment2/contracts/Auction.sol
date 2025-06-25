// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "./Timer.sol";

contract Auction {

    address internal judgeAddress;
    address internal timerAddress;
    address internal sellerAddress;
    address internal winnerAddress;
    uint winningPrice;

    bool refund_call = false;
    bool finalised_call;
    uint refund_amount;
    uint finalised_amount;

    uint bidDeposit;
    bool english = false;
    bool vickrey = false;
    
    mapping(address=>uint) EnglishBids;
    mapping(address=>uint) public bids;
    mapping(address=>bytes32) public commitments;

    // constructor
    constructor(address _sellerAddress,
                     address _judgeAddress,
                     address _timerAddress) {

        judgeAddress = _judgeAddress;
        timerAddress = _timerAddress;
        sellerAddress = _sellerAddress;
        if (sellerAddress == address(0))
          sellerAddress = msg.sender;
    }

    // This is provided for testing
    // You should use this instead of block.number directly
    // You should not modify this function.
    function time() public view returns (uint) {
        if (timerAddress != address(0))
            return Timer(timerAddress).getTime();

        return block.number;
    }

    function getWinner() public view virtual returns (address winner) {
        return winnerAddress;
    }

    function getWinningPrice() public view returns (uint price) {
        return winningPrice;
    }

    // If no judge is specified, anybody can call this.
    // If a judge is specified, then only the judge or winning bidder may call.
    function finalize() public virtual {

        require(address(judgeAddress) == address(0) || msg.sender == getWinner() || msg.sender == judgeAddress);
        require(getWinner() != address(0));

        finalised_call = true;
        finalised_amount = address(this).balance;
        withdraw();
    }

    // This can ONLY be called by seller or the judge (if a judge exists).
    // Money should only be refunded to the winner.
    function refund() public {

        require(msg.sender == judgeAddress || msg.sender == sellerAddress);
        require(getWinner() != address(0));

        refund_call = true;
        refund_amount = address(this).balance;
        withdraw();
    }

    // Withdraw funds from the contract.
    // If called, all funds available to the caller should be refunded.
    // This should be the *only* place the contract ever transfers funds out.
    // Ensure that your withdrawal functionality is not vulnerable to
    // re-entrancy or unchecked-spend vulnerabilities.
    function withdraw() public {

        if (english) {

            payable(msg.sender).transfer(EnglishBids[msg.sender]);
            EnglishBids[msg.sender] = 0;

        }
        else if (vickrey) {

            payable(msg.sender).transfer(bidDeposit + bids[msg.sender]); 
            delete bids[msg.sender];
            
        }
        else {

            if (refund_call)
                payable(winnerAddress).transfer(refund_amount);
                refund_amount = 0;
                
            if (finalised_call)
                payable(sellerAddress).transfer(finalised_amount);
                finalised_amount = 0;
        }
    }
}
