// CPP program to solve the sequence alignment
// problem. Adapted from https://www.geeksforgeeks.org/sequence-alignment-problem/ 
#include <sys/time.h>
#include <string>
#include <cstring>
#include <iostream>
#include "sha512.hh"

using namespace std;

std::string getMinimumPenalties(std::string *genes, int k, int pxy, int pgap, int *penalties);
int getMinimumPenalty(std::string x, std::string y, int pxy, int pgap, int *xans, int *yans);

/*
Examples of sha512 which returns a std::string
sw::sha512::calculate("SHA512 of std::string") // hash of a string, or
sw::sha512::file(path) // hash of a file specified by its path, or
sw::sha512::calculate(&data, sizeof(data)) // hash of any block of data
*/

// Return current wallclock time, for performance measurement
uint64_t GetTimeStamp() {
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec*(uint64_t)1000000+tv.tv_usec;
}

int main(int argc, char **argv){
	int misMatchPenalty;
	int gapPenalty;
	int k;
	std::cin >> misMatchPenalty;
	std::cin >> gapPenalty;
	std::cin >> k;	
	std::string genes[k];
	for(int i=0;i<k;i++) std::cin >> genes[i];

	int numPairs= k*(k-1)/2;

	int penalties[numPairs];
		
	uint64_t start = GetTimeStamp ();

	// return all the penalties and the hash of all allignments
	std::string alignmentHash = getMinimumPenalties(genes,
		k,misMatchPenalty, gapPenalty,
		penalties);
		
	// print the time taken to do the computation
	printf("Time: %ld us\n", (uint64_t) (GetTimeStamp() - start));
		
	// print the alginment hash
	std::cout<<alignmentHash<<std::endl;

	for(int i=0;i<numPairs;i++){
		std::cout<<penalties[i] << " ";
	}
	std::cout << std::endl;
	return 0;
}

int min3(int a, int b, int c) {
	if (a <= b && a <= c) {
		return a;
	} else if (b <= a && b <= c) {
		return b;
	} else {
		return c;
	}
}

// equivalent of  int *dp[width] = new int[height][width]
// but works for width not known at compile time.
// (Delete structure by  delete[] dp[0]; delete[] dp;)
int **new2d (int width, int height)
{
	int **dp = new int *[width];
	size_t size = width;
	size *= height;
	int *dp0 = new int [size];
	if (!dp || !dp0)
	{
	    std::cerr << "getMinimumPenalty: new failed" << std::endl;
	    exit(1);
	}
	dp[0] = dp0;
	for (int i = 1; i < width; i++)
	    dp[i] = dp[i-1] + height;

	return dp;
}

std::string getMinimumPenalties(std::string *genes, int k, int pxy, int pgap,
	int *penalties)
{
	int probNum=0;
	std::string alignmentHash="";
	for(int i=1;i<k;i++){
		for(int j=0;j<i;j++){
			std::string gene1 = genes[i];
			std::string gene2 = genes[j];
			int m = gene1.length(); // length of gene1
			int n = gene2.length(); // length of gene2
			int l = m+n;
			int xans[l+1], yans[l+1];
			penalties[probNum]=getMinimumPenalty(gene1,gene2,pxy,pgap,xans,yans);
			// Since we have assumed the answer to be n+m long,
			// we need to remove the extra gaps in the starting
			// id represents the index from which the arrays
			// xans, yans are useful
			int id = 1;
			int a;
			for (a = l; a >= 1; a--)
			{
				if ((char)yans[a] == '_' && (char)xans[a] == '_')
				{
					id = a + 1;
					break;
				}
			}
			std::string align1="";
			std::string align2="";
			for (a = id; a <= l; a++)
			{
				align1.append(1,(char)xans[a]);
			}
			for (a = id; a <= l; a++)
			{
				align2.append(1,(char)yans[a]);
			}
			std::string align1hash = sw::sha512::calculate(align1);
			std::string align2hash = sw::sha512::calculate(align2);
			std::string problemhash = sw::sha512::calculate(align1hash.append(align2hash));
			alignmentHash=sw::sha512::calculate(alignmentHash.append(problemhash));
			
			// Uncomment for testing purposes
			//  std::cout << penalties[probNum] << std::endl;
			//  std::cout << align1 << std::endl;
			//  std::cout << align2 << std::endl;
			//  std::cout << std::endl;

			probNum++;
		}
	}
	return alignmentHash;
}

// function to find out the minimum penalty
// return the minimum penalty and put the aligned sequences in xans and yans
int getMinimumPenalty(std::string x, std::string y, int pxy, int pgap, int *xans, int *yans)
{
	
	int i, j; // intialising variables

	int m = x.length(); // length of gene1
	int n = y.length(); // length of gene2
	
	// table for storing optimal substructure answers
	int **dp = new2d (m+1, n+1);
	size_t size = m + 1;
	size *= n + 1;
	memset (dp[0], 0, size);

	// intialising the table
	for (i = 0; i <= m; i++)
	{
		dp[i][0] = i * pgap;
	}
	for (i = 0; i <= n; i++)
	{
		dp[0][i] = i * pgap;
	}

	// calcuting the minimum penalty
	for (i = 1; i <= m; i++)
	{
		for (j = 1; j <= n; j++)
		{
			if (x[i - 1] == y[j - 1])
			{
				dp[i][j] = dp[i - 1][j - 1];
			}
			else
			{
				dp[i][j] = min3(dp[i - 1][j - 1] + pxy ,
						dp[i - 1][j] + pgap ,
						dp[i][j - 1] + pgap);
			}
		}
	}

	// Reconstructing the solution
	int l = n + m; // maximum possible length
	
	i = m; j = n;
	
	int xpos = l;
	int ypos = l;
	
	while ( !(i == 0 || j == 0))
	{
		if (x[i - 1] == y[j - 1])
		{
			xans[xpos--] = (int)x[i - 1];
			yans[ypos--] = (int)y[j - 1];
			i--; j--;
		}
		else if (dp[i - 1][j - 1] + pxy == dp[i][j])
		{
			xans[xpos--] = (int)x[i - 1];
			yans[ypos--] = (int)y[j - 1];
			i--; j--;
		}
		else if (dp[i - 1][j] + pgap == dp[i][j])
		{
			xans[xpos--] = (int)x[i - 1];
			yans[ypos--] = (int)'_';
			i--;
		}
		else if (dp[i][j - 1] + pgap == dp[i][j])
		{
			xans[xpos--] = (int)'_';
			yans[ypos--] = (int)y[j - 1];
			j--;
		}
	}
	while (xpos > 0)
	{
		if (i > 0) xans[xpos--] = (int)x[--i];
		else xans[xpos--] = (int)'_';
	}
	while (ypos > 0)
	{
		if (j > 0) yans[ypos--] = (int)y[--j];
		else yans[ypos--] = (int)'_';
	}

	int ret = dp[m][n];

	delete[] dp[0];
	delete[] dp;
	
	return ret;
}
