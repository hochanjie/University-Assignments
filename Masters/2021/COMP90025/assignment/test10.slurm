#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:10:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test10
#SBATCH -o test10.out # STDOUT 
#SBATCH -e test10.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test10 p_dynamic_init.cpp
./test10 < mseq-big13-example.dat