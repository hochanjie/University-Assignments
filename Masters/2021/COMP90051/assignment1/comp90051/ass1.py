import pandas as pd 
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import KFold
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix,accuracy_score
from sklearn.metrics import roc_curve, auc
from sklearn.metrics import explained_variance_score
from sklearn import metrics
import statsmodels.api as sm
import seaborn as sn
import itertools
import time
import networkx as nx
import random
import lightgbm as lgb


def shortestDistance(graph, n1, n2):
    try:
        return 1 / nx.shortest_path_length(graph, n1, n2)
    except:
        return 0


def commonNeighbours(graph, n1, n2):
    try:
        return len(list(nx.common_neighbors(graph, n1, n2)))
    except:
        return 0


def jaccard(graph, n1, n2):
    try:
        return list(nx.jaccard_coefficient(graph, [(n1, n2)]))[0][2]
    except:
        return 0


def adamicAdar(graph, n1, n2):
    try:
        return list(nx.adamic_adar_index(graph, [(n1, n2)]))[0][2]
    except:
        return 0


def preferentialAttachment(graph, n1, n2):
    try:
        return list(nx.preferential_attachment(graph, [(n1, n2)]))[0][2]
    except:
        return 0


def resourceAllocation(graph, n1, n2):
    try:
        return list(nx.resource_allocation_index(graph, [(n1, n2)]))[0][2]
    except:
        return 0


def localPath(graph, n1, n2):
    try:
        paths = list(nx.all_simple_paths(graph, source=n1, target=n2, cutoff=3))
        A2 = 0.0
        A3 = 0.0
        A1 = 0.0
        for path in paths:
            if len(path) == 3:
                A2 = A2 + 1.0
            elif len(path) == 4:
                A3 = A3 + 1.0
            elif len(path) == 2:
                A1 = A1 + 1.0
        return A1 + 0.01 * A2 + 0.0001 * A3
    except:
        return 0

def addFeaturesToDataframe(graph, data):
    data['CommonNeighbours'] = data.apply(lambda l: commonNeighbours(graph, l.Source, l.Sink), axis=1)
    print('Added "CommonNeighbours" column')
    data['Jaccard'] = data.apply(lambda l: jaccard(graph, l.Source, l.Sink), axis=1)
    print('Added "Jaccard" column')
    data['AdamicAdar'] = data.apply(lambda l: adamicAdar(graph, l.Source, l.Sink), axis=1)
    print('Added "AdamicAdar" column')
    data['PreferentialAttachment'] = data.apply(lambda l: preferentialAttachment(graph, l.Source, l.Sink), axis=1)
    print('Added "PreferentialAttachment" column')
    data['ResourceAllocation'] = data.apply(lambda l: resourceAllocation(graph, l.Source, l.Sink), axis=1)
    print('Added "ResourceAllocation" column')
    data['Dist'] = data.apply(lambda l: shortestDistance(graph, l.Source, l.Sink),
                              axis=1)  ## can't just leave as highest number cause will be detrimental when normalising
    print('Added "Dist" column')

    # newly-added
    data['LP'] = data.apply(lambda l: localPath(graph, l.Source, l.Sink), axis=1)
    print('Added "LP" column')


def addmoreFeatures(g1, df3):
    # generate features
    df_tmp = df3.copy()
    CNs, SIs, JCs, HPIs, HDIs, PAs, RAs = [], [], [], [], [], [], []
    for idx, i in df_tmp.iterrows():
        n1 = int(i[0])
        n2 = int(i[1])
        CN, SI, JC, HPI, HDI, PA, RA = Pxy(g1, n1, n2)
        CNs.append(CN)
        SIs.append(SI)
        JCs.append(JC)
        HPIs.append(HPI)
        HDIs.append(HDI)
        PAs.append(PA)
        RAs.append(RA)

    df_tmp['CN'] = CNs
    df_tmp['SI'] = SIs
    df_tmp['JC'] = JCs
    df_tmp['HPI'] = HPIs
    df_tmp['HDI'] = HDIs
    df_tmp['PA'] = PAs
    df_tmp['RA'] = RAs
    return df_tmp

