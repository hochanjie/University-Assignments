"""
This is a template algorithm on Quantopian for you to adapt and fill in.
"""
# from quantopian.pipeline.factors import AverageDollarVolume,SimpleMovingAverage
from quantopian.algorithm import attach_pipeline, pipeline_output
from quantopian.pipeline import Pipeline
import quantopian.pipeline.filters as Filters
import quantopian.pipeline.factors as Factors
from quantopian.pipeline.factors import DailyReturns
import pandas as pd
import numpy as np
from quantopian.pipeline.data.builtin import USEquityPricing
from quantopian.pipeline.factors import CustomFactor
import statsmodels.api as sm


def initialize(context):
    # Consumer Staples (essential g&s) Select Sector SPDR ETF (TICKER: XLP)
    # Consumer Discretionary (non-essential g&s) Select Sector SPDR ETF (TICKER: XLY)
    # Energy Select Sector SPDR ETF (TICKER: XLE)
    # Financials Select Sector SPDR ETF (TICKER: XLF)
    # Health Care Select Sector SPDR ETF (TICKER: XLV)
    # Industrials Select Sector SPDR ETF (TICKER: XLI)
    # Materials Select Sector SPDR ETF (TICKER: XLB)
    # Technology Select Sector SPDR ETF (TICKER: XLK)
    # Utilities Select Sector SPDR ETF (TICKER: XLU)
    # 2003-12-25
    context.portfolio_30_day_returns = []
    # etf.com and etfdb.com include factsheets, can use for current stats and reasoning
    # see above for sectors ^^^
    # Overall, can see that this strategy rotates around all sectors => depends on estimations, includes sectors that are doing well and decrease sectors not doing well
    # favours larger caps
    # average-high MSCI ESG Fund Rating for most (except XLE enever LOLLL), (A ~6.5 - average management of ESG issues, "Highly rated funds consist of companies that tend to show strong and/or improving management of financially relevant environmental, social and governance issues. These companies may be more resilient to disruptions arising from ESG events.")
    context.spy = sid(
        8554)  # SPY, includes all sectors, however favours Info Tech (~27% in 2020), "highly liquid, vanilla, offers exposure to the US large-cap space, attracts long-term investors"
    context.shy = sid(
        23921)  # SHY, "The iShares 1-3 Year Treasury Bond ETF - track the investment results of an index composed of U.S. Treasury bonds with remaining maturities b/n 1 & 3 yrs, safe when high vol, but lower yield"
    context.stock = [sid(19659),
                     # XLP, "may perform well during a downturn, offers impressive liquidity, cost efficiency, and depth of exposure"
                     sid(19662),
                     # XLY, "performs well during recovery (opposite-ish of XLP), like above, liquid, cost efficient and exposure"
                     sid(19655),
                     # XLE, "includes large oil producers, usually not a long-term buy and hold, only when oil prices thrive, maintains some concentration issues, as a few stocks account for big chunks of the total portfolio, not doing so well right now"
                     sid(19656),
                     # XLF, "concentrated in large banks (you know who they are), highly liquid, ESG rating a bit lower ~5"
                     sid(19661),
                     # XLV, LOLL of course they have 6.66 ESG rating for Healthcare!!!, lower risk, doing fairly ok right now (for obvious reasons)"
                     sid(19657),
                     # XLI, "transportation firms, providers of commercial and professional services, and manufacturers of capital goods / concentrated in bigger caps, liquid so is used for rotation strat"
                     sid(19654),
                     # XLB, " indirect exposure to commodity prices through the stocks of companies engaged in the extraction or production of natural resources. Because the materials sector often accounts for a small portion of broad-based benchmarks, XLB may be a useful tool for long-term investors looking for more balanced exposure to the U.S. equity market. It can also be handy for those looking to implement a shorter-term tilt towards the materials sector"
                     sid(19658),
                     # XLK, it's tech, we're all sick of answering why tech, so you can review your interview notes for reasons why
                     sid(
                         19660)]  # XLU, " The fund dominates its segment, with huge assets and volume, but it's also more concentrated in a handful of large firms."

    # =================================================

    # Rebalance first day of every month, 1 hour after market open.
    schedule_function(record_day_start_port_value, date_rules.every_day(), time_rules.market_open())
    # ===========================

    # initial portfolio done dynamically:
    # context.portfolio_30_day_returns = data.history()......

    schedule_function(rebalance, date_rules.month_start(), time_rules.market_open(hours=1))
    schedule_function(close, date_rules.month_start(), time_rules.market_open())

    # Record tracking variables at the end of each day.
    schedule_function(count_positions, date_rules.every_day(), time_rules.market_close())
    # schedule_function(trail_stop,date_rules.every_day(),time_rules.market_open())

    # ===================================
    # Adjust array of daily returns of our portfolio to maintain a 30-day window

    schedule_function(record_day_end_port_value, date_rules.every_day(), time_rules.market_close())

    # ===================================
    # Create our dynamic stock selector.
    attach_pipeline(make_pipeline(context), 'myPipe')


