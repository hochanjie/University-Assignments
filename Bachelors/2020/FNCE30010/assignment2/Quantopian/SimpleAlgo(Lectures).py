"""
Underlying backtesting engine is OPEN SOURCE! called - zipline


Now this full backtest results actually look really good! but why??
Once we get the fullbacktest results, we can then continue on to use jupyter notebooks to do the 
analysis

Need notebooks before and also after the backtest!

can do this entire process in the notebook as well, but we get a multi index dataframe instead
of a single index like we get here, because here we get a new df everyday for that day

the last bit of the URL after the slash of the backtest we've run here, we can grab and take it\
over to the notebook into the "get_backtest" function to retrieve all the data as a string

returns the backtest as an object

"""
import quantopian.algorithm as algo
from quantopian.pipeline import Pipeline
from quantopian.pipeline.data.builtin import USEquityPricing
from quantopian.pipeline.filters import QTradableStocksUS

# factor import
from quantopian.pipeline.factors import AverageDollarVolume
from quantopian.pipeline.factors import SimpleMovingAverage

'''
- defines the intial params of the strategy
- schedule function
    - what function to run and when 
- multiple schedule functions are run in the order
    that they are defined in the initialise function
    
    this example is about simple moving averages (hello world of quant stuff)
    
-DISCRETE TIME STEP SYSTEM
    in the first time step, orders to be placed are only collected, and in the next
    time step, these orders are placed
    so logically code seems okay, but results in massive negative cash if not fixed
    as it is fixed below
    
    so to fix, in timestep t, account for the orders that are going to be placed in timestep t
    before calculating the orders in timestep t, which will be placed in t+1
    
    
    anything that needs to be passed around to functions should be attached to the context object
    DON'T USE ANY GLOBAL VARS
    
'''
def initialize(context):
    """
    Called once at the start of the algorithm.
    """
    # Rebalance every day, 1 hour after market open.
    algo.schedule_function(
        rebalance,
        algo.date_rules.every_day(),
        algo.time_rules.market_open(hours=1),
    )

    # Record tracking variables at the end of each day.
    algo.schedule_function(
        record_vars,
        algo.date_rules.every_day(),
        algo.time_rules.market_close(),
    )

    # Create our dynamic stock selector.
    algo.attach_pipeline(make_pipeline(), 'pipeline')
    
    # can define own vars in the context object too
    context.max_position = 0.05


'''
- pipeline - what data should the strategy be run on?
- ONLY DEFINES THE UNIVERSE, passed to the algorithm outside this function
    - use CONTEXT object
    - can define own settings, and get data

'''
def make_pipeline():
    """
    A function to create our dynamic stock selector (pipeline). Documentation
    on pipeline can be found here:
    https://www.quantopian.com/help#pipeline-title
    """

    # Base universe set to the QTradableStocksUS
    base_universe = QTradableStocksUS()
    
    yesterday_close = USEquityPricing.close.latest
    
    # let's say we are interested in volume, and we don't wanna trade stonks more than a certain %
    # of the daily volume
    
    # so we can build a factor (takes raw data as input, perform some computations on it)
    # some of these factors are built in, and for that we need the imports above

    # Factor of yesterday's close price.
    # specific factor class object that can be passed into the pipeline, can make own factors
    # using the CustomFactor class too
    
    #  this allows us to build these filters and attach them to the pipeline
    dollar_volume = AverageDollarVolume(window_length = 5)
    is_liquid = (dollar_volume > 100000)
    
    # simple moving averages example
    # we can ADD this new data to the pipeline
    sma_5 = SimpleMovingAverage(inputs = [USEquityPricing.close], window_length = 5)
    sma_25 = SimpleMovingAverage(inputs = [USEquityPricing.close], window_length = 25)

    pipe = Pipeline(
        columns={
            'close': yesterday_close,
            'sma_5': sma_5,
            'sma_25': sma_25
        },
        
        #WE CAN THEN ADD THE FILTER WE MADE HERE TO BE HANDLED BY THE PIPELINE ITSELF
        screen=(base_universe & is_liquid)
    )
    return pipe




def before_trading_start(context, data):
    """
    Called every day before market open.
    """
    context.output = algo.pipeline_output('pipeline')

    # These are the securities that we are interested in trading each day.
    context.security_list = context.output.index


    '''
    This is where we access the data, and based on what we have accessed, we trade
    in this simpleAlgo, trade securities when short moving average passes the long moving avg
    
    context.output has access to the pipeline that we created
    context.output -> underlying object from pandas
    all pandas standard library functions can be called on this data
    
    '''
    
def rebalance(context, data):
    """
    Execute orders according to our schedule_function() timing.
    function run each day as defined above in scheduling
    
    - in this code, we are mixing two things, making the decision of what should
        be bought and what should be sold, and also placing the actual orders
    - our strategy does not depend on what is happening during the day
    - our strategy should be deciding before the day starts whether we should buy or sell
        and the when the next trading day starts we should buy
        
    - in the portfolio management sense, decision of what to buy/sell is different from the 
        decision of actually executing the orders, ideally both won't be mixed
    - on quantopian they mix the two steps usually (other people's strats), they mix essentially
        actions happening at different timesteps
        
    - part of the strategy should be refactored to before trading starts function
    
    - we will need the refactored version in our own strategy that nitin has uploaded, 
        because given our strats the decision to buy / sell would be taken before the trading day
        actually begins
    - this can be prioritised way later tho, however this approach does provide greater flexibility
        such as eg. deciding need to  uy 30 units, only have cash for 15, so only place the order for
        15 instead of having like a partially filled order
        
        also consider, in our code it just takes the first thing that appears in the list and 
        places an order on it. This may not be what we want exactly. what if a stock that is considered
        later in the loop which provides a greater benefit?
    
    
    """
    # this var used to account for cash in this disccrete time step, as
        # orders are decided in timestep t, but only submitted in timestep t+1
    cash_remaining = context.portfolio.cash
    
    for sec in context.security_list:
        sma_5 = context.output.at[sec, 'sma_5']
        sma_25 = context.output.at[sec, 'sma_25']
       
        
        # LONGS
        if sma_5 > sma_25:
            # MAKE SURE TO CHECK FOR REMAINING CASH HERE
            
            # portfolio attribute tracks the holdings like in fmclient
            order_price = context.portfolio.starting_cash * context.max_position
            
            if order_price < cash_remaining:
            
                # in quantopian, can place orders as %of portfolio, % of cash, $ amount etc,
                # we are using the $ amound
                # placing an order like this returns a reference to this order, so these can be tracked
                order_value(sec, order_price)
                cash_remaining -= order_price
            
       # SHORTS
       # this object automatically updates if we have a position in a particular stonk
        elif sec in context.portfolio.positions.keys():
            position_size = context.portfolio.positions[sec].amount
            # new way to place the order (by units)
            order(sec, -1 * position_size)
            

# scheduled to run at the close of each day
def record_vars(context, data):
    """
    Plot variables at the end of each day.
    """
    record("Positions", len(context.portfolio.positions))
    record("Cash", context.portfolio.cash)
    


def handle_data(context, data):
    """
    Called every minute.
    """
    pass