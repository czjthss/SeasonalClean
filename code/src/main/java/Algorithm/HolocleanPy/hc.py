from holoclean import HoloClean
from detect import NullDetector, ViolationDetector
from repair.featurize import *
import numpy as np
import pandas as pd
import sys

FILE_DIR = "/Users/chenzijie/Documents/GitHub/repair-seasonal-exp/src/main/java/Algorithm/HolocleanPy/"


def constant_ext(data_ext, interval):
    for i in range(interval):  # head nan
        data_ext[i] = data_ext[interval]
    for i in range(len(data_ext) - interval, len(data_ext)):  # tail nan
        data_ext[i] = data_ext[len(data_ext) - interval - 1]
    return data_ext


def error_tolerant_decomposition(s, period):
    # step 1: get trend
    trend = np.array([])
    interval = period // 2
    if period % 2 == 1:
        # head interval
        for i in range(interval):
            trend = np.append(trend, np.NaN)

        # moving median
        for i in range(len(s) - period + 1):
            trend = np.append(trend, np.median(s[i:i + period]))

        # tail interval
        for i in range(interval):
            trend = np.append(trend, np.NaN)
    else:
        # head interval
        for i in range(interval):
            trend = np.append(trend, np.NaN)

        # moving median
        for i in range(len(s) - period):
            temp = np.append(s[i + 1:i + period], (s[i] + s[i + period]) / 2)
            trend = np.append(trend, np.median(temp))

        # tail interval
        for i in range(interval):
            trend = np.append(trend, np.NaN)

    # step 1.5: fillna linear
    trend = constant_ext(trend, interval)

    # step 2: de-trend
    detrend = s - trend

    # step3: seasonal
    seasonal = np.array([])
    temp = [[] for _ in range(period)]
    for i in range(len(detrend)):
        temp[i % period].append(detrend[i])

    # mean --> median
    for t in temp:
        seasonal = np.append(seasonal, np.median(t))
    median_s = np.median(seasonal)
    seasonal = seasonal - median_s

    # extend
    len_s = len(seasonal)
    for i in range(len(s) - len_s):
        seasonal = np.append(seasonal, seasonal[i % period])

    # trend = new_ext(trend, interval, s, seasonal)

    # step 4: residual
    resid = s - trend - seasonal
    return seasonal, trend, resid


def reformat_data(file_path, period):
    origin = pd.read_csv(file_path, dtype=float)
    seasonal, trend, resid = error_tolerant_decomposition(origin["value"], period)

    # record constraint
    mean, std = np.mean(resid), np.std(resid)

    # record data
    origin["seasonal"] = seasonal
    origin["trend"] = trend
    origin["resid"] = resid
    origin["thr1"] = mean - 3 * std
    origin["thr2"] = mean + 3 * std
    origin.to_csv(FILE_DIR + "dirty.csv", index=None)


def load_data():
    # input
    y = []
    f = open(FILE_DIR + "input.txt", "r")
    for line in f.readlines():
        if line.strip() != "":
            y.append(float(line.strip()))
    y = np.array(y)
    f.close()

    save = pd.DataFrame(columns=["timestamp", "value"])
    save["value"] = y
    save["timestamp"] = np.arange(len(y))
    save.to_csv(FILE_DIR + "dirty.csv", index = None)


if __name__ == '__main__':
    if len(sys.argv) >= 2:
        period = int(sys.argv[1])  # First argument

    load_data()

    reformat_data(FILE_DIR + 'dirty.csv', period)

    session = HoloClean(
        db_name='holo',
        domain_thresh_1=0,
        domain_thresh_2=0,
        weak_label_thresh=0.99,
        max_domain=10000,
        cor_strength=0.6,
        nb_cor_strength=0.8,
        epochs=10,
        weight_decay=0.01,
        learning_rate=0.001,
        threads=1,
        batch_size=1,
        verbose=True,
        timeout=3 * 60000,
        feature_norm=False,
        weight_norm=False,
        print_fw=True
    ).session


    # loading data
    session.load_data('ts', FILE_DIR + 'dirty.csv')
    session.load_dcs(FILE_DIR + "constraints.txt")
    session.ds.set_constraints(session.get_dcs())

    # 3. Detect erroneous cells using these two detectors.
    # detectors = [NullDetector(), ViolationDetector()]
    detectors = [ViolationDetector()]
    # detectors = [NullDetector()]
    session.detect_errors(detectors)

    # 4. Repair errors utilizing the defined features.
    session.setup_domain()
    featurizers = [
        # InitAttrFeaturizer(),
        OccurAttrFeaturizer(),
        FreqFeaturizer(),
        # ConstraintFeaturizer(),
    ]

    session.repair_errors(featurizers)

    repaired_df = pd.read_csv(FILE_DIR + "repair.csv")
    repair = repaired_df["seasonal"] + repaired_df["trend"] + repaired_df["resid"]

    f = open(FILE_DIR + "output.txt", "w")
    f.write("\n".join([str(item) for item in repair]))
    f.close()
