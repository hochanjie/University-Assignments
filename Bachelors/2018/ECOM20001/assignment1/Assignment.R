# Set working directory using setwd() function

setwd("~/Downloads/ECOM20001/Assignment/Assignment 1")


# Read data from a csv file using read.csv() function

mydata=read.csv(file = "billionaires_clean.csv")


# Create variables for each table attribute simplify script

country = mydata$country
numbil0 = mydata$numbil0
open = mydata$open
gattwto = mydata$gattwto
gdppc = mydata$gdppc
pop = mydata$pop
roflaw = mydata$roflaw
topint = mydata$topint


# ==========================================================================================================================

# QUESTION 1
# ----------


# Summary statistics for the entire dataset using the summary()
# function and the standard deviation using the sapply( ,sd) function

summary(mydata)
sapply(mydata,sd)


# Summary statistics for all attributes of countries that are closed (open == 0) to trade and open (open == 1) to trade

summary(mydata[open == 0,])
sapply(mydata[open == 0,],sd)
summary(mydata[open == 1,])
sapply(mydata[open == 1,],sd)


# ==========================================================================================================================

# QUESTION 2
# ----------


# Density function of the number of billionaires in a country with a main title (assigner to 'main') and axes labels
# (assigned to 'xlab' and 'ylab'), where the x-axis is limited to the range of the number of billionaires (assigned to
# 'xlim') with the graph coloured red (assigned to 'col') using the density() function, and plotting it using the plot()
# function into a pdf for easy extraction using the pdf() function at the beginning and dev.off() function at the end

pdf("Density of Billionaires per Country.pdf")
plot(density(numbil0),
     main = "Density of Billionaires per Country",
     xlab = "Number of Billionaires",
     xlim = c(min(numbil0), max(numbil0)),
     ylab = "Density",
     col = "red") 
dev.off()


# Density function of the number of years a country was a GATT or WTO member with a main title and axes labels
# witht the range of the x-axis limited to the range of the number of years a country was a GATT or WTO member

pdf("Density of Years as a Member of GATT or WTO per Country.pdf")
plot(density(gattwto),
     main = "Density of Years Spent as a GATT or WTO Member per Country",
     xlab = "Number of Years",
     xlim = c(min(gattwto), max(gattwto)),
     ylab = "Density",
     col = "blue")
dev.off()


# ==========================================================================================================================

# QUESTION 3
# ----------


# Density function of the number of billionaires in a country closed (open == 0) to trade in red vs
# a country that was open (open == 1) in blue (appended using the lines() function) with a main title,  
# axes labels where the x-axis is limited to the range of the number of billionaires in a country,
# and a legend to help differentiate the graphs at the topright using the legend() function

pdf("Density of Billionaires per Country as per Trade Openness.pdf")
plot(density(numbil0[open == 0]), 
     main = "Density of Billionaires per Country as per Trade Openness",
     xlab = "Number of Billionaires",
     xlim = c(min(numbil0), max(numbil0)),
     col = "red", 
     lty = 1)
lines(density(numbil0[open == 1]), col = "blue", lty = 1)
legend("topright", legend = c("Not open", "Open"), col = c("red", "blue"), lty = c(1,1))
dev.off()


# ==========================================================================================================================

# QUESTION 4
# ----------


# Scatter graph of the number of billionaires in a country against the number of years that
# country was a member of GATT or WTO where the dots are filled in (assigned to 'pch') with 
# a main title, axes labels, and a line of best fit, calculated using a linear model of the
# two variables using the lm() function, appended to the graph using the abline() function

pdf("Distribution of Billionaires per Country based on Years Spent as a GATT or WTO Member.pdf")
best_fit_1 = lm(numbil0 ~ gattwto, data = mydata)
plot(gattwto, numbil0,
     main = "Distribution of Billionaires per Country \nbased on Years Spent as a GATT or WTO Member",
     xlab = "Number of Years",
     ylab = "Number of Billionaires",
     col = "red",
     pch = 16)
abline(best_fit_1, col = "forestgreen", lwd = 2)
dev.off()


# Scatter graph of the number of billionaires in a country closed to trade against the number of years 
# that country was a member of GATT or WTO with a main title, axes labels, and a line of best fit

