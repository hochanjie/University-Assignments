# Assignment 2 COMP30024: Artificial Intelligence Semester 1 2020

# Group Name: DeepMagic
# 
# Student 1 Name: Chan Jie Ho
# Student 1 Number: 961948
#
# Student 2 Name: Shivam Agarwal
# Student 2 Number: 951424

# Actions module holds all the classes and functions required to define the
# actions taken in the game.

from referee.game import _NEAR_SQUARES
import copy

# ============================================================================ #
# ACTION CLASS #
# ------------ #
#
# Action superclass that defines the action.
#
# It takes in the name of the action as input.

class Action:

    def __init__(self, name):
        self.name = name

# ============================================================================ #
# BOOM CLASS #
# ---------- #
#
# Boom action class that holds all the variables needed to define a unique boom.
#
# It requires the coordinates of the tile to boom.

class Boom(Action):

    def __init__(self, coord):
        super().__init__('BOOM')
        self.x = coord[0]
        self.y = coord[1]

    def get_tuple_form(self):
        # ("BOOM", (x, y))        
        return (self.name, (self.x, self.y))


# ============================================================================ #
# MOVE CLASS #
# ---------- #
#
# Move action class that holds all the variables needed to define a unique move.
#
# It requires the number of pieces to move and the coordinates of the source
# and destination tiles.

class Move(Action):

    def __init__(self, no, s_coord, d_coord):
        super().__init__('MOVE')
        self.no_of_pieces = no
        self.src_x = s_coord[0]
        self.src_y = s_coord[1]
        self.dst_x = d_coord[0]
        self.dst_y = d_coord[1]

    def get_tuple_form(self):
        # ("MOVE", n, (xa, ya), (xb, yb))
        return (self.name, self.no_of_pieces, (self.src_x, self.src_y), (self.dst_x, self.dst_y))


# ============================================================================ #
# VALID_MOVES FUNCTION #
# -------------------- #
#
# Helper function that generates all the possible moves that a player can take
# depending on the current setup of the board.
#
# It takes in the coordinates of the player's/opponent's pieces/stacks
# and returns a list of all the possible movements.

def valid_moves(player_pieces, enemy_pieces):

    valid = []
    direction = ['N', 'S', 'E', 'W']

    # Iterate through each tile that the player is on
    for piece in player_pieces.keys():

        # One possible action is to boom that tile
        x, y = piece
        n = player_pieces[piece]
        valid.append(Boom((x, y)))

        # Other possible actions would be to move the piece(s) on that tile

        # Allow for movement of up to n steps for the n pieces in the stack
        for i in range(1, n + 1):

            # Allow for movement of up to n pieces from the stack
            for steps in range(1, n + 1):

                # Allow for any of the 4 directions
                for cardinal in direction:

                    # Find the coordinates of the destination tile
                    dest_x = x
                    dest_y = y

                    if cardinal == 'N':
                        dest_y += steps

                    elif cardinal == 'S':
                        dest_y -= steps

                    elif cardinal == 'E':
                        dest_x += steps

                    else:
                        dest_x -= steps

                    # Check if the destination tile is on the board
                    if dest_x in range(8) and dest_y in range(8):

                        # Only valid if the destination tile does not have a
                        # black piece on it
                        if not ((dest_x, dest_y) in enemy_pieces):
                            valid.append(Move(n, (x, y), (dest_x, dest_y)))

    return valid

# ---------------------------------------------------------------------------- #
# MOVE FUNCTION #
# ------------- #
#
# Helper function that changes the internal representation of the board to
# reflect a move of one of the player's/opponent's pieces.
#
# It takes our player instance, number of pieces to move, the coordinates
# of the source and destination, and the colour of the moved piece(s) as input.
#
# The action input will be in the form of n, (xa, ya), (xb, yb).

