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


## k-means clustering
the implementation turned out to be simple, fairly elegant, and decently
speedy, which makes the negative results somewhat more disappointing.

### evaluation plan?
decided to evaluate across k=2 to 12 by determining, for each cluster, how many
(if any) dissenters it contained. the notion behind this is that if there was
some distinguishing factor for those dissenters, they would tend to end up in
the same cluster each time. in particular, at the higher k-values, a positive
result would mean that only one or two clusters contained all the vectors.

### negative result
however, the only consistent result (across multiple trials) was basically the
scattering of the dissenters evenly between clusters.
- at k=2, for example, the 9 dissenters are divided between the clusters nearly equally.
- from k=9 onwards, the dissenters are usually scattered between 3 or 4 clusters. this scattering tends to change between runs, reinforcing the conclusion that the dissenters are not distinguishable from the supporters.
- in between, while strong divides may sometimes be suggested, it is clear from multiple runs that these assignments are just noise.

altogether, it is clear that a negative conclusion must be drawn - the current
features, approaches, and even the dataset itself are nowhere near enough to
distinguish dissenters and supporters.
