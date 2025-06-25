#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:30:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=benchmark
#SBATCH -o benchmark.out # STDOUT 
#SBATCH -e benchmark.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o benchmark kseqalign.cpp
./benchmark < mseq-big13-example.dat