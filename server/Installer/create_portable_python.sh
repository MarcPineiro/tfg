#!/bin/bash

# Define variables
PYTHON_VERSION="3.9.13"
PORTABLE_PYTHON_DIR="./portable_python"

# Clean up any previous portable Python directory
rm -rf $PORTABLE_PYTHON_DIR

# Create a new directory for the portable Python installation
mkdir -p $PORTABLE_PYTHON_DIR

# Download and extract the specified version of Python
wget https://www.python.org/ftp/python/$PYTHON_VERSION/Python-$PYTHON_VERSION.tgz
tar -xzf Python-$PYTHON_VERSION.tgz
cd Python-$PYTHON_VERSION

# Configure and install Python to the specified directory
./configure --prefix=$(pwd)/../$PORTABLE_PYTHON_DIR --enable-optimizations
make -j$(nproc)
make install

# Clean up the downloaded files
cd ..
rm -rf Python-$PYTHON_VERSION Python-$PYTHON_VERSION.tgz

# Install pip in the portable Python environment
$PORTABLE_PYTHON_DIR/bin/python3 -m ensurepip
$PORTABLE_PYTHON_DIR/bin/python3 -m pip install --upgrade pip

echo "Portable Python created in $PORTABLE_PYTHON_DIR"
