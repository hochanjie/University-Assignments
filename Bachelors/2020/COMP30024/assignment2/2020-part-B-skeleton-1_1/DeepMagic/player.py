# Assignment 2 COMP30024: Artificial Intelligence Semester 1 2020

# Group Name: DeepMagic
# 
# Student 1 Name: Chan Jie Ho
# Student 1 Number: 961948
#
# Student 2 Name: Shivam Agarwal
# Student 2 Number: 951424

# Player module holds all the classes and functions required to define the
# player that will be playing the Expendibots game. The player should be able 
# to keep track of their pieces as well as their opponent's, and choose the 
# best possible action from the all the possible movements using a minimax
# algorithm, updating after each player's turn.

from DeepMagic.actions import *
from DeepMagic.evaluation import *
from DeepMagic.minimax import *
from referee.game import _BLACK_START_SQUARES, _WHITE_START_SQUARES

# ============================================================================ #
# EXAMPLEPLAYER CLASS #
# ------------------- #
#
# ExamplePlayer class that will play the Expendibots game, keeping track of its
# own pieces and the opponents pieces by setting up an 8x8 matrix of the board 
# as well as two dictionaries of the coordinates of the pieces and the number 
# of pieces on that coordinate (like Assignment 1).
#
# It takes the colour of the player (either "black" or "white") as input.

class ExamplePlayer:

    def __init__(self, colour):
        """
        This method is called once at the beginning of the game to initialise
        your player. You should use this opportunity to set up your own internal
        representation of the game state, and any other information about the
        game state you would like to maintain for the duration of the game.

        The parameter colour will be a string representing the player your
        program will play as (White or Black). The value will be one of the
        strings "white" or "black" correspondingly.
        """    
        self.colour = colour
        self.board, self.pieces, self.opponent = set_board(colour)
        self.no_of_move = 0

    # ------------------------------------------------------------------------ #
    # ACTION FUNCTION #
    # --------------- #
    #
    # Action function that decides what action the player should take
    #
    # It returns the best possible action, decided by the minimax algorithm

    def action(self):
        """
        This method is called at the beginning of each of your turns to request
        a choice of action from your program.

        Based on the current state of the game, your player should select and
        return an allowed action to play on this turn. The action must be
        represented based on the spec's instructions for representing actions.
        """
        self.no_of_move += 1
        return MinimaxAgent(1, self.no_of_move).minimax_decision(self)

    # ------------------------------------------------------------------------ #

    # UPDATE FUNCTION #
    # --------------- #
    #
    # Update function that updates the internal representation of the board and
    # the pieces remaining on the board that the player and the opponent
    # controls after each player's turn.
    #
    # It takes in the colour of the player that last performed the action and
    # the action performed.

    def update(self, colour, action):
        """
        This method is called at the end of every turn (including your player’s
        turns) to inform your player about the most recent action. You should
        use this opportunity to maintain your internal representation of the
        game state and any other information about the game you are storing.

        The parameter colour will be a string representing the player whose turn
        it is (White or Black). The value will be one of the strings "white" or
        "black" correspondingly.

        The parameter action is a representation of the most recent action
        conforming to the spec's instructions for representing actions.

        You may assume that action will always correspond to an allowed action
        for the player colour (your method does not need to validate the action
        against the game rules).
        """
        
        # Determine if the action was a move or boom action and perform it
        if action[0] == "MOVE":
            move(self, action[1:], colour)

        else:
            boom(self, action[1])

# ============================================================================ #
# CELLOBJECT CLASS #
# ---------------- #
#
# CellObject class that makes up each individual tile in the 8x8 matrix and 
# what it holds.
# 
# It requires the number of pieces on that tile and the type of the pieces 
# (whether the algorithm will be finding the max of it – the player – or the 
# min – the opponent) and the coordinates.


class CellObject:

    def __init__(self, n, colour, coordinate):
        self.n = n
        self.colour = colour
        self.coordinate = coordinate

# ============================================================================ #
# SET_BOARD FUNCTION #
# ------------------ #
#
# Helper function that creates the initial 8x8 matrix of CellObjects and places
# all the initial pieces on the corresponding tiles.
#
# It takes the coordinates of the player's/opponent's pieces as input and
# returns 8x8 board, and the dictionaries of the coordinates of the player's/
# opponent's pieces and how many pieces are on each coordinate.

def set_board(colour):

    if colour == "white":
        player_pieces = _WHITE_START_SQUARES
        opponent_pieces = _BLACK_START_SQUARES
        opponent_colour = "black"

    else:
        player_pieces = _BLACK_START_SQUARES
        opponent_pieces = _WHITE_START_SQUARES
        opponent_colour = "white"

    board = [[0 for x in range(8)] for y in range(8)]

    # Fill the 8x8 matrix with empty tiles first
    for x in range(8):
        for y in range(8):
            board[x][y] = CellObject(0, None, (x, y))

    # Iterate through the given coordinates and fill the 8x8 matrix with
    # the pieces and add it to a dictionary to keep track of the pieces
    # using the same representation as Assignment 1
    player = {}
    opponent = {}

    for square in player_pieces:
        x, y = square
        board[x][y] = CellObject(1, colour, (x, y))
        player[(x, y)] = 1

    for square in opponent_pieces:
        x, y = square
        board[x][y] = CellObject(1, opponent_colour, (x, y))
        opponent[(x, y)] = 1

    return board, player, opponent

# ============================================================================ #

# :)