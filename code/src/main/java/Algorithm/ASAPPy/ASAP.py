import sys
import numpy as np
import scipy.stats
import numpy.fft
import math


class Metrics(object):
    def __init__(self, values):
        self.set_values(values)

    def set_values(self, values):
        self.values = values
        self.r = self.k = None

    @property
    def kurtosis(self):
        if self.k is None:
            self.k = scipy.stats.kurtosis(self.values)
        return self.k

    @property
    def roughness(self):
        if self.r is None:
            self.r = np.std(np.diff(self.values))
        return self.r


class ACF(Metrics):
    CORR_THRESH = 0.2

    def __init__(self, values, max_lag=None):
        super(ACF, self).__init__(values)
        if max_lag is None:
            max_lag = len(values) / 5
        self.max_lag = int(max_lag)
        self.max_acf = 0.0

        # Calculate autocorrelation via FFT
        # Demean
        demeaned = values - np.mean(values)
        # Pad data to power of 2
        l = int(2.0 ** (int(math.log(len(demeaned), 2.0)) + 1))
        padded = np.append(demeaned, ([0.0] * (l - len(demeaned))))
        # FFT and inverse FFT
        F_f = numpy.fft.fft(padded)
        R_t = numpy.fft.ifft(F_f * np.conjugate(F_f))
        self.correlations = R_t[:max_lag].real / R_t[0].real

        # Find autocorrelation peaks
        self.peaks = []
        if len(self.correlations) > 1:
            positive = self.correlations[1] > self.correlations[0]
            max = 1
            for i in range(2, len(self.correlations)):
                if not positive and self.correlations[i] > self.correlations[i - 1]:
                    max = i
                    positive = not positive
                elif positive and self.correlations[i] > self.correlations[max]:
                    max = i
                elif positive and self.correlations[i] < self.correlations[i - 1]:
                    if max > 1 and self.correlations[max] > self.CORR_THRESH:
                        self.peaks.append(max)
                        if self.correlations[max] > self.max_acf:
                            self.max_acf = self.correlations[max]
                    positive = not positive
        # If there is no autocorrelation peak within the MAX_WINDOW boundary,
        # try windows from the largest to the smallest
        if len(self.peaks) <= 1:
            self.peaks = range(2, len(self.correlations))


def moving_average(data, _range):
    ret = np.cumsum(data)
    ret[_range:] = ret[_range:] - ret[:-_range]
    return ret[_range - 1:] / _range


def SMA(data, _range, slide):
    ret = moving_average(data, _range)[::slide]
    return list(ret)


def binary_search(head, tail, data, min_obj, orig_kurt, window_size):
    while head <= tail:
        w = int(round((head + tail) / 2.0))
        smoothed = SMA(data, w, 1)
        metrics = Metrics(smoothed)
        if metrics.kurtosis >= orig_kurt:
            if metrics.roughness < min_obj:
                window_size = w
                min_obj = metrics.roughness
            head = w + 1
        else:
            tail = w - 1
    return window_size


def smooth_ASAP(data, max_window=5, resolution=None):
    data = np.array(data)
    # Preaggregate according to resolution
    slide_size = 1
    window_size = 1
    if resolution and len(data) >= 2 * resolution:
        slide_size = len(data) // resolution
        data = SMA(data, slide_size, slide_size)
    acf = ACF(data, max_lag=len(data) // max_window)
    peaks = acf.peaks
    orig_kurt = acf.kurtosis
    min_obj = acf.roughness
    lb = 1
    largest_feasible = -1
    tail = len(data) / max_window
    for i in range(len(peaks) - 1, -1, -1):
        w = peaks[i]

        if w < lb or w == 1:
            break
        elif math.sqrt(1 - acf.correlations[w]) * window_size > math.sqrt(1 - acf.correlations[window_size]) * w:
            continue

        smoothed = SMA(data, w, 1)
        metrics = Metrics(smoothed)
        if metrics.roughness < min_obj and metrics.kurtosis >= orig_kurt:
            min_obj = metrics.roughness
            window_size = w
            lb = round(max(w * math.sqrt((acf.max_acf - 1) / (acf.correlations[w] - 1)), lb))
    if largest_feasible > 0:
        if largest_feasible < len(peaks) - 2:
            tail = peaks[largest_feasible + 1]
        lb = max(lb, peaks[largest_feasible] + 1)

    window_size = binary_search(lb, tail, data, min_obj, orig_kurt, window_size)
    return window_size, slide_size


def ASAP(ts, window_size=None):
    slide_size = 1
    if window_size is None:
        window_size, slide_size = smooth_ASAP(ts, max_window=99999999999, resolution=99999999999)
    data = SMA(ts, slide_size, slide_size)
    smoothed = SMA(data, window_size, 1)
    smoothed_l, smoothed_r = int(window_size / 2), int(window_size / 2) + len(smoothed) - 1

    repair = np.zeros(len(data))
    for ii in range(len(repair)):
        if ii < smoothed_l or ii > smoothed_r:
            repair[ii] = data[ii]
        else:
            repair[ii] = smoothed[ii - smoothed_l]
    return repair


if __name__ == '__main__':
    window_size = None
    if len(sys.argv) >= 2:
        window_size = int(sys.argv[1])  # First argument

    FILE_DIR = "/Users/chenzijie/Documents/GitHub/repair-seasonal-exp/src/main/java/Algorithm/ASAPPy/"
    # input
    y = []
    f = open(FILE_DIR + "input.txt", "r")
    for line in f.readlines():
        if line.strip() != "":
            y.append(float(line.strip()))
    y = np.array(y)
    f.close()

    repair = ASAP(y, window_size)

    f = open(FILE_DIR + "output.txt", "w")
    f.write("\n".join([str(item) for item in repair]))
    f.close()
