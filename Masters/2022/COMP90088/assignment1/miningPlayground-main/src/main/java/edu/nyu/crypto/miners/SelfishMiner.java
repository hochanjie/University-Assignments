package edu.nyu.crypto.miners;

import edu.nyu.crypto.blockchain.Block;
import edu.nyu.crypto.blockchain.NetworkStatistics;

public class SelfishMiner extends CompliantMiner implements Miner {

	private Block currentHead, privateHead;

	public SelfishMiner(String id, int hashRate, int connectivity) {
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

			int blockHeight = block.getHeight();
			int currentHeight = currentHead.getHeight();
			int privateHeight = privateHead.getHeight();
			
			if (isMinerMe && (privateHead == null || (privateHeight < blockHeight))) {
				privateHead = block;
			} 
			else if (currentHeight < blockHeight) {

				if (privateHeight < blockHeight) {
					currentHead = privateHead = block;
				}
				else if (privateHeight - blockHeight <= 1) { 
					currentHead = privateHead;
				}
			}
		}
	}


	@Override
	public void initialize(Block genesis, NetworkStatistics networkStatistics) {
		currentHead = privateHead = genesis;
	}

	@Override
	public void networkUpdate(NetworkStatistics statistics) {
	}
}
