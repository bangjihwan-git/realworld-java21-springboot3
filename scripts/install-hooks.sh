#!/bin/bash

# Git hooks installation script
# This script installs pre-commit hooks to ensure code quality

set -e

echo "🔧 Installing Git hooks..."
echo ""

# Check if .git directory exists
if [ ! -d ".git" ]; then
    echo "❌ Error: .git directory not found!"
    echo "   Please run this script from the project root directory."
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Copy pre-commit hook
if [ -f "scripts/pre-commit" ]; then
    cp scripts/pre-commit .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo "✅ Pre-commit hook installed successfully"
else
    echo "❌ Error: scripts/pre-commit not found!"
    exit 1
fi

echo ""
echo "🎉 Git hooks installation completed!"
echo ""
echo "The following hooks are now active:"
echo "  • pre-commit: Runs tests and code formatting checks before each commit"
echo ""
echo "💡 To bypass hooks in emergency situations (not recommended):"
echo "   git commit --no-verify"
echo ""

exit 0
