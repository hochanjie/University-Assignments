package edu.nyu.crypto.miners;

import edu.nyu.crypto.blockchain.Block;
import edu.nyu.crypto.blockchain.NetworkStatistics;

public class MajorityMiner extends CompliantMiner implements Miner {

    
    private Block currentHead;
    private double networkHashRate;

    public MajorityMiner(String id, int hashRate, int connectivity) {
        super(id, hashRate, connectivity);
        
    }

    @Override
    public Block currentlyMiningAt(){
        return currentHead;
    }

    @Override
    public Block currentHead(){
        return currentHead;
    }

    @Override
    public void blockMined(Block block, boolean isMinerMe){

        if ((block != null) && block.getHeight() > currentHead.getHeight() &&
        (isMinerMe || (this.getHashRate()/networkHashRate) < 0.51)) {

            currentHead = block;
            
        }
    }

    @Override
    public void initialize(Block genesis, NetworkStatistics statistics){
        currentHead = genesis;
        networkHashRate = statistics.getTotalHashRate();
    }
    
    @Override
    public void networkUpdate(NetworkStatistics statistics){
        networkHashRate = statistics.getTotalHashRate();
    }

    
}

