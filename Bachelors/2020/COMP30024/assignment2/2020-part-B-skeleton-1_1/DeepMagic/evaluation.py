# Assignment 2 COMP30024: Artificial Intelligence Semester 1 2020

# Group Name: DeepMagic
# 
# Student 1 Name: Chan Jie Ho
# Student 1 Number: 961948
#
# Student 2 Name: Shivam Agarwal
# Student 2 Number: 951424

# Evaluation module holds all the classes and functions required to evaluate 
# and estimate a score for the given state in the form of:
#
# Eval(s) = w1f1(s) + w2f2(s) + . . . + wnfn(s)

from referee.game import _NEAR_SQUARES
import copy

_PLAYER_PIECES_COUNT_WEIGHT_ = 100
_OPPONENT_PIECES_COUNT_WEIGHT_ = -40

_PLAYER_STACKS_WEIGHT_ = 10
_OPPONENT_STACKS_WEIGHT_ = -5

_PLAYER_MAX_HEIGHT_WEIGHT_ = 10
_OPPONENT_MAX_HEIGHT_WEIGHT_ = -5

_PLAYER_CLUSTER_COUNT_WEIGHT_ = -5
_OPPONENT_CLUSTER_COUNT_WEIGHT_ = 10

_PLAYER_CLUSTER_SIZE_WEIGHT_ = -5
_OPPONENT_CLUSTER_SIZE_WEIGHT_ = 10

_PLAYER_CLUSTER_DANGER_SIZE_WEIGHT_ = -5
_OPPONENT_CLUSTER_DANGER_SIZE_WEIGHT_ = 10

# ============================================================================ #
# EVALUATE FUNCTION #
# ----------------- #
#
# Evaluate function that determines the overall score of the evaluation. 
#
# It takes in the player instance which stores all the remaining pieces on the 
# board and returns the final result of the evaluation.
# 
# Values to use when evaluating:
# - Number of player pieces
# - Number of opponent pieces
# - Number of player stacks
# - Number of opponent stacks
# - Max height of player stacks
# - Max height of enemy
# - Average size of player clusters
# - Average size of enemy clusters
# - Clustering score 

def evaluate(player):

    pieces = player.pieces
    opponent = player.opponent

    player_pieces = sum(pieces.values())
    opponent_pieces = sum(opponent.values())

    if player_pieces == 0 and opponent_pieces != 0:
        return -10000000

    elif opponent_pieces == 0 and player_pieces != 0:
        return 10000000

    value = 0
    value += _PLAYER_PIECES_COUNT_WEIGHT_ * player_pieces
    value += _OPPONENT_PIECES_COUNT_WEIGHT_ * opponent_pieces

    # value -= find_min_distance(pieces, opponent)

    # value += boom_score(player) * 5

    value += _PLAYER_STACKS_WEIGHT_ * stacks_score(pieces)
    value += _OPPONENT_STACKS_WEIGHT_ * stacks_score(opponent)

    if not pieces.values() or not opponent.values():
        return value
    
    value += _PLAYER_MAX_HEIGHT_WEIGHT_ * max(pieces.values())
    value += _OPPONENT_MAX_HEIGHT_WEIGHT_ * max(opponent.values())

    value += clustering_score(pieces, True)
    value += clustering_score(opponent, False)
    
    value += danger_clustering_score(pieces, opponent)

    return value

# ---------------------------------------------------------------------------- #

# STACKS_SCORE FUNCTION #
# --------------------- #
#
# Score calculator helper function that scores the stacking of the pieces. This 
# is so we can emphasize stacking from our side as stacking provides us the 
# mobility we need to attack from a far distance.
# 
# It takes in the player's/opponent's pieces as input and returns how many 
# pieces are stacked.

def stacks_score(pieces):

    value = 0
    
    for coordinate in pieces.keys():
        n = pieces[coordinate]

        if n > 1:
            value += 1

    return value

# ---------------------------------------------------------------------------- #
# DANGER_CLUSTERING_SCORE FUNCTION #
# -------------------------------- #
#
# Score calculator helper function that scores the difference between the size 
# of our clusters that the opponent is connected to and can easily boom and 
# vice versa. This is so we can minimise the risk we take when we have a large 
# cluster and emphasize attacking their large clusters, sacrificing as little 
# as possible.
#
# It takes all the pieces remaining as input and returns the appropriate value.

def danger_clustering_score(player_pieces, opponent_pieces):

    value = 0
    all_connected_clusters = connected_clusters(player_pieces, opponent_pieces)

    for connected_cluster in all_connected_clusters:

        value += (_PLAYER_CLUSTER_DANGER_SIZE_WEIGHT_ * connected_cluster["player pieces"])
        value += (_OPPONENT_CLUSTER_DANGER_SIZE_WEIGHT_ * connected_cluster["opponent pieces"])
    
    return value

# ---------------------------------------------------------------------------- #
# CONNECTED_CLUSTERS FUNCTION #
# --------------------------- #
#
# Helper function that finds all the player's and opponent's clusters that are 
# connected to each other (are next to each other).
#
# It takes all the pieces remaining as input and returns a list of connected 
# clusters in the form of a dictionary. 
# 
# The output is in the form of [{player: [clusters], opponent: [clusters]}, ...]

