#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:25:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=parallel
#SBATCH -o parallel.out
#SBATCH -e parallel.err

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o parallel parallel.cpp
./parallel < mseq-big13-example.dat