pdf("Distribution of Billionaires of Countries Closed to Trade based on Years Spent as a GATT or WTO Member.pdf")
best_fit_2 = lm(numbil0[open == 0] ~ gattwto[open == 0], data = mydata)
plot(gattwto[open == 0], numbil0[open == 0],
     main = "Distribution of Billionaires of Countries Closed to Trade \nbased on Years Spent as a GATT or WTO Member",
     xlab = "Number of Years",
     ylab = "Number of Billionaires",
     col = "red",
     pch = 16)
abline(best_fit_2, col = "forestgreen", lwd = 2)
dev.off()


# Scatter graph of the number of billionaires in a country open to trade against the number of years 
# country was a member of GATT or WTO with a main title, axes labels, and a line of best fit

pdf("Distribution of Billionaires of Countries Open to Trade based on Years Spent as a GATT or WTO Member.pdf")
best_fit_3 = lm(numbil0[open == 1] ~ gattwto[open == 1], data = mydata)
plot(gattwto[open == 1], numbil0[open == 1],
     main = "Distribution of Billionaires of Countries Open to Trade \nbased on Years Spent as a GATT or WTO Member",
     xlab = "Number of Years",
     ylab = "Number of Billionaires",
     col = "red",
     pch = 16)
abline(best_fit_3, col = "forestgreen", lwd = 2)
dev.off()


# ==========================================================================================================================

# QUESTION 5
# ----------


# Two-sample t-test for the null that the mean number of billionaires in countries open to trade (open == 1) is
# equal to the mean number of billionaires in countries closed to trade (open == 0) using the t.test() function

t.test(numbil0[open == 1], numbil0[open == 0])


# ==========================================================================================================================

# QUESTION 6
# ----------

# Single linear regression model of the number of billionaires in a country (regressand) 
# based on the number of years the country was a member of GATT or WTO (regressor)

reg1 = lm(numbil0 ~ gattwto, data = mydata)


# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Number of years as a member of GATT or WTO (gattwto)
# • Rule of law index (roflaw)

reg2 = lm(numbil0 ~ gattwto + roflaw, data = mydata)


# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Number of years as a member of GATT or WTO (gattwto)
# • Rule of law index (roflaw)
# • Top marginal income tax rate (topint)

reg3 = lm(numbil0 ~ gattwto + roflaw + topint, data = mydata)


# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Number of years as a member of GATT or WTO (gattwto)
# • Rule of law index (roflaw)
# • Top marginal income tax rate (topint)
# • Population (pop)

reg4 = lm(numbil0 ~ gattwto + roflaw + topint + pop, data = mydata)


# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Number of years as a member of GATT or WTO (gattwto)
# • Rule of law index (roflaw)
# • Top marginal income tax rate (topint)
# • Population (pop)
# • GDP per capita (gdppc)

reg5 = lm(numbil0 ~ gattwto + roflaw + topint + pop + gdppc, data = mydata)


# Footnote to explain the table of reports (below)

custom_note = "Dependent variable is numbil0, the number of billionaires in the country.
Homoscedastic standard errors in parentheses.
Statistical significance from two-sided tests of the null of no effect marked as * for 5% and ** for 1%"


# Install the texreg package as to use the screenreg function (below) using the library() function

library("texreg", lib.loc="/Library/Frameworks/R.framework/Versions/3.5/Resources/library")


# Print the coefficients of all the linear regression models (compiled in a list using the list() function) in 
# a table form using the texreg() function where each column is numbered from (1) through (5) (assigned to
# 'custom.model.names') as to allow comparison between linear regression models as to help determine omitted
# variable biasness as well as performing two-sided t-tests on the coeffients at a 5% and 1% significance level
# using stars to denote the rejection of null of the coefficient being equal to 0 using stars (assigned to 'stars'), 
# reporting the goodness of fit statistics at the bottom such as the number of observations, adjusted R^2, and 
# the regression F-statistic (assigned to 'include.fstatistic') while not including the rmse (assigned to 'include.rmse')
# and the R^2 (assigned to 'include.rsquared') with the footnote at the bottom of the table (assigned to 'custom.note')
# and the table title (assigned to 'caption' and 'caption.above') and put them all in a file (assigned to 'file')
 
htmlreg(list(reg1, reg2, reg3, reg4, reg5), 
        file = "Regressions.doc",
        caption = "Billionaires and country trade openness",
        caption.above = TRUE,
        stars = c(0.01, 0.05),
        custom.note = custom_note,
        include.rsquared = FALSE,
        include.rmse = FALSE,
        include.fstatistic = TRUE,
        custom.model.names = c("(1)", "(2)", "(3)", "(4)","(5)"))


# ==========================================================================================================================
