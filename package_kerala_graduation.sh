#!/bin/bash
# Kerala Police ERP V6.2.0 Kochi-Justice Graduation Packager
APP_NAME="Kerala Police ERP"
JAR_NAME="crime-management-system-1.0.1-JUSTICE.jar"
STAGING="dmg_staging_v6_final"

echo ">>> Resetting Staging Environment..."
rm -rf "$STAGING"
mkdir -p "$STAGING/$APP_NAME.app/Contents/MacOS"
mkdir -p "$STAGING/$APP_NAME.app/Contents/MacOS"
mkdir -p "$STAGING/$APP_NAME.app/Contents/Resources"
ln -s /Applications "$STAGING/Applications"

echo ">>> Embedding Kerala Runtime..."
cp "target/$JAR_NAME" "$STAGING/$APP_NAME.app/Contents/Resources/app.jar"

# Mac App Launcher Script
cat > "$STAGING/$APP_NAME.app/Contents/MacOS/KeralaPoliceERP" <<EOF
#!/bin/bash
DIR="\$(cd "\$(dirname "\$0")" && pwd)"
ICON="\$DIR/../Resources/app.icns"

# SPRING BOOT 3.2 JUSTICE LAUNCHER + MAC DOCK BRANDING
java -Xdock:icon="\$ICON" \
     -Xdock:name="Kerala Police ERP" \
     -Dapple.awt.application.name="Kerala Police ERP" \
     -Dcom.apple.mrj.application.apple.menu.about.name="Kerala Police ERP" \
     -Dapple.laf.useScreenMenuBar=true \
     -Dapple.awt.application.appearance=system \
     -cp "\$DIR/../Resources/app.jar" \
     -Dloader.main=com.crimemanagement.DesktopLauncher \
     org.springframework.boot.loader.launch.JarLauncher
EOF
chmod +x "$STAGING/$APP_NAME.app/Contents/MacOS/KeralaPoliceERP"

# Info.plist
cat > "$STAGING/$APP_NAME.app/Contents/Info.plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>KeralaPoliceERP</string>
    <key>CFBundleIconFile</key>
    <string>app.icns</string>
    <key>CFBundleName</key>
    <string>Kerala Police ERP</string>
    <key>CFBundleDisplayName</key>
    <string>Kerala Police ERP</string>
    <key>CFBundleIdentifier</key>
    <string>com.keralapolice.erp</string>
    <key>CFBundleVersion</key>
    <string>6.6.0</string>
    <key>CFBundleShortVersionString</key>
    <string>6.6.0</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleSignature</key>
    <string>KPLP</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.15.0</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

if [ -f "icons/KeralaPolice.icns" ]; then
    cp "icons/KeralaPolice.icns" "$STAGING/$APP_NAME.app/Contents/Resources/app.icns"
fi

echo ">>> Generating Graduation DMG..."
hdiutil create -volname "Kerala Police ERP" -srcfolder "$STAGING" -ov -format UDZO -size 300m "target/Kerala-Police-ERP-V6-FINAL.dmg"

echo ">>> GRADUATION COMPLETE: target/Kerala-Police-ERP-V6-FINAL.dmg"
