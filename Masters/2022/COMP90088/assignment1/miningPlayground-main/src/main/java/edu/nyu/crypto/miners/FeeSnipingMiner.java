package edu.nyu.crypto.miners;

import edu.nyu.crypto.blockchain.Block;
import edu.nyu.crypto.blockchain.NetworkStatistics;

public class FeeSnipingMiner extends CompliantMiner implements Miner {

    private Block currentHead, privateHead;

    public FeeSnipingMiner(String id, int hashRate, int connectivity) {
        super(id, hashRate, connectivity);

    }

    @Override
    public Block currentlyMiningAt() {
        return privateHead;
    }

    @Override
    public Block currentHead() {
        return currentHead;
    }

    @Override
    public void blockMined(Block block, boolean isMinerMe) {

        if (block != null) {

            if (isMinerMe) {
                currentHead = privateHead = block;
            } 
            else if (currentHead.getHeight() < block.getHeight()) { 

                currentHead = block;

                if (privateHead.getHeight() <= block.getHeight()) {
                    privateHead = block;
                }
                if (block.getBlockValue() >= Math.pow(0.2, 2)*(1+getHashRate())) { 
                    privateHead = block.getPreviousBlock();
                }
            }
        }
    }


    @Override
    public void initialize(Block genesis, NetworkStatistics statistics) {
        currentHead = privateHead = genesis;
    }

    @Override
    public void networkUpdate(NetworkStatistics statistics) {
    }

}
