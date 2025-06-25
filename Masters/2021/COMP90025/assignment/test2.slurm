#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:25:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test2
#SBATCH -o test2.out # STDOUT 
#SBATCH -e test2.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test2 p_penalty.cpp
./test2 < mseq-big13-example.dat