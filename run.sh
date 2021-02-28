#!/bin/bash
echo "Enter the directory where your images are located."
read -p ">> " IMAGE_DIR

SOURCE_IMAGE_DIR="${IMAGE_DIR}/source"
TARGET_IMAGE_DIR="${IMAGE_DIR}/target"
OUTPUT_IMAGE_DIR="${IMAGE_DIR}/output"

echo "Enter the sub-image resolution as two numbers separated by a space. Example: 20 20"
read -ra SUB_IMAGE_RESOLUTION -p ">> "

echo "Enter the output image granularity as two numbers separated by a space. Example: 20 20"
read -ra OUT_GRANULARITY -p ">> "

java -cp build Main $SOURCE_IMAGE_DIR $TARGET_IMAGE_DIR $OUTPUT_IMAGE_DIR ${SUB_IMAGE_RESOLUTION[0]} ${SUB_IMAGE_RESOLUTION[1]} ${OUT_GRANULARITY[0]} ${OUT_GRANULARITY[1]}

read -p "Press any key to continue: "
