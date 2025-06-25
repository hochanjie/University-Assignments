# Set working directory using setwd() function

setwd("~/Downloads/ECOM20001/Assignment/Assignment 2")


# Read data from a csv file using read.csv() function

mydata=read.csv(file = "billionaires_clean2.csv")


# Create variables for each table attribute simplify script

country = mydata$country
year = mydata$year
numbil0 = mydata$numbil0
natrent = mydata$natrent
pop = mydata$pop
gdppc = mydata$gdppc
roflaw = mydata$roflaw


# ==========================================================================================================================

# QUESTION 1
# ----------


# Summary statistics and standard deviation for numbil0, natrent, pop, gdppc, roflaw using the summary() and sd() functions

summary(numbil0)
sd(numbil0)

summary(natrent)
sd(natrent)

summary(pop)
sd(pop)

summary(gdppc)
sd(gdppc)

summary(roflaw)
sd(roflaw)


# Scale natrent as 1 unit increase be increase of $10 billlion

mydata$natrent = natrent / 10000000000
natrent = mydata$natrent

# Scale pop as 1 unit increase be increase of 10 million people

mydata$pop = pop / 10000000
pop = mydata$pop

# ==========================================================================================================================

# QUESTION 2
# ----------


library(ggplot2)

# Scatter plot of numbil0 against natrent

pdf("numbil0 vs natrent.pdf")
ggplot(mydata, aes(y=numbil0, x=natrent)) +                             # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between numbil0 and natrent") +                 # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# Scatter plot of numbil0 against pop

pdf("numbil0 vs pop.pdf")
ggplot(mydata, aes(y=numbil0, x=pop)) +                                 # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between numbil0 and pop") +                     # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# Scatter plot of natrent against pop

pdf("natrent vs pop.pdf")
ggplot(mydata, aes(y=natrent, x=pop)) +                                 # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between natrent and pop") +                     # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# ==========================================================================================================================

# QUESTION 3
# ----------

# Scatter plot of numbil0 against gdppc

pdf("numbil0 vs gdppc.pdf")
ggplot(mydata, aes(y=numbil0, x=gdppc)) +                               # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between numbil0 and gdppc") +                   # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# Scatter plot of numbil0 against roflaw

pdf("numbil0 vs roflaw.pdf")
ggplot(mydata, aes(y=numbil0, x=roflaw)) +                              # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between numbil0 and roflaw") +                  # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# Scatter plot of gdppc against roflaw

pdf("gdppc vs roflaw.pdf")
ggplot(mydata, aes(y=gdppc, x=roflaw)) +                                # Define the dataset, x and y variables for scatter plot
  geom_point(alpha = .3) +                                              # Allow for shading of the points in the scatter plot to help visualisation
  stat_smooth(method = "lm", formula = y ~ poly(x,1), col="blue") +     # Fit a polynomial of DEGREE 2 (QUADRATIC)
  ggtitle("Relationship Between gdppc and roflaw") +                    # Scatter plot title
  theme(plot.title = element_text(hjust = 0.5))                         # Center the scatter plot title
dev.off()


# ==========================================================================================================================

# QUESTION 4
# ----------

# Make dummy variables for each of the year from 2005 to 2013 so we can
# avoid the dummy variable trap by making (omitting) 2005 as a base year

d2005=as.numeric(mydata$year==2005)
d2006=as.numeric(mydata$year==2006)
d2007=as.numeric(mydata$year==2007)
d2008=as.numeric(mydata$year==2008)
d2009=as.numeric(mydata$year==2009)
d2010=as.numeric(mydata$year==2010)
d2011=as.numeric(mydata$year==2011)
d2012=as.numeric(mydata$year==2012)
d2013=as.numeric(mydata$year==2013)


# Single linear regression model of the number of billionaires in a country (regressand) based on 
# the total natural resources rent (regressor) and save the heteroskedastic-robust standard errors

