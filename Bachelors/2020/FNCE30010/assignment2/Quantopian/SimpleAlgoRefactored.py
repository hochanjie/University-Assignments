"""
This is a template algorithm on Quantopian for you to adapt and fill in.
"""
import quantopian.algorithm as algo
from quantopian.pipeline import Pipeline
from quantopian.pipeline.data.builtin import USEquityPricing
from quantopian.pipeline.filters import QTradableStocksUS

# new imports
from quantopian.pipeline.factors import SimpleMovingAverage
from quantopian.pipeline.factors import AverageDollarVolume
import numpy as np

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

    context.bet_size = 0.01

    set_commission(commission.PerDollar(cost=0.0010))


def make_pipeline():
    """
    A function to create our dynamic stock selector (pipeline). Documentation
    on pipeline can be found here:
    https://www.quantopian.com/help#pipeline-title
    """
    # Define factors.
    sma_5 = SimpleMovingAverage(inputs=[USEquityPricing.close], window_length=5)
    sma_25 = SimpleMovingAverage(inputs=[USEquityPricing.close], window_length=25)
    dollar_volume = AverageDollarVolume(window_length=5)

    # Define a filter.
    is_liquid = (dollar_volume > 100000)

    # Base universe set to the QTradableStocksUS
    base_universe = QTradableStocksUS()

    # Factor of yesterday's close price.
    yesterday_close = USEquityPricing.close.latest

    pipe = Pipeline(
        columns={
            'close': yesterday_close,
             'sma_5': sma_5,
             'sma_25': sma_25,
             'long': sma_5 > sma_25
        },
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
    longs =  context.output[context.output['long']]

    if len(longs) > 10:
        selected_buy = np.random.choice(longs.index, 10, replace=False)
        log.info("Select from {}".format(selected_buy))
    else:
        selected_buy = longs.index

    cash_remaining = context.portfolio.cash
    cash_required = context.bet_size * len(selected_buy) * context.portfolio.starting_cash

    # Either we trade all selected or nothing at all and use cash for future trades
    if cash_remaining > cash_required:
        context.longs = selected_buy
    else:
        context.longs = []

    shorts = set(context.output[context.output['long']==False].index)
    positions = set(context.portfolio.positions.keys())
    context.to_sell = shorts.intersection(positions)




def rebalance(context, data):
    """
    Execute orders according to our schedule_function() timing.
    """
    for i in context.longs:
            order_price = context.portfolio.starting_cash * context.bet_size
            order_id = order_value(i, order_price)

    for i in context.to_sell:
            position_size = context.portfolio.positions[i].amount
            order_id = order(i, -1 * position_size)
            #log.info("Closed on {}: {}".format(i, order_id))

def record_vars(context, data):
    """
    Plot variables at the end of each day.
    """
    record("Cash", context.portfolio.cash/1000)
    record("Positions", len(context.portfolio.positions))


def handle_data(context, data):
    """
    Called every minute.
    """
    pass