# network graph drawing
def createG1(txt):
    g1 = {}
    converted_txt = []
    tmp = []
    for link in txt:
        for subset in itertools.permutations(link, 2):
            tmp.append(subset[0])
            tmp.append(subset[1])
            converted_txt.append(subset)
    train1 = pd.DataFrame(converted_txt, columns=["srce", "dest"])
    freq = train1.groupby(["srce", "dest"]).size().values
    #train1['freq'] = freq
    txt_1 = sorted(set(converted_txt))
    df1 = pd.DataFrame(txt_1, columns=["srce", "dest"])
    df1['freq'] = freq
    df1['score'] = 1 - 0.99/freq
    for points, f in zip(txt_1, freq):
        g1[points[0]] = g1.setdefault(points[0], [])
        g1[points[0]].append((points[1], f))

    return g1, list(set(tmp)), df1


# path finding
def find_min(open_dict, sink):
    if sink in open_dict.keys():
        return [sink, open_dict[sink][0], open_dict[sink][1]]
    values = list(open_dict.values())
    values.sort(key=lambda x: x[1])
    min = values[0][0]
    for i in open_dict.keys():
        if open_dict[i][0] == min:
            return [i, open_dict[i][0], open_dict[i][1]]


def show_path(close_dict, path, sink):
    if (sink in close_dict.keys()):
        path.append(sink)
        path = show_path(close_dict, path, close_dict[sink][1])
    return path


def find_path(g1, srce, sink):
    path = []
    open_dict = {srce: [0, -1]}
    close_dict = {}
    while len(open_dict.keys()) != 0:
        min_key = find_min(open_dict, sink)
        close_dict[min_key[0]] = [min_key[1], min_key[2]]  # [score, parent node]
        if min_key[0] == sink:
            return show_path(close_dict, path, sink)[::-1]
        else:
            subnodes = []
            if min_key[0] not in g1.keys():
                open_dict.pop(min_key[0])
                continue
            for i in g1[min_key[0]]:
                subnodes.append([i[0], i[1], min_key[0]])  # [current, score, parent node]
            for node in subnodes:
                if node[0] in close_dict.keys():
                    continue
                if node[0] not in open_dict.keys():
                    open_dict[node[0]] = [node[1], node[2]]
                elif node[0] in open_dict.keys():
                    if open_dict[node[0]][0] > node[1]:
                        open_dict[node[0]][0] = node[1]
                        open_dict[node[0]][1] = node[2]
            open_dict.pop(min_key[0])
    return []


def cal_score(g1, path):
    s = 0
    for i in range(len(path) - 1):
        for j in g1[path[i]]:
            if j[0] == path[i + 1]:
                s += j[1]
    return s

#load test data
def loadTestData(filename):
    return pd.read_csv(filename)

#generating features
def fu(g1, u):
    return 1

def getNodeScore(g1, Nu):
    AA = 0
    RA = 0
    CCN = len(Nu)
    CRA = 0
    if len(Nu) == 0:
        return 0, 0, 0, 0
    for u in Nu:
        AA += 1/np.log(len(g1[u]))
        RA += 1/len(g1[u])
        CCN += fu(g1, u)
        CRA += fu(g1, u)/len(g1[u])
    return AA, RA, CCN, CRA

def Pxy(g1, x, y):
    try:
        Gx = g1[x]
        Gy = g1[y]
    except:
        return 0, 0, 0, 0, 0, 0, 0, 0
    Nx = [i[0] for i in Gx]
    Ny = [i[0] for i in Gy]
    NxINy = list(set(Nx) & set(Ny))
    NxUNy = list(set(Nx + Ny))
    AA, RA, CCN, CRA = getNodeScore(g1, NxINy)
    cardNx = len(Nx)
    cardNy = len(Ny)
    PA = cardNx * cardNy
    JC = len(NxINy)/len(NxUNy)
    HPI = len(NxINy)/min(cardNx, cardNy)
    HDI = len(NxINy)/max(cardNx, cardNy)
    return AA, len(NxINy), len(NxINy)/np.sqrt(cardNx*cardNy), JC, HPI, HDI, PA, RA

#load train data
def loadTrainData():
    filename = "train.txt"
    return [line.rstrip("\n") for line in open(filename)]

#create graph
def createUndirectedGraph(dataRow):
    g = nx.Graph()
    for row in dataRow:
        authorIds = row.split()
        for i, author in enumerate(authorIds):
            for coauthor in authorIds[i+1:]:
                if g.has_edge(author, coauthor):
                    g[author][coauthor]['frequency'] += 1 # TODO: train this (e.g. noise in the training data)
                else:
                    g.add_edge(author, coauthor, frequency=1)
    return g

