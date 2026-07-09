#!/bin/bash
# scripts/migrate-service-package.sh
# Service Package Restructuring Automation Script
# Run from project root: bash scripts/migrate-service-package.sh

set -euo pipefail

BASE="src/main/java/com/iviet/ivshs/service"
DRY_RUN="${DRY_RUN:-false}"

echo "=== Service Package Restructuring ==="
echo "Base: $BASE"
echo "Dry run: $DRY_RUN"
echo ""

# Step 1: Create target directories
echo "[1/5] Creating target directories..."
mkdir -p "$BASE/impl" "$BASE/strategy" "$BASE/factory"

# Step 2: Discover and move files
echo "[2/5] Moving files with git mv..."

# Track all git mv commands for summary
declare -a MOVED_FILES

move_file() {
    local src="$1" dst="$2"
    if [ ! -f "$src" ]; then
        echo "  WARNING: Source not found: $src"
        return
    fi
    if [ "$DRY_RUN" = "true" ]; then
        echo "  [DRY RUN] git mv $src $dst"
    else
        git mv "$src" "$dst"
        echo "  MOVED: $(basename $src) -> $dst"
    fi
    MOVED_FILES+=("$src")
}

# Move Strategy files
echo "  --- strategy/ ---"
for f in $(find "$BASE" -name "*Strategy*.java" -type f); do
    move_file "$f" "$BASE/strategy/"
done

# Move Orchestrator files
echo "  --- factory/ ---"
for f in $(find "$BASE" -name "*Orchestrator*.java" -type f); do
    move_file "$f" "$BASE/factory/"
done

# Move Impl files (anything in an impl/ directory, plus token/provider)
echo "  --- impl/ ---"
for f in $(find "$BASE" -path "*/impl/*.java" -type f); do
    move_file "$f" "$BASE/impl/"
done
# Also move token/provider files
for f in $(find "$BASE/token/provider" -name "*.java" -type f 2>/dev/null); do
    move_file "$f" "$BASE/impl/"
done

# Move remaining interface files (everything else left in subpackages)
echo "  --- service/ (interfaces) ---"
for f in $(find "$BASE" -name "*.java" -type f ! -path "*/impl/*" ! -path "*/strategy/*" ! -path "*/factory/*"); do
    move_file "$f" "$BASE/"
done

# Step 3: Update package declarations
echo "[3/5] Updating package declarations..."

for f in "$BASE"/*.java; do
    [ -f "$f" ] || continue
    sed -i 's/^package com\.iviet\.ivshs\.service\..*/package com.iviet.ivshs.service;/' "$f"
done

for f in "$BASE"/impl/*.java; do
    [ -f "$f" ] || continue
    sed -i 's/^package com\.iviet\.ivshs\.service\..*/package com.iviet.ivshs.service.impl;/' "$f"
done

for f in "$BASE"/strategy/*.java; do
    [ -f "$f" ] || continue
    sed -i 's/^package com\.iviet\.ivshs\.service\..*/package com.iviet.ivshs.service.strategy;/' "$f"
done

for f in "$BASE"/factory/*.java; do
    [ -f "$f" ] || continue
    sed -i 's/^package com\.iviet\.ivshs\.service\..*/package com.iviet.ivshs.service.factory;/' "$f"
done

# Step 4: Update imports project-wide
echo "[4/5] Updating imports project-wide..."

# Strategy imports
for f in $(grep -rln "import com\.iviet\.ivshs\.service.*Strategy;" src/ --include="*.java" 2>/dev/null); do
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.\([A-Za-z]*Strategy;\)/import com.iviet.ivshs.service.strategy.\1/' "$f"
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.[a-z][a-z]*\.\([A-Za-z]*Strategy;\)/import com.iviet.ivshs.service.strategy.\1/' "$f"
done

# Orchestrator imports
for f in $(grep -rln "import com\.iviet\.ivshs\.service.*Orchestrator;" src/ --include="*.java" 2>/dev/null); do
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.\([A-Za-z]*Orchestrator;\)/import com.iviet.ivshs.service.factory.\1/' "$f"
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.[a-z][a-z]*\.\([A-Za-z]*Orchestrator;\)/import com.iviet.ivshs.service.factory.\1/' "$f"
done

# Impl imports (classes in .*.impl. and .token.provider.)
for f in $(grep -rln "import com\.iviet\.ivshs\.service\..*\.impl\." src/ --include="*.java" 2>/dev/null); do
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.impl\.\([A-Z]\)/import com.iviet.ivshs.service.impl.\1/' "$f"
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z][a-z]*\.[a-z][a-z]*\.impl\.\([A-Z]\)/import com.iviet.ivshs.service.impl.\1/' "$f"
done
for f in $(grep -rln "import com\.iviet\.ivshs\.service\.token\.provider\." src/ --include="*.java" 2>/dev/null); do
    sed -i 's/import com\.iviet\.ivshs\.service\.token\.provider\.\([A-Z]\)/import com.iviet.ivshs.service.impl.\1/' "$f"
