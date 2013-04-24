# lma-dissenters

This is my project for the [Statistical Learning and Data Mining][sldm] class,
an undegrad course offered at UT in the spring of 2013. The goal of the project
is to determine if the difference between supporters and dissenters all
tweeting into a single hashtag can be determined automatically. 

## LMA
LMA, or LMA13, or the tag #lma13, all refer to the [2013 Legal Marketing
Association Conference][lma], held in Las Vegas between April 8th and April
10th. During this time, attendees and other supporters of the conference took
to discussing the events on twitter, using the hashtag [#lma13][lma13]. At the
same time, various dissenters took the opportunity to broadcast their
opposition to the concepts of legal marketing advanced by the LMA conference.

## Data
While it's clear that this will make for a great project, none of it can happen
without data. The code I've written:
- extracts all the usernames from an initial sample of tweets tagged with #lma13 I obtained early on
- downloads as many (non-retweet) tweets from those users as possibles
- filters out any irrelevant users and tweets to produce a proper dataset

### Proper Dataset?
The initial sample set contained users that, on further review, actually had
nothing to say about #lma13. The tweets downloaded from these users are not
included in the dataset. Furthermore, many users who did tweet about #lma13
also tweeted about things during that time period. A simple hueristic is used
to remove tweets that are not relevant.

The final and most important piece of the dataset is a set of manual
annotations, produced by myself, identifying the dissenting users in the dataset.

### Reproduction
Anyone who wishes to reproduce my results will, of course, have something of a
task on their hands, seeing as I hard coded many file paths. It should be
possible, though, to adjust the base path for these paths and replicate the
directory structure that my code expects without too much trouble. 

Even after that, though, it should be apparent that it will be hard to
replicate my results without my data - the original LMA13 tweets are already
difficult to obtain from twitter's search functions. Your best bet would be to
get in touch with me, and we can arrange a way for me to give you the curated
dataset produced by my work. Then you can plug it right in to start at the
featurization stage.

(If you want to use this for something else, go ahead. Note that I'm not
checking in my [twitter4j configuration][t4jconf]; you will need to obtain your
own api keys and set the file up yourself. Make sure to exclude retweets!)

## Contents
The contents of this repo are (or will be)
- the Scala code I developed to obtain and process the dataset, extract features, and train and evaluate the models
- a full report explaining the project and the results
- a poster that may be presented in class



[sldm]: http://www.cs.utexas.edu/~pradeepr/courses/SLDM/
[lma]: http://www.lmaconference.com/
[lma13]: https://twitter.com/search?q=#lma13&src=typd
[t4jconf]: http://twitter4j.org/en/configuration.html