#normalize result
def normalize(results):
    minf = min(results)
    maxf = max(results)
    new = []
    for i in results:
        newi = (i - minf)/(maxf - minf)
        new.append(newi)
    return new

#read train data
def readTxtFile(filename):
    return [line.rstrip("\n") for line in open(filename)]

#evaluation based on length of path, currently discard
def evaluation(G1, i, trainGraph):
    try:
        path = nx.dijkstra_path(trainGraph, source=str(int(i[1])), target=str(int(i[2])), weight='Distance')
        l = len(path)
        if l == 2:
            JC, RA, AA, PA, CCN, CRA = Pxy(G1, i[1], i[2])
            return JC
        elif l > 2:
            JC1 = 1
            for j in range(l - 1):
                JC, RA, AA, PA, CCN, CRA = Pxy(G1, int(path[j]), int(path[j + 1]))
                if JC == 0:
                    return 0
                JC1 *= JC
            return JC1
    except:
        return 0

# this is your code
class EdgeAddClassifier:
    def __init__(self):
        self.model = None

    def train(self, graph):
        self.__trainAttempt4(graph)

    def predict(self, edgeList):
        return self.__predictAttempt4(edgeList)

    #####################################################################
    # Attempt 4: Kaggle Score = ?
    #####################################################################
    # stocahstic edge adding
    def __trainAttempt4(self, graph):
        self.__buildProbabilityGraph(graph)
        print('iteration 0: number of edges = ', len(self.model.edges))
        self.__stochasticadd(500000)
        print('iteration 1: number of edges = ', len(self.model.edges))

    def __predictAttempt4(self, edgeList):
        return edgeList.apply(
            lambda i: self.__classifyInstance4(str(i.Source), str(i.Sink)),
            axis=1)
    # trying to calculate score based on weight of l1, l2, l3
    def __cal_feature(self, trainGraph, n1, n2, commonN, normaliseLimit):
        l1 = self.__normalise(commonN, 0, normaliseLimit)
        l2 = list(nx.adamic_adar_index(trainGraph, [(n1, n2)]))[0][2]
        l3 = list(nx.resource_allocation_index(trainGraph, [(n1, n2)]))[0][2]
        return l1 + l2 + l3

    def __stochasticadd(self, maxiter):
        sum = 0
        i = 0
        while sum < 1000:
            if i % 300000 == 0 and i != 0:
                sum = self.__evaluate(i)
            self.__addEdgesrandom(6)
            i += 1

    # add edge when there is a common node or when there is a path
    def __addEdgesrandom(self, normaliseLimit):
        allNodes = list(self.model.nodes)
        nodes = random.sample(allNodes, 2)
        newNodes = []
        n1 = nodes[0]
        n2 = nodes[1]
        if not self.model.has_edge(n1, n2):
            commonN = len(list(nx.common_neighbors(self.model, n1, n2)))
            if commonN > 0:
                score = self.__cal_feature(trainGraph, n1, n2, commonN, normaliseLimit)
                newNodes.append((n1, n2, 0.9))
            else:
                try:
                    path = nx.dijkstra_path(trainGraph, source=source, target=sink)
                    l = len(list(path))
                    score = 1
                    for i in range(l - 1):
                        # assume that P(x, y) = P(x,z) * P(z, y) if shortest path from x to y is [x, z, y]
                        score *= self.model[source][sink]['prob']
                    if score < 0.01:
                        pass
                    elif score > 1:
                        newNodes.append((n1, n2, 1))
                    else:
                        newNodes.append((n1, n2, score))
                except:
                    pass
        self.model.add_weighted_edges_from(newNodes, weight='prob')

    # modified
    def __classifyInstance4(self, source, sink):
        if self.model.has_edge(source, sink):
            return self.model[source][sink]['prob']
        else:
            return 0

    #####################################################################
    # Helper Functions
    #####################################################################

    def __buildProbabilityGraph(self, graph):
        self.model = nx.Graph()
        for (source, sink) in graph.edges:
            freq = graph[source][sink]['frequency']
            prob = self.__normalise(freq, 0, 2)  # TODO: remove hardcoded
            self.model.add_edge(source, sink, prob=prob)

    def __normalise(self, v, minF, maxF):
        result = (v - minF) / (maxF - minF)
        if result > 1:
            return 1
        elif result <= 0:
            return 0
        else:
            return 1
    def __evaluate(self, iter):
        print("# of edges:", len(self.model.edges))
        test = pd.read_csv('test-public.csv')
        p1 = self.predict(test)
        test['Predicted'] = p1
        test[['Id', 'Predicted']].to_csv('results/EdgeAddClassifier.csv', index=False)
        pred = list(p1)
        print("# of iter:", iter)
        print("# of zeros:", pred.count(0))
        print("# of ones", pred.count(1))
        print("sum:", sum(pred))
        return sum(pred)

