# Project 1 (25%)
https://www.kaggle.com/t/bc342ccd41ee49f3a9256fe359c8f6d1
Start: Thursday 18 March 2021
Competition due: Thursday 15 April 2021
Report due: Friday 16 April 2021

## Tasks:
- [x] Read brief
      - Training network (hyper-graph)
        - nodes = authors
        - bi-directional hyper-edge = link 2 or more authors
      - Testing network
- [ ] Research
  - [x] Have a look at the data
  - [x] Matrix representations (https://www.section.io/engineering-education/graph-data-structure-python/)
        - adjacency matrix
        - incidence matrix - too large considering the number of nodes we have
        - adjacency list
        - edge list
  - [ ] Edge classification (using sigmoid to represent probability) on homogenous graph
    - [ ] https://docs.dgl.ai/guide/training-edge.html
    - [x] https://www.kdnuggets.com/2019/08/neighbours-machine-learning-graphs.html
        - homogeneous vs heterogeneous graph
        - static vs dynamic graphs
        - learning categories:
          - node classification (infer missing values from some of the nodes)
          - link prediction (infer missing / hidden relationships)
          - community detection (infer clusters of nodes)
          - graph classification (classify the graph as a whole)
  - [ ] link prediction (not really what we are looking for as this predicts whether there will be a link. Doesn't work if there's already a link and they wish to re-establish the link)
    - [x] https://en.wikipedia.org/wiki/Link_prediction
          - Approaches:
            - topology-based methods
              - common neighbors
              - Jaccard measure
              - Adamic-Adar measure
              - Katz measure
            - node-attribute based methods
              - Euclidean distance
              - Consine similarity
            - mixed methods
              - Graph embeddings
              - probabilistic relationship models
              - Markov logistic networks
    - [x] https://networkx.org/documentation/networkx-1.10/reference/algorithms.link_prediction.html
    - [ ] https://www.analyticsvidhya.com/blog/2020/01/link-prediction-how-to-predict-your-future-connections-on-facebook/
- [ ] Competition (13 marks)
  - [ ] Decide how to represent the hyper-graph
  - [ ] Decide how to use the hyper-graph to make predictions
  - [ ] Predict on test data which edges are part of authorship network are real (50% of test sample) and which are fake (other 50% of test sample)
  - [ ] Predict whether the pair of authors will coauthor at least 1 paper together in the future
  - [ ] Generate output test predictions to upload to Kaggle
  - [ ] Test in Kaggle
- [ ] Report (audience: post-grad student) - 3 pages (12 marks)
  - [ ] Description of problem
  - [ ] Notation used in the report
  - [ ] Final approach to link prediction
    - [ ] motivation
    - [ ] reasoning
    - [ ] why it performed well/badly
  - [ ] Alternative approaches considered
        - Counting how frequently 2 people work together and converting that to a percentage
        - Finding the shortest path between 2 nodes and weight the probability and distance to predict the probability
    - [ ] empirical evaluation to support reasoning
  - [ ] Hand-in as PDF