def move(player, action, colour):

    pieces, (xa, ya), (xb, yb) = action

    # Increment the number of pieces at the destination tile
    player.board[xb][yb].n += pieces

    # Update our player instance of the number of pieces at the destination tile
    # and whether it is the player's own piece or their opponent's
    if player.colour == colour:
        player.pieces[(xb, yb)] = player.board[xb][yb].n

    else:
        player.opponent[(xb, yb)] = player.board[xb][yb].n

    # Reduce the number of pieces at the source tile
    player.board[xb][yb].colour = player.board[xa][ya].colour
    player.board[xa][ya].n -= pieces

    # If the source tile is now empty then remove the key from the dictionary
    # that the player instance stores
    if player.board[xa][ya].n == 0:
        player.board[xa][ya].colour = None

        if player.colour == colour:
            del player.pieces[(xa, ya)]

        else:
            del player.opponent[(xa, ya)]

    # If it's not empty then just update the dictionaries with the new number
    # of pieces left on it
    else:
        if player.colour == colour:
            player.pieces[(xa, ya)] = player.board[xa][ya].n

        else:
            player.opponent[(xa, ya)] = player.board[xa][ya].n


# ---------------------------------------------------------------------------- #
# BOOM FUNCTION #
# ------------- #
#
# Helper function that changes the internal representation of the board to
# reflect a boom of one of the player's pieces.
#
# It takes our player instance, and the coordinate of the boom as input.

def boom(player, coordinate):

    x, y = coordinate

    # Update our board to have 0 pieces on that tile while making note of the
    # colour of the piece(s) that exploded
    player.board[x][y].n = 0
    colour = player.board[x][y].colour
    player.board[x][y].colour = None

    # Remove the piece(s) from our dictionary representations of the pieces
    if player.colour == colour:
        del player.pieces[(x, y)]

    else:
        del player.opponent[(x, y)]

    # Check if any other pieces near the coordinate got caught in the explosion
    for (near_x, near_y) in _NEAR_SQUARES((x, y)):
        if player.board[near_x][near_y].n != 0:
            boom(player, (near_x, near_y))


# ---------------------------------------------------------------------------- #
# GET_ALL_STATES FUNCTION #
# ----------------------- #
#
# Helper function that finds all the possible states that can be created from
# applying any of the possible actions.
#
# It takes in our player instance, the current game state, and whether we are
# trying to maximise the player or the opponent as input and returns a list of
# all possible states.
#
# game_state will be in the form of (self.pieces, self.opponent)

def get_all_states(player, maximising_player):

    all_states = []

    # Get the colour of the player to maximise and the list of possible actions
    # that the player can take
    if maximising_player:
        moves = valid_moves(player.pieces, player.opponent)
        colour = player.colour

    else:
        moves = valid_moves(player.opponent, player.pieces)
        if player.colour == "white":
            colour = "black"

        else:
            colour = "white"

    # Iterate through the list of possible actions and get the resulting state
    for movement in moves:

        action = movement.get_tuple_form()
        temp = copy.deepcopy(player)
        temp.update(colour, action)
        all_states.append(temp)

    return all_states

# ---------------------------------------------------------------------------- #
# APPLY_ACTION FUNCTION #
# --------------------- #
#
# Helper function that applies the given action without altering the player 
# with the given game state.
#
# It takes in our player instance which has the current game state, and the 
# action as input and returns a copy of the player with the new game state.

def apply_action(player, action_obj):

    action = action_obj.get_tuple_form()
    temp = copy.deepcopy(player)

    if action[0] == "MOVE":
        move(temp, action[1:], temp.colour)

    else:
        boom(temp, action[1])

    return temp

# ---------------------------------------------------------------------------- #
# REMOVE_BOOMS FUNCTION #
# --------------------- #
#
# Helper function that removes all the boom actions from the list of actions.
#
# It takes in the list of valid actions, and removes the booms from that list.

def remove_booms(list_actions):

    for act in list_actions:
        if act.name == "BOOM":
            list_actions.remove(act)

# ============================================================================ #

# :)