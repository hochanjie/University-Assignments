import pandas as pd
import networkx as nx
import random
import sklearn
from tqdm import tqdm
import sklearn.utils
import itertools
import numpy as np
from sklearn.feature_selection import RFE
from sklearn.linear_model import LogisticRegression

#loading train data
def loadTrainData():
    filename = "train.txt"
    return [line.rstrip("\n") for line in open(filename)]

def loadTrainDataAsUndirectedGraph():
    filename = "train.txt"
    rows = [line.rstrip("\n") for line in open(filename)]
    g = nx.Graph()
    for row in rows:
        authorIds = row.split()
        for i, author in enumerate(authorIds):
            for coauthor in authorIds[i+1:]:
                if g.has_edge(author, coauthor):
                    g[author][coauthor]['frequency'] += 1
                else:
                    g.add_edge(author, coauthor, frequency=1)
    return g


def createBalancedTrainingData(graph, testDF):
    trueInstances = [[n1, n2, 1] for (n1, n2) in graph.edges if graph[n1][n2]['frequency'] > 1]
    #     trueInstances = [[n1, n2, 1] for (n1, n2) in graph.edges]
    sourceSink = testDF["Source-Sink"].tolist()
    adj_G = nx.to_numpy_matrix(graph, nodelist=graph.nodes)
    # get unconnected node-pairs
    f1 = []
    f2 = []

    # traverse adjacency matrix
    l = int(0.5 * len(trueInstances))
    while len(f1) < l or len(f2) < l:
        random.seed()  # Removed seed
        i = random.sample(range(adj_G.shape[0]), 1)[0]
        j = random.sample(range(adj_G.shape[1]), 1)[0]
        try:
            # If shortest path > 4 then score goes down to 0.77
            if 2 < nx.shortest_path_length(graph, str(i), str(j)):
                if len(f2) < l and (i, j) not in sourceSink:
                    # print(i, j)
                    f2.append([str(i), str(j), 0])
        except:
            if len(f1) < l and (i, j) not in sourceSink:
                f1.append([str(i), str(j), 0])

    print('len(f1):', len(f1))
    print('len(f2):', len(f2))
    print('len(trueInstances):', len(trueInstances))

    data = sklearn.utils.shuffle(f1 + f2 + trueInstances)
    return pd.DataFrame(data, columns=['Source', 'Sink', 'Label'])


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


# functions that generating features
def fu(g1, u):
    return 1


def getNodeScore(g1, Nu):
    RA = 0
    CCN = len(Nu)
    CRA = 0
    if len(Nu) == 0:
        return 0, 0, 0
    for u in Nu:
        RA += 1 / len(g1[u])
        CCN += fu(g1, u)
        CRA += fu(g1, u) / len(g1[u])
    return RA, CCN, CRA


def localPath2(graph, n1, n2):
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

        return A1, A2, A3
    except:
        return 0, 0, 0


def Pxy(g1, x, y):
    try:
        Gx = g1[x]
        Gy = g1[y]
    except:
        return 0, 0, 0, 0, 0, 0, 0
    Nx = [i[0] for i in Gx]
    Ny = [i[0] for i in Gy]
    NxINy = list(set(Nx) & set(Ny))
    NxUNy = list(set(Nx + Ny))
    RA, CCN, CRA = getNodeScore(g1, NxINy)
    cardNx = len(Nx)
    cardNy = len(Ny)
    PA = cardNx * cardNy
    JC = len(NxINy) / len(NxUNy)
    HPI = len(NxINy) / min(cardNx, cardNy)
    HDI = len(NxINy) / max(cardNx, cardNy)
    return len(NxINy), len(NxINy) / np.sqrt(cardNx * cardNy), JC, HPI, HDI, PA, RA


# preprocessing train data
def pre_row(trainRow):
    txt = [list(map(int, i.split())) for i in trainRow]
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
    # train1['freq'] = freq
    txt_1 = sorted(set(converted_txt))
    df1 = pd.DataFrame(txt_1, columns=["Source", "Sink"])
    # df1['freq'] = freq
    df1['score'] = 1
    for points, f in zip(txt_1, freq):
        g1[points[0]] = g1.setdefault(points[0], [])
        g1[points[0]].append((points[1], f))
    V = list(set(tmp))
    return g1, V, df1, txt_1


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


