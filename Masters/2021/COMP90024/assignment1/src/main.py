#!/usr/bin/python
# -*- coding: utf-8 -*-
'''
Title: COMP90024 Cluster and cloud computing : Assignment 1

Author(s): Chan Jie Ho (961948), Chaoyin Chen (1225100)

Date: 11.04.2021

Description: the parallel implementation for evaluation tweets score in differnet regions

'''

from mpi4py import MPI
import json
import time
import sys

startTime = time.time()

# Get the rank of node and the size of the mpi universe
mpi = MPI.COMM_WORLD
rank = mpi.Get_rank()
size = mpi.Get_size()

gridFile, AFINN, twitterFile = sys.argv[1:]

# Load and preprocess data that is to be broadcasted on the master node ot the slave nodes so that we only need to
# process this once
if rank == 0:
    # Data on the grid coordinates
    with open(gridFile, encoding='utf-8') as f:
        gridData = json.load(f)

    f.close()

    # Remove unnecessary information and insert coordinates for each cell into a dictionary with the following format:
    # {A1: [xmin, xmax, ymin, ymax], A2: […], …}
    grid = {}
    for feature in gridData["features"]:
        properties = feature["properties"]
        grid[properties["id"]] = [properties["xmin"], properties["xmax"], properties["ymin"], properties["ymax"]]

    # Sentiment scores
    scoreDict = {}
    multiWordsDict = {}
    longestKey = 0

    with open(AFINN, encoding="utf-8") as f:
        for line in f:
            words = line.split()
            score = words[-1]
            word = ' '.join(words[:-1])
            if ' ' in word:
                multiWordsDict[word] = int(score)
                longestKey = max(longestKey, word.count(' ') + 1)
            else:
                scoreDict[word] = int(score)
    f.close()

else:
    grid = None
    scoreDict = None
    multiWordsDict = None

# Broadcast the dictionaries to the slave nodes so that they don't have to process the files themselves
grid = mpi.bcast(grid, root=0)
scoreDict = mpi.bcast(scoreDict, root=0)
multiWordsDict = mpi.bcast(multiWordsDict, root=0)

# Data on the tweets
tweets = []

with open(twitterFile, encoding='utf-8') as f:
    for index, line in enumerate(f):
        if (index - 1) % size == rank and index != 0:
            # Ignore the first line or stop once we reach the end
            if line == "[\n" or line == "]}\n":
                continue

            try:
                # Fix the json line broken by the enumeration
                if line.endswith(']}\n'):
                    tweet = json.loads(line[:-3])
                elif line.endswith(']}') or line.endswith(',\n'):
                    tweet = json.loads(line[:-2])

                elif line.endswith('\n'):
                    tweet = json.loads(line[:-1])

                # Get just the text and coordinates
                value = tweet["value"]
                text = value["properties"]["text"]
                x, y = value["geometry"]["coordinates"]
                tweets.append([text, x, y])

            except:
                print("Error reading line {}: {}\n\n".format(index, line))
                continue

f.close()

'''
Description: return which grid the point belong to.
            (x,y) in grid if xmin < x <= xmax 
                              ymin <= y < ymax 
'''


def getGridBelong(gridDict, xpos, ypos):
    for key in gridDict.keys():
        grid_xmin, grid_xmax, grid_ymin, grid_ymax = gridDict[key]

        if grid_xmin < xpos <= grid_xmax and grid_ymin <= ypos < grid_ymax:
            return key
    return -1


'''
input: tweets: list(list(tweet_content,xpos,ypos))
      gridDict: dict(ID:[xmin,xmax,ymin,ymax])
output: 
      gridScore: dict(ID:[count,score])
Description: this function plays the main role for producing the result described in instruction
'''


def preprocessingData(tweets, gridDict):
    ignoreList = [',', '.', '?', '!', '\"', '\'']
    gridScore = {x: [0, 0] for x in gridDict.keys()}
    for content, xpos, ypos in tweets:
        content = content.lower()
        contentScore = 0
        # search for multiple keys first
        for multiKey in multiWordsDict:
            occurCnt = content.count(multiKey)
            contentScore = contentScore + occurCnt * multiWordsDict[multiKey]
            content = content.replace(multiKey, '')

        # split the tweets based on space
        content_words = content.strip().lower().split(' ')
        n = len(content_words)

        # decide which grid the tweet belongs to
        gridID = getGridBelong(gridDict, xpos, ypos)
        # Warning: all content get gridID = C2 (fixed: the feature of the dataset)
        if gridID == -1:
            # print("Error: content position not in any grid")
            continue
        # increase the number of tweets in the grid area
        gridScore[gridID][0] = gridScore[gridID][0] + 1

        # search for single words
        for index, word in enumerate(content_words):
            # recursively remove all matched
            while len(word) > 0 and word[-1] in ignoreList:
                word = word[:-1]  # conditionally remove end punctuation
            wordScore = scoreDict.get(word)
            # print(wordScore)
            if not wordScore:
                continue

            if wordScore:
                contentScore = contentScore + wordScore
        gridScore[gridID][1] = gridScore[gridID][1] + contentScore
    return gridScore


# processed: {gridID:[tweetCount, gridScore]}
processed = preprocessingData(tweets, grid)


'''
input: 
    dict1: {gridID:[tweetCount, gridScore]}
    dict2: {gridID:[tweetCount, gridScore]}
output: 
    dict1: {gridID:[tweetCount, gridScore]}
Description: Adds the tweetCount and gridScore for similar gridIDs
'''


def dictCounter(dict1, dict2, datatype):
    for cell in dict1:
        totalTweets = dict1[cell][0] + dict2[cell][0]
        score = dict1[cell][1] + dict2[cell][1]
        dict1[cell] = [totalTweets, score]
    return dict1


dictSumOp = MPI.Op.Create(dictCounter, commute=True)
finalResult = mpi.reduce(processed, op=dictSumOp, root=0)

if rank == 0:
    endTime = time.time()
    print("time used: ", endTime - startTime)
    for gridID, scores in finalResult.items():
        print("{}: {} {}".format(gridID, scores[0], scores[1]))

