#!/bin/bash
echo ">>> Kochi-Justice V6.1.6: Final Reconstruction..."

# Force kill any running ERP or JRE instances
osascript -e 'do shell script "pkill -f Kerala-Police-Justice-ERP || true" with administrator privileges'
osascript -e 'do shell script "pkill -f crime-management-system || true" with administrator privileges'

# Detach all previous versions to avoid "Busy" errors
hdiutil detach /Volumes/Kerala-Police-Justice-ERP 2>/dev/null || true

echo ">>> Kochi-Justice V6.1.6: Purging legacy application lock-files..."
osascript -e 'do shell script "rm -rf /Applications/Kerala-Police-Justice-ERP.app" with administrator privileges'

xattr -cr target/Kerala-Police-Justice-ERP-6.1.6.dmg
hdiutil mount target/Kerala-Police-Justice-ERP-6.1.6.dmg

echo ">>> Kochi-Justice V6.1.6: Deploying Flat-Library Kochi Director App..."
osascript -e 'do shell script "cp -R \"/Volumes/Kerala-Police-Justice-ERP/Kerala-Police-Justice-ERP.app\" \"/Applications/\"" with administrator privileges'

# Unmount after copying
hdiutil detach /Volumes/Kerala-Police-Justice-ERP

# Strip quarantine from the final application
osascript -e 'do shell script "xattr -rd com.apple.quarantine /Applications/Kerala-Police-Justice-ERP.app" with administrator privileges'

open -R /Applications/Kerala-Police-Justice-ERP.app