def record_day_start_port_value(context, data):
    # reord the portfolio value at the start of the day
    context.day_start_port_val = context.portfolio.portfolio_value


def record_day_end_port_value(context, data):
    # record portfolio value at the end of the day
    context.day_end_port_val = context.portfolio.portfolio_value

    daily_return = (context.day_end_port_val - context.day_start_port_val) / context.day_start_port_val

    if len(context.portfolio_30_day_returns) == 30:
        # adjust the array of portfolio daily returns
        context.portfolio_30_day_returns = context.portfolio_30_day_returns[1:] + [daily_return]
    elif len(context.portfolio_30_day_returns) < 30:
        context.portfolio_30_day_returns.append(daily_return)


def make_pipeline(context):
    """
    A function to create our dynamic stock selector (pipeline). Documentation on
    pipeline can be found here: https://www.quantopian.com/help#pipeline-title
    """

    universe = Filters.StaticAssets(context.stock)

    # Factor of yesterday's close price.
    day20_ret = Factors.Returns(inputs=[USEquityPricing.close], window_length=21, mask=universe)
    day3mo_ret = Factors.Returns(inputs=[USEquityPricing.close], window_length=63, mask=universe)

    day6mo_ret = Factors.Returns(inputs=[USEquityPricing.close], window_length=126, mask=universe)
    day1yr_ret = Factors.Returns(inputs=[USEquityPricing.close], window_length=252, mask=universe)

    volatility = Factors.AnnualizedVolatility(mask=universe)

    # day360_ret=Factors.Returns(inputs=[USEquityPricing.close], window_length=252, mask=universe)
    # rank: Factor method
    """
    Parameters:
    - methods (in string - ordinal (DEFAULT)/min/max/dense/average) - unsure how this works, but we probably only need default https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.rankdata.html
    - asscending (bool)
    - mask (just normal filtering)
    - groupby
    Returns: "a new factor that computes ranking of the data produced by self", 
    Comment from Peich: tried to log ranked returns an not returning the table, I think the same issue as when we tried printing DailyReturns()
    """
    # day20_rank=day20_ret.rank(ascending=False)
    # day3mo_rank=day3mo_ret.rank(ascending=False)
    # day6mo_rank=day6mo_ret.rank(ascending=False)
    # day1yr_rank=day1yr_ret.rank(ascending=False)
    # vol_rank=volatility.rank(ascending=True)

    WEIGHT1 = 0.20
    WEIGHT2 = 0.30
    WEIGHT4 = 0.10

    score = (WEIGHT1 * day3mo_ret) + (WEIGHT2 * volatility) + (WEIGHT2 * day20_ret) + (WEIGHT4 * day6mo_ret) + (
                WEIGHT4 * day1yr_ret)
    score_rank = score.rank(ascending=False)

    # mo=mo_rank.percentile_between(50,100)
    best = (score_rank <= 2)
    pipe = Pipeline(
        columns={
            'Score': score,
            'Score_Rank': score_rank
            # 'temp_good_alpha': regression
        },
        screen=(best)
    )
    return pipe


