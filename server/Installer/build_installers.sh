#!/bin/bash

# Define the paths and names
APP_NAME="MyApp"
VERSION="1.0.0"
BUILD_DIR="./build"
DIST_DIR="/output" # Output directory will be a mounted volume
PYTHON_APP_DIR="./myapp"
PORTABLE_PYTHON_DIR="./portable_python"
NSIS_SCRIPT="myapp_installer.nsi"
NSIS_OUTPUT="$DIST_DIR/$APP_NAME-Installer.exe"

# Create necessary directories
mkdir -p $BUILD_DIR
mkdir -p $DIST_DIR

# Step 1: Generate the Windows Installer using NSIS
echo "Generating Windows Installer..."
makensis $NSIS_SCRIPT

if [ $? -eq 0 ]; then
    echo "Windows Installer created: $NSIS_OUTPUT"
else
    echo "NSIS installer creation failed!"
    exit 1
fi

# Step 2: Create a Debian package (.deb)
echo "Creating Debian package..."

DEB_DIR="$BUILD_DIR/$APP_NAME-$VERSION-deb"
mkdir -p $DEB_DIR/DEBIAN
mkdir -p $DEB_DIR/usr/local/$APP_NAME

# Copy application files to the DEB structure
cp -r $PYTHON_APP_DIR/* $DEB_DIR/usr/local/$APP_NAME/
cp -r $PORTABLE_PYTHON_DIR/* $DEB_DIR/usr/local/$APP_NAME/

# Create the control file for the package
cat <<EOL > $DEB_DIR/DEBIAN/control
Package: $APP_NAME
Version: $VERSION
Section: base
Priority: optional
Architecture: all
Depends: python3, python3-pip, libssl-dev, libffi-dev
Maintainer: Your Name <youremail@example.com>
Description: $APP_NAME is a Python-based application packaged for Debian-based systems.
EOL

dpkg-deb --build $DEB_DIR $DIST_DIR/$APP_NAME-$VERSION.deb

if [ $? -eq 0 ]; then
    echo "Debian package created: $DIST_DIR/$APP_NAME-$VERSION.deb"
else
    echo "Debian package creation failed!"
    exit 1
fi

# Step 3: Create an RPM package (.rpm)
echo "Creating RPM package..."

RPM_DIR="$BUILD_DIR/$APP_NAME-$VERSION-rpm"
mkdir -p $RPM_DIR/usr/local/$APP_NAME

# Copy application files to the RPM structure
cp -r $PYTHON_APP_DIR/* $RPM_DIR/usr/local/$APP_NAME/
cp -r $PORTABLE_PYTHON_DIR/* $RPM_DIR/usr/local/$APP_NAME/

# Create the spec file for the RPM
cat <<EOL > $BUILD_DIR/$APP_NAME.spec
Name:           $APP_NAME
Version:        $VERSION
Release:        1%{?dist}
Summary:        $APP_NAME is a Python-based application packaged for Red Hat-based systems.

License:        Your License
URL:            http://example.com
Source0:        %{name}-%{version}.tar.gz

BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

Requires:       python3, python3-pip, libssl, libffi

%description
$APP_NAME is a Python-based application packaged for Red Hat-based systems.

%prep

%build

%install
mkdir -p %{buildroot}/usr/local/$APP_NAME
cp -r $PYTHON_APP_DIR/* %{buildroot}/usr/local/$APP_NAME/
cp -r $PORTABLE_PYTHON_DIR/* %{buildroot}/usr/local/$APP_NAME/

%files
/usr/local/$APP_NAME

%changelog
EOL

rpmbuild -bb --buildroot=$RPM_DIR $BUILD_DIR/$APP_NAME.spec --define "_rpmdir $DIST_DIR"

if [ $? -eq 0 ]; then
    echo "RPM package created: $DIST_DIR/$APP_NAME-$VERSION.rpm"
else
    echo "RPM package creation failed!"
    exit 1
fi

# Step 4: Create an AppImage for universal Linux compatibility
echo "Creating AppImage..."

APPIMAGE_DIR="$BUILD_DIR/$APP_NAME-$VERSION-appimage"
mkdir -p $APPIMAGE_DIR/usr/bin
mkdir -p $APPIMAGE_DIR/usr/share/applications
mkdir -p $APPIMAGE_DIR/usr/share/icons/hicolor/256x256/apps

# Copy application files to the AppImage structure
cp -r $PYTHON_APP_DIR/* $APPIMAGE_DIR/usr/bin/
cp -r $PORTABLE_PYTHON_DIR/* $APPIMAGE_DIR/usr/bin/

# Create a desktop entry
cat <<EOL > $APPIMAGE_DIR/$APP_NAME.desktop
[Desktop Entry]
Name=$APP_NAME
Exec=/usr/bin/python3 /usr/bin/main.py
Icon=$APP_NAME
Type=Application
Categories=Utility;
EOL

# Copy icon to the correct location
cp $PYTHON_APP_DIR/app_icon.png $APPIMAGE_DIR/usr/share/icons/hicolor/256x256/apps/$APP_NAME.png

# Download and use AppImageTool to create the AppImage
wget -q https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool-x86_64.AppImage

./appimagetool-x86_64.AppImage $APPIMAGE_DIR $DIST_DIR/$APP_NAME-$VERSION-x86_64.AppImage

if [ $? -eq 0 ]; then
    echo "AppImage created: $DIST_DIR/$APP_NAME-$VERSION-x86_64.AppImage"
else
    echo "AppImage creation failed!"
    exit 1
fi

# Cleanup
rm -rf $BUILD_DIR
rm appimagetool-x86_64.AppImage

echo "All installers created successfully and are available in the $DIST_DIR directory."