reg1 = lm(numbil0 ~ natrent, data = mydata)
ct1 = coeftest(reg1, vcov = vcovHC(reg1, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Total natural resources rent (natrent)
# • Population (pop)

reg2 = lm(numbil0 ~ natrent + pop, data = mydata)
ct2 = coeftest(reg2, vcov = vcovHC(reg2, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Total natural resources rent (natrent)
# • Population (pop)
# • GDP per capita (gdppc)

reg3 = lm(numbil0 ~ natrent + pop + gdppc, data = mydata)
ct3 = coeftest(reg3, vcov = vcovHC(reg3, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Total natural resources rent (natrent)
# • Population (pop)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)

reg4 = lm(numbil0 ~ natrent + pop + gdppc + roflaw, data = mydata)
ct4 = coeftest(reg4, vcov = vcovHC(reg4, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Total natural resources rent (natrent)
# • Population (pop)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)
# • Year dummy variables (d20xx)

reg5 = lm(numbil0 ~ natrent + pop + gdppc + roflaw + d2006 + d2007 + d2008 + d2009 + d2010 + d2011 + d2012 + d2013, data = mydata)
ct5 = coeftest(reg5, vcov = vcovHC(reg5, "HC1")) 

# Footnote to explain the table of reports (below)

custom_note = "Dependent variable is the number of billionaires in the country. 
natrent is natural resources rents in billions of dollars. 
pop is total population of a country in millions. 
gdppc is GDP per capita in thousands of dollars. 
roflaw is an index between 0 and 1 on the level of the rule of law in a country. 
dXXXX is a dummy variable for the year XXXX. 
Heteroskedasticity robust standard errors in parentheses.
Statistical significance from two-sided tests of the null of no effect marked as * for 5% and ** for 1%"

library(texreg)

# Print the coefficients of all the linear regression models (compiled in a list using the list() function) in 
# a table form using the texreg() function where each column is numbered from (1) through (5) (assigned to
# 'custom.model.names') as to allow comparison between linear regression models as to help determine omitted
# variable biasness as well as performing two-sided t-tests on the coeffients at a 5% and 1% significance level
# using stars to denote the rejection of null of the coefficient being equal to 0 using stars (assigned to 'stars'), 
# reporting the goodness of fit statistics at the bottom such as the number of observations, adjusted R^2, and 
# the regression F-statistic (assigned to 'include.fstatistic') while not including the rmse (assigned to 'include.rmse')
# and the R^2 (assigned to 'include.rsquared') with the footnote at the bottom of the table (assigned to 'custom.note')
# and the table title (assigned to 'caption' and 'caption.above') and put them all in a file (assigned to 'file')
# then override the homoskedastic standard errors and p-values with the heteroskedastic-robust standard errors (assigned to
# 'override.se' and 'override.pvalues')


htmlreg(list(reg1, reg2, reg3, reg4, reg5), 
        file = "Billionaires and country characteristics.doc",
        caption = "Billionaires and country characteristics",
        caption.above = TRUE,
        stars = c(0.01, 0.05),
        custom.note = custom_note,
        include.rsquared = FALSE,
        include.rmse = FALSE,
        include.fstatistic=TRUE,
        override.se = list(ct1[,2], ct2[,2], ct3[,2], ct4[,2], ct5[,2]),
        override.pvalues = list(ct1[,4], ct2[,4], ct3[,4], ct4[,4], ct5[,4]),
        reorder.coef = c(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1),
        custom.model.names = c("(1)", "(2)", "(3)", "(4)","(5)"),
        custom.coef.names = c("Constant", "natrent", "pop", "gdppc", "roflaw", "d2006", "d2007", "d2008", "d2009", "d2010", "d2011", "d2012", "d2013"),
        custom.gof.names = c("adj. R^2", "N", "F"),
        reorder.gof = c(2, 1, 3))


# ==========================================================================================================================

# QUESTION 5
# ----------


# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Population (pop)
# • Total natural resources rent (natrent)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)

reg6 = lm(numbil0 ~ log(pop) + natrent + gdppc + roflaw, data = mydata)
ct6 = coeftest(reg6, vcov = vcovHC(reg6, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Population (pop)
# • Total natural resources rent (natrent)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)

reg7 = lm(log(numbil0) ~ pop + natrent + gdppc + roflaw, data = mydata)
ct7 = coeftest(reg7, vcov = vcovHC(reg7, "HC1")) 

# Multiple linear regression model of the number of billionaires in a country (regressand) based on (regressors):
# • Population (pop)
# • Total natural resources rent (natrent)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)

reg8 = lm(log(numbil0) ~ log(pop) + natrent + gdppc + roflaw, data = mydata)
ct8 = coeftest(reg8, vcov = vcovHC(reg8, "HC1")) 

# Footnote to explain the table of reports (below)

custom_note = "numbil0 is the number of billionaires in the country.
natrent is natural resources rents in billions of dollars. 
pop is total population of a country in millions. 
gdppc is GDP per capita in thousands of dollars. 
roflaw is an index between 0 and 1 on the level of the rule of law in a country. 
Heteroskedasticity robust standard errors in parentheses.
Statistical significance from two-sided tests of the null of no effect marked as * for 5% and ** for 1%"

# Print the coefficients of all the linear regression models (compiled in a list using the list() function) in 
# a table form using the texreg() function where each column is numbered from (1) through (3) (assigned to
# 'custom.model.names') as to allow comparison between linear regression models as to help determine omitted
# variable biasness as well as performing two-sided t-tests on the coeffients at a 5% and 1% significance level
# using stars to denote the rejection of null of the coefficient being equal to 0 using stars (assigned to 'stars'), 
# reporting the goodness of fit statistics at the bottom such as the number of observations, adjusted R^2, and 
# the regression F-statistic (assigned to 'include.fstatistic') while not including the rmse (assigned to 'include.rmse')
# and the R^2 (assigned to 'include.rsquared') with the footnote at the bottom of the table (assigned to 'custom.note')
# and the table title (assigned to 'caption' and 'caption.above') and put them all in a file (assigned to 'file')
# then override the homoskedastic standard errors and p-values with the heteroskedastic-robust standard errors (assigned to
# 'override.se' and 'override.pvalues')

htmlreg(list(reg6, reg7, reg8), 
        file = "Billionaires and population size.doc",
        caption = "Billionaires and population size",
        caption.above = TRUE,
        stars = c(0.01, 0.05),
        custom.note = custom_note,
        include.rsquared = FALSE,
        include.rmse = FALSE,
        include.fstatistic=TRUE,
        override.se = list(ct6[,2], ct7[,2], ct8[,2]),
        override.pvalues = list(ct6[,4], ct7[,4], ct8[,4]),
        reorder.coef = c(2, 6, 3, 4, 5, 1),
        custom.model.names = c("(1) numbil0", "(2) log(numbil0)", "(3) log(numbil0)"),
        custom.coef.names = c("Constant", "lnpop", "natrent", "gdppc", "roflaw", "pop"),
        custom.gof.names = c("adj. R^2", "N", "F"),
        reorder.gof = c(2, 1, 3))

# ==========================================================================================================================

# QUESTION 6
# ----------

# Create the interaction variables between log(pop) and each of the dummy year variables

lnpop_d2005 = log(pop) * d2005
lnpop_d2006 = log(pop) * d2006
lnpop_d2007 = log(pop) * d2007
lnpop_d2008 = log(pop) * d2008
lnpop_d2009 = log(pop) * d2009
lnpop_d2010 = log(pop) * d2010
lnpop_d2011 = log(pop) * d2011
lnpop_d2012 = log(pop) * d2012
lnpop_d2013 = log(pop) * d2013

# Multiple linear regression model of the log of the number of billionaires in a country (regressand) based on (regressors):
# • Log of population (log(pop))
# • Interaction terms between log(pop) and each of the dummy year variables with 2005 as the base year (omitted) (lnpop_d20xx)
# • Total natural resources rent (natrent)
# • GDP per capita (gdppc)
# • Rule of law index (roflaw)
# • Dummy year variables with 2005 as the base year (omitted) (d20xx)

reg9 = lm(log(numbil0) ~ log(pop) + lnpop_d2006 + lnpop_d2007 + lnpop_d2008 + lnpop_d2009 + lnpop_d2010 + lnpop_d2011 + lnpop_d2012 + lnpop_d2013 + natrent + gdppc + roflaw + d2006 + d2007 + d2008 + d2009 + d2010 + d2011 + d2012 + d2013, data = mydata)
coeftest(reg9, vcov = vcovHC(reg9, "HC1")) 
summary(reg9)$adj.r.squared                                          # Report the adjusted R squared
nobs(reg9)                                                           # Report the number of observations used


# ==========================================================================================================================

# QUESTION 7
# ----------

# Linear hypothesis test of the above regression with the interaction terms set to 0 to
# test the significance of the partial effects on the elasticity of numbil0 with respect to pop

linearHypothesis(reg9,c("lnpop_d2006 = 0", "lnpop_d2007 = 0", "lnpop_d2008 = 0", "lnpop_d2009 = 0", "lnpop_d2010 = 0", "lnpop_d2011 = 0", "lnpop_d2012 = 0", "lnpop_d2013 = 0"), vcov = vcovHC(reg9, "HC1"))


# ==========================================================================================================================

# QUESTION 8
# ----------

# Create interaction variable between log(gdppc) and roflaw

log_gdppc_roflaw = log(gdppc) * roflaw

# Multiple linear regression model of the log of the number of billionaires in a country (regressand) based on (regressors):
# • Log of GDP per capita (log(gdppc))
# • Rule of law index (roflaw)
# • Interaction term between log of GDP per capita and the rule of law index (log_gdppc_roflaw)
# • Log of population (log(pop))
# • Total natural resources rent (natrent)

reg10 = lm(log(numbil0) ~ log(gdppc) + roflaw + log_gdppc_roflaw + log(pop) + natrent, data = mydata)
coeftest(reg10, vcov = vcovHC(reg10, "HC1")) 
summary(reg10)$adj.r.squared                                      # Report the adjusted R squared
nobs(reg10)                                                       # Report the number of observations used

# ==========================================================================================================================

# QUESTION 9
# ----------

# Create variables to hold the coefficients of the log(gdppc) and the interaction
# term of log(gdppc) and roflaw that can be retrieved using the summary() function

coef_log_gdppc = summary(reg10)$coefficients[2,1]
coef_log_gdppc_roflaw = summary(reg10)$coefficients[4,1] * 0.1    # Change the 0.1 to test other roflaw values

# Add the two variables with respect to the given roflaw value to get the elasticity

elasticity = coef_log_gdppc + coef_log_gdppc_roflaw
elasticity

# Linear hypothesis test of the above regression with the log(gdppc) and the interaction term set to 0 to test the
# significance of the partial effects on the elasticity of numbil0 with respect to gdppc depending on the roflaw value

linearHypothesis(reg10,c("log(gdppc)+ 0.1 * log_gdppc_roflaw=0"),vcov = vcovHC(reg10,"HC1"))


# ==========================================================================================================================
