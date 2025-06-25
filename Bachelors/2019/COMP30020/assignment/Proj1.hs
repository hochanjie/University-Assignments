module Proj1 (feedback, initialGuess, nextGuess, GameState) where

  import Card
  import Data.List
  import qualified Data.Set
  --import Data.Containers.ListUtils
  
  -- GameState type constructor ------------------------------------------------
  
  data GameState = GameState {
    pool :: [[Card]]
  } deriving Show

  ------------------------------- MAIN FUNCTIONS -------------------------------

  -- Feedback function ---------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Compares the guessed cards to the answer and returns the statistics of the
  -- guess ---------------------------------------------------------------------
  ------------------------------------------------------------------------------

  feedback :: [Card] -> [Card] -> (Int,Int,Int,Int,Int)
  
  feedback answer guess = (correct, lower, sameRank, higher, sameSuit) where
    
    guessRank = [rank x | x <- guess]
    answerRank = [rank x | x <- answer]
    guessSuit = [suit x | x <- guess]
    answerSuit = [suit x | x <- answer]
    
    correct = (length answer) - (length (guess \\ answer))
    lower = length (filter (< minimum guessRank) answerRank)
    sameRank = length (answerRank \\ (answerRank \\ guessRank))
    higher = length (filter (> maximum guessRank) answerRank)
    sameSuit = length (answerSuit \\ (answerSuit \\ guessSuit))


  -- initialGuess function -----------------------------------------------------
  ------------------------------------------------------------------------------
  -- Function to evenly space out the ranks among the cards in the guess and 
  -- assign different suits to them (no need to worry about having more than 4
  -- cards in the guess) -------------------------------------------------------
  ------------------------------------------------------------------------------

  initialGuess :: Int -> ([Card],GameState)
  initialGuess n = (cards, state) where
    cards = [Card suit rank | (suit,rank) <- 
      zip (take n [Club ..]) (take n (everyNth (div 13 n) [R2 ..]))]

    combos = nubOrd [sort cards | cards <- combo, allDifferent cards] where
      combo = sequence (replicate n [(Card Club R2) .. (Card Spade Ace)])

    state = GameState {pool = (combos \\ [cards])}


  -- nextGuess function --------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Function that takes the previous guess and then:
  -- • removes any guesses with any cards that are outside the lower and upper 
  --   bound implied from the feedback of the previous guess
  --
  -- • runs the Knuth algorithm (filter) on the remaining choices in the 
  --   GameState. This is where we only keep the combos within the pool that 
  --   would've given a feedback consistent to the feedback that the previous 
  --   guess had gotten if that particular combo was the actual answer
  --
  -- • make that the new GameState, with the next guess being the first choice 
  --   in the new pool ---------------------------------------------------------
  ------------------------------------------------------------------------------
  nextGuess :: ([Card],GameState) -> (Int,Int,Int,Int,Int) -> ([Card],GameState)
  nextGuess (prev, state) fb = (newGuess, newState) where
     
    newPool = [cards | cards <- removeOuter (prev, state) fb, 
      feedback cards prev == fb]
     
    newGuess = head newPool
    newState = state {pool = tail newPool}
    

  ------------------------------ HELPER FUNCTIONS ------------------------------

  -- allDifferent function -----------------------------------------------------
  ------------------------------------------------------------------------------
  -- Checks to make sure all the cards in a potential guess is different -------
  ------------------------------------------------------------------------------

  allDifferent :: [Card] -> Bool
  allDifferent [] = True
  allDifferent (x:xs) = notElem x xs && allDifferent xs
  
  
  -- everyNth function ---------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Similar to the everyNth function from Assignment 1 where it takes every Nth
  -- rank ----------------------------------------------------------------------
  ------------------------------------------------------------------------------

  everyNth :: Int -> [rank] -> [rank]
  everyNth n xs = case drop (n - 1) xs of
      (y:ys)  -> y : everyNth n ys
      []      -> []

  
  -- removeOuter function ------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Function to reducing the size of the pool using the number of lower and 
  -- higher ranked cards in the feedback ---------------------------------------
  ------------------------------------------------------------------------------

  removeOuter :: ([Card], GameState) -> (Int,Int,Int,Int,Int) -> [[Card]]
  removeOuter (guess, state) (_,lower,_,higher,_) = noOuter where
  
    guessRank  = [rank x | x <- guess]

    -- If there aren't any cards with a lower rank in the answer, then remove
    -- all card combos in the pool with lower ranked cards

    lowerBounded = if lower == 0 
      then [combos | combos <- pool state, check combos (>= minimum guessRank)]
      else pool state

    -- Now do the same for the higher ranks

    noOuter = if higher == 0 
      then [combos | combos <- pool state, check combos (<= maximum guessRank)]
      else lowerBounded

  -- check function ------------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Pretty much a filter function to check that all cards in a combo are within
  -- the desired bounds --------------------------------------------------------
  ------------------------------------------------------------------------------

  check :: [Card] -> (Rank -> Bool) -> Bool
  check [] rule = True
  check (x:xs) rule = (rule (rank x)) && check xs rule

  -- nurOrd function -----------------------------------------------------------
  ------------------------------------------------------------------------------
  -- Function to replace nub, which is much slower. This can be imported from
  -- Data.Containers.ListUtils but Grok does not have such a library and I do
  -- not know if it is safe to assume that it I can import such a library on the
  -- server that this assignment is being marked on. If yes, this section can be
  -- commented out as it would create an error if not --------------------------
  ------------------------------------------------------------------------------

  nubOrd :: [[Card]] -> [[Card]]
  nubOrd xs = go Data.Set.empty xs where
    go _ [] = []
    go s (x:xs) = 
      if x `Data.Set.member` s then go s xs 
      else x : go (Data.Set.insert x s) xs  

  ------------------------------------------------------------------------------