def before_trading_start(context, data):
    """
    Called every day before market open.
    """

    # ==============================================
    # records only the regressions where the good-alpha is significant (each day)
    context.regs = {}

    # Retrieve the previous 30 previous trading prices before each trading day
    etf_list = symbols('SPY', 'XLY', 'XLE', 'XLF', 'XLV', 'XLI', 'XLB', 'XLK', 'XLU')

    prices = data.history(etf_list,
                          fields='price',
                          bar_count=31,
                          frequency='1d')

    daily_rets_30_days = (prices.pct_change()).to_dict()

    # log.info(daily_rets_30_days.to_dict())

    for key, val in daily_rets_30_days.items():

        # don't do anything for the first 30 days
        if len(context.portfolio_30_day_returns) < 30:
            break

        # log.info(key)
        temp = sorted(val.items())[1:]  # remove the first day  as pct changes, first = nan

        # this is the 30 day window of data points for daily returns for each security
        security_30daily_returns = [x[-1] for x in temp]
        # log.info(security_30daily_returns)

        # regress the asset on the currently stored 30 datapoints of daily returns of
        #         our portfolio to get the GOOD ALPHA
        y = security_30daily_returns
        x = context.portfolio_30_day_returns
        x2 = sm.add_constant(x)

        est = sm.OLS(y, x2)
        est2 = est.fit()
        # log.info(y)
        # log.info(x)
        # log.info("Params:" + str(est2.params))
        alpha, beta = est2.params
        palpha, pbeta = est2.pvalues

        if (palpha < 0.1):
            # log.info("\nGood Alpha: " + str(alpha) +  "\nBeta      : " + str(beta) + "\nPval (alpha): " + str(palpha))
            context.regs[key] = {'alpha': alpha, 'palpha': palpha}

    # ==============================================

    context.output = pipeline_output('myPipe')

    # These are the securities that we are interested in trading each day.
    # print(context.output)

    context.longs = context.output.index.tolist()


# My Friend Blue helped me restructure the weight so I did not double leverage on Safety Months

def close(context, data):
    for stock in context.portfolio.positions:
        if not data.can_trade(stock):
            continue
        order_target(stock, 0)
        log.info("Sell" + str(stock))


def rebalance(context, data):
    # weight = context.output.score/context.output.score.sum()
    # print "score: {}    Weight: {}".format(context.output.score, weight)
    # leverage=1.0
    spy_200 = data.history(context.spy, "price", 200, "1d")
    spy_mavg = spy_200.mean()
    spy_price = data.current(context.spy, "price")
    if data.can_trade(context.shy) and spy_price < spy_mavg:
        # Do a check to make sure no positions before this 1.0 all-in
        order_target_percent(context.shy, 1.0)
        log.info("Safety Month")
        return

    # context.longs is the array of stocks to be bought
    # log.info("\nTo be bought:{}\nGood Alphas: {}".format(context.longs, context.regs))
    # log.info("\nTo be bought:{}\nGood Alphas: {}".format(context.longs, context.regs))

    # ========================================

    for stock in context.longs:

        if not data.can_trade(stock):
            continue
        if get_open_orders(stock):
            continue

        order_target_percent(stock, 1.0 / len(context.longs))
        # order_target_value(stock, context.portfolio.cash / len(context.longs))
        log.info("Buy" + str(stock))

    '''
    *****************************************************
    '''
    # if alpha is negative, reduce weight in this security
    for sec in context.portfolio.positions.keys():
        if sec in context.regs:
            if context.regs[sec]['alpha'] < 0:
                log.info(
                    "Negative alpha, reducing weight=========================================================================")
                position_size = context.portfolio.positions[sec].amount

                order(sec, -0.15 * position_size)
                log.info("Good-alpha sell " + str(sec))

    if context.portfolio.cash < 0:
        for sec in context.portfolio.positions:
            order_target_percent(sec, 0.8 / len(context.portfolio.positions.keys()))
        return

    for sec in context.regs:
        if context.regs[sec]['alpha'] > 0 and sec not in context.portfolio.positions:
            # significant positive alpha, increase weight
            order_target_percent(sec, 0.05)
            log.info("Good-alpha buy " + str(sec))
        elif context.regs[sec]['alpha'] > 0 and sec in context.portfolio.positions:
            current_pos_size = context.portfolio.positions[sec].amount
            current_alloc = current_pos_size / context.portfolio.value
            order_target_percent(sec, current_alloc + 0.05)
    '''
    *****************************************************
    '''


def count_positions(context, data):
    longs = 0
    for position in context.portfolio.positions.values():
        if position.amount > 0:
            longs += 1
    # for holdings in context.portfolio.positions.keys():
    # if holdings:
    #     longs += 1

    leverage = context.account.leverage
    # asset=context.portfolio.portfolio_value
    # record(asset=asset)
    record(longs=longs)
    record(leverage=leverage)
    record(cash=context.portfolio.cash)