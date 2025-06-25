"""
This is a template bot for  the CAPM Task.
"""
import copy
import numpy as np
from enum import Enum
from itertools import combinations
from typing import List
from fmclient import Agent, OrderSide, Order, OrderType, Session, Market


# Submission details
SUBMISSION = {"number": "961948", "name": "Chan Jie Ho"}

# Global variables
CENTS_TO_DOLLAR = 100       # To change cents to dollars
EMPTY = -1                  # To signify a missing bid/ask
HALF_MINUTE = 30            # Number of seconds in half a minute
IS_MULTIPLE = 0             # To check if price is multiple of price tick
MAX_UNITS = 1               # Only send out 1 unit per order to make things simple
MINUTE = 60                 # To change seconds to minute
ORDERS_SENT = 1              # Number of orders sent per round
PAIRS = 2                   # To find pairs stocks when calculating covariances
MIN_PROFIT_MARGIN = 1      # Minimum profit margin when using market maker
STALE_RANGE = 50            # Max price difference between old order and new order
START = 0                   # Starting number
SECTION = 4                 # Number of sections to divide the session into


class BotType(Enum):
    MARKET_MAKER = 0
    REACTIVE = 1


class CAPMBot(Agent):

    def __init__(self, account, email, password, marketplace_id, initial_bot_type, risk_penalty=0.007, session_time=20):
        """
        Constructor for the Bot

        :param account: Account name
        :param email: Email id
        :param password: password
        :param marketplace_id: id of the marketplace
        :param risk_penalty: Penalty for risk
        :param session_time: Total trading time for one session
        """

        super().__init__(account , email, password, marketplace_id, name="CAPM Bot")
        self._payoffs = {}
        self._risk_penalty = risk_penalty
        self._session_time = session_time
        self._market_ids = {} 
        
        self._waiting = {}
        self._pending = {} 
        self._orders_sent = START
        self._bot_type = initial_bot_type
        self._remaining_time = (session_time * MINUTE, session_time * MINUTE) 

        self._securities = {}
        self._variances = {}
        self._covariances = {}
        self._expected_returns = {}
        
    def initialised(self):
        """
        Extracts payoff distribution and expected returns/variances/covariances for each security 
        and initialises all attributes
        """

        for market_id, market_info in self.markets.items():
            security = market_info.item
            description = market_info.description
            self._securities[market_info] = security
            self._market_ids[security] = market_id
            self._payoffs[security] = [int(a)/CENTS_TO_DOLLAR for a in description.split(",")]
            self._expected_returns[security] = np.mean(self._payoffs[security])
            self._variances[security] = np.var(self._payoffs[security])
            
        self._waiting = {}
        self._pending = {}
        self._orders_sent = START
        self._covariances = self._get_covariances()


    def get_potential_performance(self, orders):
        """
        Returns the portfolio performance if the given list of orders is executed. 
        If the orders is empty, it returns the current performance.
        The performance as per the following formula:
        Performance = ExpectedPayoff - b * PayoffVariance, where b is the penalty for risk.

        :param orders: List of orders
        :return: Performance as per the above formula
        """

        expected_payoff = START
        payoff_variance = START

        # Get the holdings after the trade if the orders were to be executed
        # If orders is empty, it returns the current holdings
        holdings = self._get_holdings_after_trade(orders)
        covariances = self._covariances

        for security, current in holdings.items():
            if security == "Cash":
                expected_payoff += current
            else:
                expected_payoff += current * self._expected_returns[security]
                payoff_variance += np.square(current) * self._variances[security]
        
        for (first, second), covariance in covariances.items():
            payoff_variance += 2 * holdings[first] * holdings[second] * covariance
                
        performance = expected_payoff - (self._risk_penalty * payoff_variance)

        return performance


    def is_portfolio_optimal(self):
        """
        Returns true if the current holdings are optimal (as per the performance formula), false otherwise.

        I changed the output of this signature to also return a list of orders that would make help to push our 
        portfolio to optimality as I read that it was alright to do this in the Discussion Forum.
        
        :return optimal: True if the current holdings are optimal, false otherwise
        :return optimal_orders: List of orders for the non-optimal securities that could optimise our portfolio
        """

        # If there are securities that are not optimal, store the orders that we could potentially send out to make our
        # portfolio optimal
        optimal_orders = []
        available = self._get_holdings(True)

        # Get the best bid and ask prices
        best_prices = self._get_best_prices()
        current_performance = self.get_potential_performance([])

        for security, (best_bid, best_ask) in best_prices.items():

            # If either best ask or best bid is missing, it means it's optimal
            if best_bid != EMPTY and best_ask != EMPTY:

                compare = []

                # Check if the performance increases if we sell the security (optimality on the sell-side)
                sell = self._create_order(security)

                # If we don't have enough units to sell, it means we're optimal on the sell-side
                if available[security] >= sell.units:
                    sell.price = best_bid
                    sell.order_side = OrderSide.SELL
                    potential = self.get_potential_performance([sell])
                    
                    # If the potential of the simulated order (based on bot type) is better than current, we save it
                    if self._bot_type == BotType.MARKET_MAKER:
                        potential, sell = self._market_maker(sell, OrderSide.SELL)

                    if current_performance < potential:
                        compare.append((potential, security, sell))
                            
                # Check if the performance increases if we buy the security (optimality on the buy-side)
                buy = self._create_order(security)
                buy.price = best_ask
                buy.order_side = OrderSide.BUY
                potential = self.get_potential_performance([buy])

                # If we don't have enough cash to buy, it means we're optimal on the buy-side
                # If the potential of the simulated order (based on bot type) is better than current, we save it
                if self._bot_type == BotType.MARKET_MAKER:
                    potential, buy = self._market_maker(buy, OrderSide.BUY)
                    
                if current_performance < potential:
                    if available["Cash"] >= buy.price/CENTS_TO_DOLLAR:
                        compare.append((potential, security, buy))

                # If we have at least one side that's not optimal, send the order side with the higher performance
                # This is to ensure we don't send out orders on both sides of the market at the same time
                if compare:
                    if len(compare) == PAIRS:
                        compare.sort(key = lambda x: x[0])
                    optimal_orders.append(compare[0])
            
        # If there are any securities that aren't optimal, sort it based on the performance
        if optimal_orders:
            optimal_orders.sort(key = lambda x: x[0]) 
            return False, optimal_orders

        return True, []
        

    def order_accepted(self, order: Order):
        """
        When order is accepted, remove it from the waiting dictionary.
        """
        security = self._securities[order.market]
        self._waiting.pop(security, None)


    def order_rejected(self, info, order: Order):
        """
        When order is rejected, remove it from the waiting dictionary.
        """
        security = self._securities[order.market]
        self._waiting.pop(security, None)


    def received_orders(self, orders: List[Order]):
        """
        Whenever receiving a new order, check if we're still waiting for the server to respond to our previous order.
        We then respond if one of our pending orders have been traded or if our portfolio is not optimal

        :param orders: List of orders sent to the market that the bot receives
        """
        
        # Check if we're waiting for the server to receive our order
        if not self._waiting:

            # Check if one of our pending orders has been traded
            traded = False
            to_remove = []

            # Find all our orders still in the market
            current = Order.current()
            current_orders = []
            for order in current.values():
                if order.mine:
                    # self.inform("Order {} is still pending".format(order))
                    current_orders.append(int(order.ref))

            # Compare the orders still in the market with the list of pending orders our bot keeps
            if self._pending:
                for order in self._pending.values():

                    # If already traded, we need to remove from our internal list of pending orders
                    if int(order.ref) not in current_orders:
                        traded = True
                        to_remove.append(self._securities[order.market])
                        
            # Update our internal list of pending orders
            for security in to_remove:
                self._pending.pop(security, None)

            # Check if our order is optimal
            optimal, potential = self.is_portfolio_optimal()

            # If one of our orders has (partially) traded or our portfolio is not optimal, send out the potential orders
            if traded or not optimal:            
                for (potential, security, order) in potential:
                    if self._validate_order(order):
                        self._manage_pending_orders(order)


    def received_session_info(self, session: Session):
        """
        Initialise the bot at the start of each session
        """
        self.initialised()


    def pre_start_tasks(self):
        """
        Set the bot to execute the switch function every half minute
        """
        self.execute_periodically(self._switch, HALF_MINUTE)


    def received_holdings(self, holdings):
        pass
        

    def _switch(self):
        """
        Switches the bot type between Reactive and Market Maker according to the remaining time. 
        Bot is set as Reactive during the first and last section of the session as the activity is highest then.
        Bot is set as Market Maker during the rest of the sections
        """

        # Update remaining time in the session
        (session, remaining) = self._remaining_time
        remaining -= HALF_MINUTE
        self._remaining_time = (session, remaining)

        # Set the bot type according to the time
        section = session/SECTION

        if remaining <= section or (session - remaining) <= section:
            self._bot_type = BotType.MARKET_MAKER
        else:
            self._bot_type = BotType.MARKET_MAKER
            

    def _get_covariances(self):
        """
        Get the covariances for each pair of stocks.

        :return covariances: Dictionary of covariances for each pair in the form {(Security_X, Security_Y): Cov_XY}
        """

        # Create covariance matrix
        matrix = np.array(list(self._payoffs.values()))
        matrix = np.cov(matrix, bias=True)

        # Index the matrix to get the covariance for the respective pairs
        index = {}
        securities = np.array(list(self._payoffs.keys()))
    
        for i in range(len(securities)):
            index[securities[i]] = i

        # List out the pairings
        pairs = combinations(securities, PAIRS)
        covariances = {}

        for (first, second) in pairs:
            covariances[(first, second)] = matrix[index[first]][index[second]]

        return covariances


    def _get_holdings(self, available):
        """
        Get the current (available) holdings.

        :param available: Boolean to determine if we are to return settled or available holdings
        :return holdings: Dictionary of current holdings
        """

        holdings = {}
        
        if available:
            holdings["Cash"] = self.holdings.cash_available/CENTS_TO_DOLLAR

            for asset in self.holdings.assets.values():
                holdings[asset.market.item] = asset.units_available

        else:
            holdings["Cash"] = self.holdings.cash/CENTS_TO_DOLLAR

            for asset in self.holdings.assets.values():
                holdings[asset.market.item] = asset.units

        return holdings

    
    def _get_holdings_after_trade(self, orders):
        """
        Get the potential holdings if the orders were to be sent out and traded.

        :param orders: List of orders to simulate 
        :return holdings: Dictionary of potential holdings
        """

        holdings = self._get_holdings(False)

        for order in orders:
            security = self._securities[order.market]
            amount = order.price/CENTS_TO_DOLLAR * order.units

            if order.order_side == OrderSide.BUY:
                holdings[security] += order.units
                holdings["Cash"] -= amount

            else:
                holdings[security] -= order.units
                holdings["Cash"] += amount

        return holdings


    def _get_best_prices(self):
        """
        Get the best bid and best ask of each security. 
        Best bid (ask) will be set as EMPTY if no buy (sell) orders in the market.

        :return prices: Dictionary of tuples in the form {security: (best_bid, best_ask)} 
        """

        prices = {}

        for security in self._securities.values():
            prices[security] = (EMPTY, EMPTY)

        # Iterate through the current orders in the market for each security
        current_orders = Order.current()

        for order in current_orders.values():
            security = self._securities[order.market]
            (bid, ask) = prices[security]

            # Compare with the best bid/ask we already found or set a new one
            if order.order_side == OrderSide.BUY:
                if bid == EMPTY or bid < order.price:
                    prices[security] = (order.price, ask)

            else:
                if ask == EMPTY or ask > order.price:
                    prices[security] = (bid, order.price)

        return prices

    
    def _create_order(self, security):
        """
        Creates an order with the basic skeleton.

        :param security: Security of which the order is for 
        :return order: Order for the security 
        """

        order = Order.create_new()
        order.units = MAX_UNITS
        order.order_type = OrderType.LIMIT
        order.market = Market(self._market_ids[security])
    
        return order      


    def _cancel_order(self, order):
        """
        Sends out a cancel order to cancel a pending order.

        :param order: Order to be cancelled
        """

        # Copy the order details
        cancelled = copy.copy(order)
        cancelled.order_type = OrderType.CANCEL

        # Validate the cancel order
        if self._validate_order(cancelled):

            # Not adding to self._waiting as it's either accepted and cancelled or rejected as it's already consumed
            security = self._securities[cancelled.market]
            self._pending.pop(security, None)
            self.send_order(cancelled)


    def _validate_order(self, order):
        """
        Validates an order by checking the following conditions:
        - Only cancel an order that belongs to us and has not yet been consumed
        - Price follows market price tick
        - Price is not above (below) the max (min) bid (ask)
        - Sends out an order only once
        - Doesn't send out an order that resuls in a lower performance than what is already pending in the same market
        - We have enough units available to sell/short
        - We have enough cash to buy the stock

        :param order: Order to be validated against the above requirements 
        :return validation: True if order satisfies all the requirements 
        """

        # Check if bot is cancelling an order that belongs to us and has not yet been consumed
        if order.order_type == OrderType.CANCEL:
            return (order.mine and not order.is_consumed)

        else:
            # Ensure price follows price tick
            market = order.market
            if (order.price % market.price_tick != IS_MULTIPLE):
                # self.inform("Price of {} is not a multiple of price tick {}".format(order.price, market.price_tick))
                return False
            
            # Ensure price is not above (below) the max (min) bid (ask)
            if (order.price not in range(market.min_price, market.max_price)):
                # self.inform("Price of {} is not in [{},{}]".format(order.price, market.min_price, market.max_price))
                return False

            # Ensure bot is not sending an order twice
            security = self._securities[market]
            if security in self._pending:
                pending = self._pending[security]
                if pending.price == order.price and pending.order_side == order.order_side:
                    return False

            # Ensure bot is not sending an order that reduces performance
            current = self.get_potential_performance([])
            potential = self.get_potential_performance([order])

            if current >= potential:
                # self.inform("Potential performance is lower than current.")
                return False

            # Ensure we have enough units available to sell/short
            available = self._get_holdings(True)

            if order.order_side == OrderSide.SELL:
                if available[security] < order.units:
                    # self.inform("Not enough units for {} available.".format(security))
                    return False
                
            # Ensure we have enough cash available to buy the stock
            else:    
                if available["Cash"] < order.price/CENTS_TO_DOLLAR:
                    # self.inform("Not enough cash for {} available.".format(security))
                    return False
            
            return True


    def _manage_pending_orders(self, order):
        """
        Sends out an order and manages the pending orders.

        :param order: Order (already validated) to be sent out
        """
        
        security = self._securities[order.market]
        price = order.price

        # There is a pending order within the market 
        if security in self._pending:
            pending = self._pending[security]

            # Check if the price is better than the pending order
            if pending.order_side == order.order_side:
                if order.order_side == OrderSide.BUY and pending.price not in range(price - STALE_RANGE, price):
                    self._cancel_order(pending)

                elif order.order_side == OrderSide.SELL and pending.price not in range(price, price + STALE_RANGE):
                    self._cancel_order(pending)

            else:
                self._cancel_order(pending)

        # If cancelled or not in pending anymore, send order and wait for order to be accepted/rejected
        if security not in self._pending:
            self._orders_sent += ORDERS_SENT
            order.ref = self._orders_sent
            self._waiting[security] = order
            self._pending[security] = order
            self.send_order(order)


    def _market_maker(self, base_order, order_side):
        """
        Creates a market maker order by setting the price as the decrease in performance (in dollars) if we were to 
        send out the worst order (minimum sell or maximum buy – e.g. $0 sell, $10 buy) with a profit margin equal to 
        the variance of the security or MIN_PROFIT_MARGIN if it's a Risk Free Asset (Notes)

        :param base_order: Simulated order
        :param order_side: OrderSide of the order to simulate 
        :return mm_potential: Performance of the correctly-priced market maker order
        :return order: The market maker order
        """

        # Get the profit margin
        
        order = copy.copy(base_order)
        security = self._securities[order.market]
        profit_margin = max(MIN_PROFIT_MARGIN, int(round(self._variances[security])))
        
        # Create the simulated market maker order
        mm = self._create_order(security)
        mm.order_side = order_side

        # Get the current performance
        current_performance = self.get_potential_performance([])   

        # Get the potential performance of the simulated worst market maker order
        if order_side == OrderSide.SELL:
            mm.price = order.market.min_price
            mm_potential = self.get_potential_performance([mm])

            difference = (current_performance - mm_potential) * CENTS_TO_DOLLAR
            mm.price = int(difference) + profit_margin

        else:
           
            mm.price = order.market.max_price
            mm_potential = self.get_potential_performance([mm])
            difference = (current_performance - mm_potential) * CENTS_TO_DOLLAR
            mm.price = int(difference) - profit_margin

        # Set the decrease in performance as the price and return the performance of the newly priced market maker order
        mm.price -= mm.price % order.market.price_tick
        order.price = mm.price
        mm_potential = self.get_potential_performance([order])

        return mm_potential, order


if __name__ == "__main__":
    FM_ACCOUNT = "ardent-founder"
    FM_EMAIL = "chanjie.ho@student.unimelb.edu.au"
    FM_PASSWORD = "961948"
    MARKETPLACE_ID = 1054  # replace this with the marketplace id
    initial_bot_type = BotType.REACTIVE

    bot = CAPMBot(FM_ACCOUNT, FM_EMAIL, FM_PASSWORD, MARKETPLACE_ID, initial_bot_type)
    bot.run()

