#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:25:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test4
#SBATCH -o test4.out # STDOUT 
#SBATCH -e test4.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test4 p_guided_300.cpp
./test4 < mseq-big13-example.dat