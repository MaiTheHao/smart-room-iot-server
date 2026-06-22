tree -I 'target|build|.git|.idea|.vscode|node_modules|local' > local/project_tree.txt
find . -type f \( -name "*.java" -o -name "*.md" \) -not -path "*/.*" -not -path "*/node_modules/*" -not -path "*/target/*" -not -path "*/build/*"  -not -path "*/local/*" -print0 | xargs -0 awk 'FNR==1 {print "\n=== FILE: " FILENAME " ==="} {print}' > local/all_code_context.txt
