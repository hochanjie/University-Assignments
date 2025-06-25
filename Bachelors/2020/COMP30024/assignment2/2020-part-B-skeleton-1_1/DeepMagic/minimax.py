# Assignment 2 COMP30024: Artificial Intelligence Semester 1 2020

# Group Name: DeepMagic
# 
# Student 1 Name: Chan Jie Ho
# Student 1 Number: 961948
#
# Student 2 Name: Shivam Agarwal
# Student 2 Number: 951424

# Minimax module holds all the classes and functions required to define the
# minimax algorithm that decides which is the best action to play next.

from DeepMagic.actions import *
from DeepMagic.evaluation import *
import random

# ============================================================================ #
# MINIMAXAGENT CLASS #
# ------------------ #
#
# MinimaxAgent facade that runs the minimax algorithm.
# 
# It takes teh max depth of the tree which is how far the minimax algorithm will look ahead when determining the best action and the move number as input.

class MinimaxAgent:

    def __init__(self, max_depth, move_no):
        self.max_depth = max_depth
        self.move_no = move_no

    # ------------------------------------------------------------------------ #
    # MINIMAX_DECISION FUNCTION #
    # ------------------------- #
    #
    # Minimax helper function that decides what action the player should take.
    #
    # It returns the best possible action based on the values given.

    def minimax_decision(self, player):

        list_actions = valid_moves(player.pieces, player.opponent)

        if self.move_no < 5:
            remove_booms(list_actions)

        length = len(list_actions)
        value = [None] * length

        if length < 20:
            self.max_depth = 2

        elif length < 10:
            self.max_depth = 3
        
        for i, action in enumerate(list_actions):
            temp_player = apply_action(player, action)

            if self.max_depth <= 1:
                value[i] = evaluate(temp_player)
            else :
                value[i] = self.minimax_val(temp_player, self.max_depth, False)
        
        maximum = -10000
        
        for i, val in enumerate(value):
            if val > maximum:
                maximum = val
                index = i
        
        return list_actions[index].get_tuple_form()
        

    # ------------------------------------------------------------------------ #
    # MINIMAX_VAL FUNCTION #
    # -------------------- #
    #
    # Minimax helper function that finds the value of each branch in the tree.
    #
    # It takes in our player instance, the current game state, the depth that 
    # the algorithm is on and whether we are trying to maximise our player or
    # the opponent as input and returns the best action to play.

    def minimax_val(self, player, depth, maximising_player):

        # if depth == 0 or not sum(player.pieces.values()) or not sum(player.opponent.values()):
        if depth == 0:
            return evaluate(player)

        # apply all actions to the state and return the list of all the possible states
        # list of tuples of player pieces and opponent pieces
        all_states = get_all_states(player, maximising_player)

        if maximising_player:
            value = -1000000
            for child in all_states:
                value = max(value, self.minimax_val(child, depth - 1, False))
            return value
        else:
            value = 1000000
            for child in all_states:
                value = min(value, self.minimax_val(child, depth - 1, True))
            return value

# ============================================================================ #

# :)