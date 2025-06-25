"""
This is a template for Project 1, Task 1 (Induced demand-supply)
"""

from enum import Enum
from fmclient import Agent, OrderSide, Order, OrderType, Session, Market
from typing import List
import copy

# Student details
SUBMISSION = {"number": "961948", "name": "Chan Jie Ho"}

# ------ Add a variable called PROFIT_MARGIN -----
PROFIT_MARGIN = 20


# GLOBAL VARIABLES MADE BY ME #
# --------------------------- #
MAX_UNITS = 1       # Fixed number of units per order
MANAGER = "M000"    # Manager ID in private market

# ==================================================================================================================== #

# Enum for the roles of the bot
class Role(Enum):
    BUYER = 0
    SELLER = 1


# Let us define another enumeration to deal with the type of bot
class BotType(Enum):
    MARKET_MAKER = 0
    REACTIVE = 1


class DSBot(Agent):
    # ------ Add an extra argument bot_type to the constructor -----
    def __init__(self, account, email, password, marketplace_id, bot_type):
        super().__init__(account, email, password, marketplace_id, name="DSBot")
        self._public_market_id = 0
        self._private_market_id = 0
        self._role = None
        # ------ Add new class variable _bot_type to store the type of the bot
        self._bot_type = bot_type

        self._private_value = None
        self._waiting = False
        self._pending = None
        self._orders_sent = 0 # Can be used as a reference for orders when checking if accepted or rejected

    def role(self):
        return self._role

    def pre_start_tasks(self):
        pass

    # Set the public and private market IDs and other attributes the bot should remember when initialising the bot
    def initialised(self):

        self._private_value = None
        self._waiting = False
        self._pending = None
        self._orders_sent = 0

        for key, value in self.markets.items():
            if value.private_market:
                self._private_market_id = value._id
            else:
                self._public_market_id = value._id

    def order_accepted(self, order: Order):
        self.inform("Order {} was accepted.\n".format(order))
        self._waiting = False


    def order_rejected(self, info, order: Order):
        self.inform("Order {} was rejected.\n".format(order))
        self._waiting = False

    def received_orders(self, orders: List[Order]):


         # Get all the current orders
        current_orders = Order.current()
        
        # Ensure there is only one pending order at a time if market maker
        # If reactive, we shouldn't even have pending orders
        num = 0
        for order in current_orders.values():
            if order.mine:
                if num == 0 and not order.is_private and self._bot_type == BotType.MARKET_MAKER:
                    num += 1
                else:
                    self._cancel_order(order)

        # Check if we're waiting for the server to receive our order
        if not self._waiting:
            
            

            # Check if we have any pending order and if has already been traded or if the private order was refreshed
            
            refreshed = False
            private_finished = True

            # Check if we already have a private value stored
                
            if (not self._private_value == None): 
                
                # Check if the private value has changed or if the original private value order has been depleted
                refreshed = False
                for order in current_orders.values():

                    if order.is_private:
                        if order.price == self._private_value.price:
                            private_finished = False
                        else:
                            refreshed = True

                # If the private value has been refreshed or depleted, cancel all currently pending orders
                if private_finished or refreshed:

                    self._pending = None
                    self._private_value = None

                    for current in current_orders.values():
                        if current.mine:
                            self._cancel_order(current)
                            # Assumption: If cancel order is rejected, it would already have been consumed anyways, so 
                            # no need to wait for server
                            
            # If private value remains the same, then check if our pending order has been traded
            if (not refreshed) and (not private_finished) and (not self._pending == None):
                still_pending = False

                for order in current_orders.values():
                    if order.mine and not order.is_private:
                        still_pending = True

                if not still_pending:

                    self._pending = None
                    self._respond_to_private_order(self._private_value)
                    self._waiting = True

            # If no pending order and not waiting for server, then we can attempt a new trade order
            if self._pending == None and not self._waiting:
                
                # Find if we have any new private orders coming in
                for order in current_orders.values():

                    if order.is_private:
                        
                        # Set the bot role and store the private value
                        self._set_role(order)
                        self._private_value = order

                        # Send out the order according to the bot type
                        if self._bot_type == BotType.MARKET_MAKER:
                            
                            self._waiting = True
                            self._create_public_order(order.price)
                            
                        else:
                            self._find_profitable_trade()

                        break

                        

    def _print_trade_opportunity(self, other_order, response):
        self.inform(f"I am a {self.role()} with profitable order {other_order} {response}\n")

    def received_completed_orders(self, orders, market_id=None):
        pass

    def received_holdings(self, holdings):
        # cash = holdings.cash
        # cash_available = holdings.cash_available
        # assets = holdings.assets
        # for key, value in assets.items():
        #     self.inform(value)
        pass
        

    def received_session_info(self, session: Session):
        self.initialised()

    # ---------------------------------------------------------------------------------------------------------------- #
    # FUNCTIONS CREATED BY ME #
    # ======================= #
    #
    # _SET_ROLE FUNCTION #
    # ------------------ #
    #
    # Helper function that sets the role that the bot should play in the public market.
    #
    # It takes in the private order that was received and alters the _role variable in our bot class.

    def _set_role(self, private_order):

        # If the private order is a buy order, we should be buying in the public as well, and vice versa
        if private_order.order_side == OrderSide.BUY:
            self._role = Role.BUYER
        else: 
            self._role = Role.SELLER
    
    
    # ---------------------------------------------------------------------------------------------------------------- #
    # _CREATE_PUBLIC_ORDER FUNCTION #
    # ----------------------------- #
    #
    # Helper function that sends an order to the public market.
    #
    # It takes in the price to sell the ONE unit, creates the order, then sends out the order after checking its 
    # validity.

    def _create_public_order(self, price):

        order = Order.create_new()
        order.market = Market(self._public_market_id)
        order.price = price
        order.order_type = OrderType.LIMIT
        order.units = MAX_UNITS

        if self.role() == Role.BUYER:
            order.order_side = OrderSide.BUY

            # We only need to alter the price if we're a market maker
            if self._bot_type == BotType.MARKET_MAKER:
                order.price -= PROFIT_MARGIN

                # Ensure price follows price tick
                order.price -= order.price % Market(self._public_market_id).price_tick

        else:
            order.order_side = OrderSide.SELL   

            if self._bot_type == BotType.MARKET_MAKER:   

                order.price += PROFIT_MARGIN
                order.price += order.price % Market(self._public_market_id).price_tick

        # Validate the order before sending it out
        if self._validate_order(order):
            
            # Order is valid, so set the reference, send it out, store the pending order, then make sure our bot waits 
            # for the server to accept/reject the order

            self._orders_sent += 1
            order.ref = self._orders_sent
            self._pending = order
            self.send_order(order)
            self.waiting = True
            

    # ---------------------------------------------------------------------------------------------------------------- #
    # _RESPOND_TO_PRIVATE_ORDER FUNCTION #
    # ---------------------------------- #
    #
    # Helper function that responds to the order given by the manager in the private market.
    #
    # It takes in the manager's order that we are responding to and sends out the appropriate order.

    def _respond_to_private_order(self, manager_order):

        order = Order.create_new()
        order.market = Market(self._private_market_id)
        order.order_type = OrderType.LIMIT
        order.price = manager_order.price
        order.units = MAX_UNITS
        order.owner_or_target = MANAGER

        if manager_order.order_side == OrderSide.BUY:
            order.order_side = OrderSide.SELL
        else:
            order.order_side = OrderSide.BUY

        # Validate the order before sending it out
        
        if self._validate_order(order):

            # Set the reference again
            self._orders_sent += 1
            order.ref = self._orders_sent
            self.send_order(order)

    # ---------------------------------------------------------------------------------------------------------------- #
    # _CANCEL_ORDER FUNCTION #
    # ---------------------- #
    #
    # Helper function that cancels a pending order.
    #
    # It takes in the order that we want to cancel, copies it, and sends out a cancel order.

    def _cancel_order(self, order):

        # Copy the order details
        cancelled = copy.copy(order)
        cancelled.order_type = OrderType.CANCEL

        # Validates the cancel order
        if self._validate_order(cancelled):
            self.send_order(cancelled)

    # ---------------------------------------------------------------------------------------------------------------- #
    # _VALIDATE_ORDER FUNCTION #
    # ------------------------ #
    #
    # Helper function that validates an order before it is sent out.
    #
    # It takes in the order that we want to order and makes sure the order is valid by checking the following:
    #   - Price is not above (below) the max (min) bid
    #   - Bot is not sending out an order that has already been sent out
    #   - Bot has enough cash to buy
    #   - Bot has enough widgets to sell
    #   - Bot is cancelling an order that belongs to us and hasn't been consumed
    #   - The order does not reduce our profitability

    def _validate_order(self, order):

        # Check if bot is cancelling an order that belongs to us and has not yet been consumed
        if order.order_type == OrderType.CANCEL:
            return (order.mine and not order.is_consumed)

        else:
                        
            # Ensure price is not above (below) the max (min) bid
            if order.price < 0 or order.price > 1000:
                return False

            # Ensure bot is not sending an order twice
            if not self._pending == None:
                if self._pending.ref == order.ref:
                    return False

        
            # Ensure there is only one pending order at a time
            current_orders = Order.current()

            for current_order in current_orders.values():
                if current_order.mine:
                    return False

            # Calculate profits
            holdings = self.holdings

            if self._role == Role.BUYER:
                profit = self._private_value.price - order.price

            else:
                profit = order.price - self._private_value.price
            
            
            # Check if bot has enough widgets to sell and ensure it is profitable
            if order.order_side == OrderSide.SELL:
                return (holdings.assets[order.market].units_available - order.units >= 0) and (profit >= -PROFIT_MARGIN) and (order.price % Market(self._public_market_id).price_tick == 0) 

            # Check if bot has enough cash to buy and ensure it is profitable
            else:    
                return (holdings.cash_available - order.price >= 0) and (profit >= PROFIT_MARGIN) and (order.price % Market(self._public_market_id).price_tick == 0) 

    # ---------------------------------------------------------------------------------------------------------------- #
    # _FIND_ORDER FUNCTION #
    # -------------------- #
    #
    # Helper function that finds the best order in the market.
    #
    # It returns the best order based on the best bid/ask in the market.
    
    def _find_order(self):

        best_order = None

        # Find the type of order that we're finding based on our bot's role
        if self.role() == Role.BUYER:
            order_side = OrderSide.SELL
            buying = True
        else: 
            order_side = OrderSide.BUY
            buying = False

        # Find the best order
        current_orders = Order.current()
        for value in current_orders.values():
            if value.order_side == order_side and not value.is_private:
                if buying:
                    if best_order == None or best_order.price > value.price:
                        best_order = value
                else: 
                    if best_order == None or best_order.price < value.price:
                        best_order = value

        return best_order

    
    # ---------------------------------------------------------------------------------------------------------------- #
    # _FIND_PROFITABLE_TRADE FUNCTION #
    # ------------------------------- #
    #
    # Helper function that finds profitable trade opportunities and responds to them accordingly.
    #
    # It will find the best order based on the best bid/ask and prints it out along with our response to it.

    def _find_profitable_trade(self):
        
        # Find the best order based on the best bid/ask based on our bot role
        order = self._find_order()

        if not order == None:

            # Calculate the profit gained from the trade
            if self._role == Role.BUYER:
                profit = self._private_value.price - order.price
            else:
                profit = order.price - self._private_value.price
            
            holdings = self.holdings

            if (profit > 0):

                if (profit < PROFIT_MARGIN) :
                    response = "but I will not respond as it does not meet my minimum profit margin."

                elif order.order_side == OrderSide.SELL and holdings.assets[order.market].units_available - order.units < 0:
                    response = "but I am unable to respond as I do not have enough widgets to sell."  
                
                elif order.order_side == OrderSide.BUY and holdings.cash_available - order.price < 0:
                    response = "but I am unable to respond as I do not have enough cash to buy."  

                else:    
                    response = "and I will be taking it."
                    self._create_public_order(order.price)
                    self._waiting = True
    
                self._print_trade_opportunity(order, response)

# ==================================================================================================================== #

if __name__ == "__main__":
    FM_ACCOUNT = "ardent-founder"
    FM_EMAIL = "chanjie.ho@student.unimelb.edu.au"
    FM_PASSWORD = "961948"
    MARKETPLACE_ID = 915

    # Comment out one of the following out depending on how you want the bot to act during the session
    _botType = BotType.MARKET_MAKER
    #_botType = BotType.REACTIVE
 
    ds_bot = DSBot(FM_ACCOUNT, FM_EMAIL, FM_PASSWORD, MARKETPLACE_ID, _botType)
    ds_bot.run()

# ==================================================================================================================== #

# :)