def connected_clusters(player_pieces, opponent_pieces):
    
    player_clusters = find_all_clusters(player_pieces)
    connecting_clusters = []
    checked_clusters = [] 

    # Iterate through our clusters
    for cluster, number in player_clusters:
        if not cluster in checked_clusters:
            checked_clusters.append(cluster)

            connected = {}
            connected_players = []
            connected_opponents = []
            connected_player_pieces = 0
            connected_opponent_pieces = 0

            surrounding_tiles = surrounding_cluster(cluster)

            # Find if there are any of the opponent pieces near our cluster(s) or if our pieces are near the opponent's cluster(s)
            for tile in surrounding_tiles:

                # If found the opponent pieces, find that particular cluster
                if tile in opponent_pieces:
                    if not tile in connected_opponents:

                        opponent_cluster, opponent_cluster_number = find_cluster(tile, [], opponent_pieces)

                        for new_tile in surrounding_cluster(opponent_cluster):
                            surrounding_tiles.append(new_tile)

                        for piece in opponent_cluster: 
                            connected_opponents.append(piece)

                        connected_opponent_pieces += opponent_cluster_number

                # If found another one of our clusters
                elif tile in player_pieces:
                    if tile not in connected_players:
                        for checking_cluster, checking_cluster_number in player_clusters:
                            if tile in checking_cluster:
                                checked_clusters.append(checking_cluster)

                                for new_tile in surrounding_cluster(checking_cluster):
                                    surrounding_tiles.append(new_tile)

                                for piece in checking_cluster: 
                                    connected_players.append(piece)

                                connected_player_pieces += checking_cluster_number
                                

            if connected_players:
                connected["player"] = connected_players
                connected["opponent"] = connected_opponents
                connected["player pieces"] = connected_player_pieces
                connected["opponent pieces"] = connected_opponent_pieces
                connecting_clusters.append(connected)

    return connecting_clusters

# ---------------------------------------------------------------------------- #
# SURROUNDING_CLUSTER FUNCTION #
# ---------------------------- #
#
# Helper function that finds all the tiles surrounding a cluster.
#
# It takes a cluster as input and returns a list of surrounding tiles.

def surrounding_cluster(cluster):
    tiles = []

    for piece in cluster:    
        surrounding = _NEAR_SQUARES(piece)

        for tile in surrounding:
            if tile not in tiles and tile not in cluster:
                tiles.append(tile)

    return tiles

# ---------------------------------------------------------------------------- #
# FIND_ALL_CLUSTERS FUNCTION #
# -------------------------- #
#
# Helper function that finds all the clusters that a player has.
#
# It takes the player's remaining pieces as input and returns a list of 
# clusters.

def find_all_clusters(pieces):

    clusters = []
    visited = []

    for coordinate in pieces.keys():
        if not coordinate in visited:
            clusters.append(find_cluster(coordinate, visited, pieces))

    return clusters

# ---------------------------------------------------------------------------- #
# FIND_CLUSTER FUNCTION #
# --------------------- #
#
# Helper function that finds all pieces in the same cluster as the piece at 
# coordinate given. It uses a breadth first search algorithm to find the 
# connected pieces.
#
# It takes the starting coordinate, a list of already visited tiles, and all 
# the player's remaining pieces in the game as input and returns a 2-tuple of 
# a list of the coordinates of pieces/stacks that are part of the cluster and 
# the number of individual pieces in that cluster.

def find_cluster(coordinate, visited, pieces):

    # Mark coordinate as visited
    visited.append(coordinate)
    queue = []
    cluster = []
    cluster_pieces = 0

    # Add coordinate to cluster
    queue.append(coordinate)
    cluster.append(coordinate)
    cluster_pieces += pieces[coordinate]

    # Find in immediate vicinity
    while queue:
        tile = queue.pop(0)

        for coordinates in _NEAR_SQUARES(tile):
            if coordinates in pieces:
                if not coordinates in visited:

                    queue.append(coordinates)
                    visited.append(coordinates)
                    cluster.append(coordinates)

                    cluster_pieces += pieces[coordinates]

    return (cluster, cluster_pieces)

# ---------------------------------------------------------------------------- #
# CLUSTERING_SCORE FUNCTION #
# ------------------------- #
#
# Score calculator helper function that scores the average size of the player's 
# clusters as well as the size of the largest cluster that the player has. This 
# is so we can minimise the risk we take when we have large clusters.
#
# It takes all the pieces remaining and whether it is of the player's or 
# opponent's pieces as input and returns the appropriate value.

def clustering_score(pieces, player):

    value = 0

    clusters = find_all_clusters(pieces)
    clusters_count = 0
    clusters_size_average = 0
    
    for cluster in clusters:
        clusters_count += 1
        clusters_size_average += cluster[1]

    clusters_size_average = clusters_size_average/clusters_count

    if player:  
        value += _PLAYER_CLUSTER_COUNT_WEIGHT_ * clusters_count
        value += _PLAYER_CLUSTER_SIZE_WEIGHT_ * clusters_size_average

    else:
        value += _OPPONENT_CLUSTER_COUNT_WEIGHT_ * clusters_count
        value += _OPPONENT_CLUSTER_SIZE_WEIGHT_ * clusters_size_average

    return value

# ---------------------------------------------------------------------------- #
# BOOM_SCORE FUNCTION #
# ------------------- #
#
# Helper function – not used anymore

def boom_score(player):
    pieces = player.pieces
    ans = 0
    for piece in pieces.keys():
        temp = copy.deepcopy(player)
        boom(temp, piece)
        resultant = sum(temp.pieces.values()) - sum(temp.opponent.values())
        original = sum(pieces.values()) - sum(player.opponent.values())
        ans += (resultant - original)

    return ans

# ---------------------------------------------------------------------------- #
# FIND_MIN_DISTANCE FUNCTION #
# -------------------------- #
#
# Helper function – not used anymore

def find_min_distance(pieces, opponent):
    ans = 0
    for f_piece in pieces.keys():
        x1, y1 = f_piece
        for e_piece in opponent.keys():
            x2, y2 = e_piece
            dist = abs(x2-x1) + abs(y2-y1)
            ans += dist

    return ans

# ============================================================================ #

# :)