# preprocess training data
def pre_row(trainRow):
    txt = [list(map(int, i.split())) for i in trainRow]
    trainGraph = createUndirectedGraph(trainRow)
    g1 = {}
    converted_txt = []
    tmp = []
    for link in txt:
        for subset in itertools.permutations(link, 2):
            tmp.append(subset[0])
            tmp.append(subset[1])
            converted_txt.append(subset)
    train1 = pd.DataFrame(converted_txt, columns=["Source", "Sink"])
    freq = train1.groupby(["Source", "Sink"]).size().values
    #train1['freq'] = freq
    txt_1 = sorted(set(converted_txt))
    df1 = pd.DataFrame(txt_1, columns=["Source", "Sink"])
    #df1['freq'] = freq
    df1['score'] = 1
    for points, f in zip(txt_1, freq):
        g1[points[0]] = g1.setdefault(points[0], [])
        g1[points[0]].append((points[1], f))
    V = list(set(tmp))
    return g1, V, df1, txt_1

#evaluation function mentioned in paper
def AUC(R, M):
    nume = 0
    deno = 0
    for i in R:
        deno += len(M)
        nume += sum(i > j for j in M)
        nume += 0.5*list(M).count(i)
    return nume/deno

def precision(R, M, thres):
    s1 = sum(i > thres for i in R)
    s2 = sum(j > thres for j in M)
    return (s1)/(s1+s2)

