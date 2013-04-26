# Notes on the results

## dataset
120 examples
9 dissenters

## naive bayes

the explanation for the performance results is that the classifier predicts
everything as being a dissenting example, even though the supporter prior is
much larger. it seems that features in general tend to skew the result to the
dissenter class, suggesting (as suspected) that the features selected simply do
not come anywhere near to being good enough to distinguish the two classes.

### with features:
fold    prec    rec     acc
0       0.000000        NaN     0.000000
1       0.166667        1.000000        0.166667
2       0.250000        1.000000        0.250000
3       0.000000        NaN     0.083333
4       0.166667        1.000000        0.166667
5       0.090909        1.000000        0.166667
6       0.100000        1.000000        0.250000
7       0.000000        NaN     0.000000
8       0.000000        NaN     0.000000
9       0.000000        NaN     0.000000
avg     0.077424        NaN     0.108333

### without features
fold    prec    rec     acc
0       NaN     NaN     1.000000
1       NaN     0.000000        0.916667
2       NaN     0.000000        0.916667
3       NaN     0.000000        0.916667
4       NaN     0.000000        0.916667
5       NaN     0.000000        0.833333
6       NaN     0.000000        0.916667
7       NaN     0.000000        0.916667
8       NaN     0.000000        0.916667
9       NaN     NaN     1.000000
avg     NaN     NaN     0.925000

the high accuracy (and the NaNs) come from the fact that everything (in this
case) is instead predicted as supporting. with only 9 dissenters in the
dataset, of course it all breaks down.
