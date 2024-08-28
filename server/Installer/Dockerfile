# Use an appropriate base image
FROM ubuntu:20.04

# Set environment variables to avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install required dependencies
RUN apt-get update && apt-get install -y \
    wget \
    dpkg-dev \
    rpm \
    build-essential \
    nsis \
    python3 \
    python3-pip \
    libssl-dev \
    libffi-dev \
    python3-venv \
    && apt-get clean

# Set the working directory
WORKDIR /build

# Copy the application files to the container
COPY . .

# Make the scripts executable
RUN chmod +x create_portable_python.sh build_installers.sh

# Run the script to create the portable Python environment
RUN ./create_portable_python.sh

# Execute the build script
CMD ["./build_installers.sh"]