#this function is used in K-fold, it take splitted data, train train dataset and holdout dev dataset
def rowtoauc(trainRow, devRow, trainGraph):
    # Load and Preprocess data
    g1, V, df1, txt_1 = pre_row(trainRow)
    # generate psudo data based on missing link
    psuedo = []
    i = 0
    while len(psuedo) < len(txt_1):
        random.seed(i)
        nodes = random.sample(V, 2)
        n1, n2 = nodes[0], nodes[1]
        try:
            path = nx.dijkstra_path(trainGraph, source=n1, target=n2)
        except:
            if [n1, n2, 0] not in psuedo:
                psuedo.append([n1, n2, 0])
        i += 1
    df2 = pd.DataFrame(psuedo, columns=["Source", "Sink", 'score'])
    df3 = df1.append(df2)

    addFeaturesToDataframe(trainGraph, df3)

    # extract feature
    AAs, CNs, SIs, JCs, HPIs, HDIs, PAs, RAs = [], [], [], [], [], [], [], []
    for idx, i in df3.iterrows():
        n1 = i[0]
        n2 = i[1]
        AA, CN, SI, JC, HPI, HDI, PA, RA = Pxy(g1, n1, n2)
        AAs.append(AA)
        CNs.append(CN)
        SIs.append(SI)
        JCs.append(JC)
        HPIs.append(HPI)
        HDIs.append(HDI)
        PAs.append(PA)
        RAs.append(RA)

    df3['AA'] = AAs
    df3['CN'] = CNs
    df3['SI'] = SIs
    df3['JC'] = JCs
    df3['HPI'] = HPIs
    df3['HDI'] = HDIs
    df3['PA'] = PAs
    df3['RA'] = RAs

    # train_test_split
    # ['CommonNeighbours', 'AdamicAdar', 'Dist', 'SI', 'HPI', 'LP'] 0.9618883787832393
    # ['CommonNeighbours', 'AdamicAdar', 'Dist', 'SI', 'HPI']       0.9618883787832393
    features = ['CommonNeighbours', 'AdamicAdar', 'Dist', 'SI', 'HPI', 'LP']
    X = df3[features]
    y = df3['score']
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=0)

    # train model
    X_train2 = X_train[features]
    X_test2 = X_test[features]
    X_test2['intercept'] = 1.0
    X_train2['intercept'] = 1.0
    train_data = lgb.Dataset(X_train, label=y_train)
    parameters = {
        'objective': 'binary',
        'metric': 'auc',
        'feature_fraction': 0.5,
        'bagging_fraction': 0.5,
        'bagging_freq': 20,
        'learning_rate':0.05,
        'num_leaves': 40,
        'num_threads': 2,
        'seed': 90051
    }
    tmp = lgb.cv(parameters, train_data, 100, nfold = 5)
    print(max(tmp['auc-mean']))
    lgbm = lgb.train(parameters, train_data, 100)
    lgbmPredictions = lgbm.predict(X_test)
    lgbmResult = pd.DataFrame(data={'Id': testIds, 'Predicted': lgbmPredictions})
    lgbmResult.to_csv('results/lightGBM.csv', index=False)

    y_pred = lr.predict_proba(X_test2)
    y_pred1 = []
    for i in y_pred:
        if i[1] > 0.5:
            y_pred1.append(1)
        else:
            y_pred1.append(0)

    X_psuedo = []
    for i in psuedo:
        n1 = i[0]
        n2 = i[1]
        AA, CN, SI, JC, HPI, HDI, PA, RA = Pxy(g1, n1, n2)
        X_psuedo.append([AA, CN, SI, JC, HPI, HDI, PA, RA, 1])

    y_psuedo = lr.predict_proba(X_psuedo)
    y_psuedo = [i[1] for i in y_psuedo]
    X_dev = []
    g_dev, V_dev, df_dev, txt_dev = pre_row(devRow)
    for i in txt_dev:
        n1 = i[0]
        n2 = i[1]
        AA, CN, SI, JC, HPI, HDI, PA, RA = Pxy(g1, n1, n2)
        X_dev.append([AA, CN, SI, JC, HPI, HDI, PA, RA, 1])

    y_dev = lr.predict_proba(X_dev)
    y_dev = [i[1] for i in y_dev]
    n_dev = random.sample(list(y_dev), 1000)
    n_psuedo = random.sample(list(y_psuedo), 1000)
    auc = AUC(n_dev, n_psuedo)
    prec = precision(y_dev, y_psuedo, 0.5)
    print(auc, prec)
    return auc, prec, lr

# Load data
test = pd.read_csv("test-public.csv")
train1 = loadTrainData()
trainGraph  = createUndirectedGraph(train1)
random.shuffle(train1)
auc_l = []
prec_l = []
model_l = []
kf = KFold(n_splits=10,shuffle=False)
traindf = np.array(train1)

#for train_index, dev_index in kf.split(train1):
    #X_train, X_dev = traindf[train_index], traindf[dev_index]
for i in range(1):
    X_train, X_dev = traindf, []
    auc, prec, lr = rowtoauc(X_train, X_dev, trainGraph)
    auc_l.append(auc)
    prec_l.append(prec)
    model_l.append(lr)
print(auc_l, prec_l)
for i in auc_l:
    if i == max(auc_l):
        final1 = model_l[i]



"""
JCs = []
nodes = df1.values.tolist()
for i in nodes:
    JC = evaluation(G1, i, trainGraph)
    JCs.append(JC)

df1['JC'] = JCs
test = list(df1['score'])
test1 = normalize(test, 0.1)
df1['score'] = test1
X = df1[['JC']]
y = df1['score']

X_train,X_test,y_train,y_test = train_test_split(X,y,test_size=0.25,random_state=0)

lr = LogisticRegression(penalty = 'l2')
lr.fit(X_train, y_train)
y_pred = lr.predict(X_test)

confusion_matrix = pd.crosstab(y_test, y_pred, rownames=['Actual'], colnames=['Predicted'])
sn.heatmap(confusion_matrix, annot=True)
plt.show()
print('Accuracy: ',metrics.accuracy_score(y_test, y_pred))
"""
"""
JCs = []
for idx, i in test.iterrows():
    JC = evaluation(G1, i, trainGraph)
    JCs.append(JC)

print(JCs)
JCs = normalize(JCs)

test['Predicted'] = JCs
# Save result
test[['Id', 'Predicted']].to_csv('results/simpleClassifierPredicted.csv', index=False)
"""
