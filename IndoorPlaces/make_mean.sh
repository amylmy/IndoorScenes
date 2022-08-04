#!/usr/bin/env sh
# Compute the mean image from the training lmdb
# N.B. this is available in DATA path

LMDB_ROOT=/media/lmy/Data/PlacesData/Dataset/places160
DATA=lmy/places160
TOOLS=build/tools

$TOOLS/compute_image_mean $LMDB_ROOT/train_lmdb \
  $DATA/places160_mean.binaryproto

echo "Done."