done

# Interface imports (remaining old subpackage imports)
OLD_PACKAGES="aircondition alert auth automation base client clientdevice control fan floor hardwareconfig language light metric notification permission powerconsumption role room rule schedule sensor setup system telemetry temperature token user device energy"
for pkg in $OLD_PACKAGES; do
    for f in $(grep -rln "import com\.iviet\.ivshs\.service\.${pkg}\.[A-Z]" src/ --include="*.java" 2>/dev/null); do
        sed -i "s/import com\\.iviet\\.ivshs\\.service\\.${pkg}\\.\\([A-Z]\\)/import com.iviet.ivshs.service.\\1/" "$f"
    done
done
# Also handle two-level subpackages like alert.event or notification.strategy
for f in $(grep -rln "import com\.iviet\.ivshs\.service\.[a-z]\+\.[a-z]\+\.[A-Z]" src/ --include="*.java" 2>/dev/null); do
    sed -i 's/import com\.iviet\.ivshs\.service\.[a-z]\+\.[a-z]\+\.\([A-Z]\)/import com.iviet.ivshs.service.\1/' "$f"
done

# Fix inline fully-qualified references (e.g., "extends com.iviet.ivshs.service.control.SensorMetadataServiceStrategy")
for f in $(grep -rln "com\.iviet\.ivshs\.service\.[a-z]\+\.[A-Z]" src/ --include="*.java" 2>/dev/null); do
    for pkg in $OLD_PACKAGES; do
        sed -i "s/com\\.iviet\\.ivshs\\.service\\.${pkg}\\.\\([A-Za-z]\\)/com.iviet.ivshs.service.\\1/g" "$f"
    done
done
# Also fix inline two-level references
for f in $(grep -rln "com\.iviet\.ivshs\.service\.[a-z]\+\.[a-z]\+\.[A-Z]" src/ --include="*.java" 2>/dev/null); do
    sed -i 's/com\.iviet\.ivshs\.service\.[a-z]\+\.[a-z]\+\.\([A-Za-z]\)/com.iviet.ivshs.service.\1/g' "$f"
done

# Add missing Strategy imports for files that relied on same-package visibility
# Files moved from subpackages that use Strategy classes need explicit imports now
echo "  Checking for files needing Strategy imports..."
for f in $(grep -rln "extends.*DeviceControlServiceStrategy\|implements.*DeviceControlServiceStrategy" "$BASE" --include="*.java" 2>/dev/null); do
    if ! grep -q "import com\.iviet\.ivshs\.service\.strategy\.DeviceControlServiceStrategy" "$f" 2>/dev/null; then
        sed -i 's/^package/import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;\npackage/' "$f"
        echo "    Added DeviceControlServiceStrategy import to $f"
    fi
done
for f in $(grep -rln "extends.*MetricServiceStrategy\|implements.*MetricServiceStrategy" "$BASE" --include="*.java" 2>/dev/null); do
    if ! grep -q "import com\.iviet\.ivshs\.service\.strategy\.MetricServiceStrategy" "$f" 2>/dev/null; then
        sed -i 's/^package/import com.iviet.ivshs.service.strategy.MetricServiceStrategy;\npackage/' "$f"
        echo "    Added MetricServiceStrategy import to $f"
    fi
done
for f in $(grep -rln "extends.*TokenStrategy\|implements.*TokenStrategy\|[^a-z]TokenStrategy[^a-z]" "$BASE" --include="*.java" 2>/dev/null); do
    if ! grep -q "import com\.iviet\.ivshs\.service\.strategy\.TokenStrategy" "$f" 2>/dev/null; then
        sed -i 's/^package/import com.iviet.ivshs.service.strategy.TokenStrategy;\npackage/' "$f"
        echo "    Added TokenStrategy import to $f"
    fi
done

# Step 5: Clean up empty directories
echo "[5/5] Cleaning up empty directories..."
find "$BASE" -type d -empty -delete 2>/dev/null || true

echo ""
echo "=== Migration Complete ==="
echo ""
echo "Summary:"
echo "  Total files moved: ${#MOVED_FILES[@]}"
echo "  Target directories:"
echo "    $(ls -1 "$BASE"/*.java 2>/dev/null | wc -l) files in service/"
echo "    $(ls -1 "$BASE"/impl/*.java 2>/dev/null | wc -l) files in service/impl/"
echo "    $(ls -1 "$BASE"/strategy/*.java 2>/dev/null | wc -l) files in service/strategy/"
echo "    $(ls -1 "$BASE"/factory/*.java 2>/dev/null | wc -l) files in service/factory/"
echo ""
echo "Next steps:"
echo "  1. Run: mvn clean compile -q"
echo "  2. Run: mvn test -q"
echo "  3. Run: git add -A && git commit -m \"refactor: flatten service package hierarchy\""
