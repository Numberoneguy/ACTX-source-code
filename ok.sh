#!/bin/bash

# Base directory for Minecraft assets
BASE_DIR="/root/ACTX-source-code/desktopRuntime/resources/assets/minecraft/models"

# Target directories for block and item models
BLOCK_DIR="$BASE_DIR/block"
ITEM_DIR="$BASE_DIR/item"

# Create target directories if they don't exist
mkdir -p "$BLOCK_DIR"
mkdir -p "$ITEM_DIR"

# Array of all standard Minecraft shulker boxes (default + 16 colors)
shulker_boxes=(
    "shulker_box"
    "white_shulker_box"
    "orange_shulker_box"
    "magenta_shulker_box"
    "light_blue_shulker_box"
    "yellow_shulker_box"
    "lime_shulker_box"
    "pink_shulker_box"
    "gray_shulker_box"
    "silver_shulker_box"
    "cyan_shulker_box"
    "purple_shulker_box"
    "blue_shulker_box"
    "brown_shulker_box"
    "green_shulker_box"
    "red_shulker_box"
    "black_shulker_box"
)

# Loop through each shulker box and generate both block and item JSONs
for box in "${shulker_boxes[@]}"; do
    block_file="$BLOCK_DIR/${box}.json"
    item_file="$ITEM_DIR/${box}.json"
    
    # Write Block JSON
    cat << EOF > "$block_file"
{
    "parent": "builtin/entity"
}
EOF

    # Write Item JSON
    cat << EOF > "$item_file"
{
    "parent": "builtin/entity"
}
EOF

    echo "Generated: ${box}.json for both block and item models."
done

echo "All 34 shulker box model files (17 blocks + 17 items) have been successfully created!"