def addmoreFeatures(g1, df3, graph):
    # generate features
    df_tmp = df3.copy()
    CNs, SIs, JCs, HPIs, HDIs, PAs, RAs = [], [], [], [], [], [], []
    A1S, A2S, A3S = [], [], []
    for idx, i in df_tmp.iterrows():
        n1 = int(i[0])
        n2 = int(i[1])
        CN, SI, JC, HPI, HDI, PA, RA = Pxy(g1, n1, n2)
        A1, A2, A3 = localPath2(graph, str(n1), str(n2))
        CNs.append(CN)
        SIs.append(SI)
        JCs.append(JC)
        HPIs.append(HPI)
        HDIs.append(HDI)
        PAs.append(PA)
        RAs.append(RA)
        A1S.append(A1)
        A2S.append(A2)
        A3S.append(A3)
        A4S.append(A4)

    df_tmp['CN'] = CNs
    df_tmp['SI'] = SIs
    df_tmp['JC'] = JCs
    df_tmp['HPI'] = HPIs
    df_tmp['HDI'] = HDIs
    df_tmp['PA'] = PAs
    df_tmp['RA'] = RAs
    df_tmp['A1'] = A1S
    df_tmp['A2'] = A2S
    df_tmp['A3'] = A3S
    return df_tmp


trainGraph = loadTrainDataAsUndirectedGraph()

testDF = pd.read_csv('test-public.csv', converters = {'Source': str, 'Sink': str})
testDF['Source-Sink'] = list(zip(testDF['Source'], testDF['Sink']))
trainDF = createBalancedTrainingData(trainGraph, testDF)

train1 = loadTrainData()
# an attempt to split the data into train and dev set
random.shuffle(train1)
# m = int(len(train1)*0.9)
# trainRow = train1[0:m]
# devRow = train1[m:]
trainRow = train1
g1, V, df1, txt_1 = pre_row(trainRow)
# gdev, Vdev, devDF, txt_dev = pre_row(devRow)

addFeaturesToDataframe(trainGraph, trainDF)
addFeaturesToDataframe(trainGraph, testDF)
# addFeaturesToDataframe(trainGraph, devDF)

trainDF = addmoreFeatures(g1, trainDF, trainGraph)
testDF = addmoreFeatures(g1,testDF, trainGraph)
# devDF = addmoreFeatures(g1, devDF)

FeatureColumns = [value for value in trainDF.columns if value not in ['Source', 'Sink', 'Label', 'RA', 'PA', 'JC', 'CN']] # 'SI', 'HPI', 'LP'
LableColumn = 'Label'

X_train = trainDF[FeatureColumns]
y_train = trainDF[LableColumn]

# ## Wrapper method
lr = LogisticRegression(class_weight="balanced")
selector = RFE(lr, n_features_to_select=8, step=1)
selector = selector.fit(X_train, y_train)
allF = pd.DataFrame({'features': X_train.columns,'importance': selector.ranking_})
importantFeatures = list(allF.query('importance==1')['features'])
importantFeatures.sort()
print('importantFeatures = ', importantFeatures)

## Filter method
from sklearn.feature_selection import SelectKBest
from sklearn.feature_selection import chi2
s = SelectKBest(chi2, k=6)
s.fit(X_train, y_train)
allF = pd.DataFrame({'features': X_train.columns,'scores': s.scores_, 'pvalue':s.pvalues_}).sort_values(by=['scores'])
importantFeatures = list(allF['features'])[:8]
importantFeatures.sort()
print('importantFeatures = ', importantFeatures)

# features = ['CommonNeighbours', 'AdamicAdar', 'Dist', 'SI', 'HPI', 'LP']
# features = ['CommonNeighbours', 'Jaccard', 'AdamicAdar','PreferentialAttachment', 'ResourceAllocation', 'Dist', 'SI', 'HPI', 'LP']
features = importantFeatures
X_train = trainDF[features]
y_train = trainDF['Label']
# X_dev = devDF[features]
X_test = testDF[features]
testIds = testDF['Id']

from sklearn.linear_model import LogisticRegression
lr = LogisticRegression(class_weight="balanced")
cross_val_score(lr, X_train, y_train, scoring='roc_auc', cv=5)
lr.fit(X_train, y_train)
predictions = lr.predict_proba(X_test)
final_result = pd.DataFrame(data={'Id': testIds, 'Predicted': predictions[:,1]})
final_result.to_csv('results/LogisticRegression.csv', index=False)