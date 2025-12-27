#!/bin/bash
echo "🚀 ServerEssentials Release Script"
read -p "Enter new version (e.g., 2.0.7.21): " VERSION

if [ -z "$VERSION" ]; then
    echo "❌ Version cannot be empty"
    exit 1
fi

echo "📦 Updating version to $VERSION..."
sed -i "s/version = '.*'/version = '$VERSION'/" build.gradle

echo "🔧 Committing version change..."
git add build.gradle
git commit -m "chore: bump version to $VERSION"

echo "🏷️ Creating tag v$VERSION..."
git tag -a "v$VERSION" -m "Release v$VERSION"

echo "⬆️ Pushing to GitHub..."
git push origin main
git push origin "v$VERSION"

echo "✅ Done! GitHub Actions will build and publish to JitPack."
echo "   Your API will be available at: com.github.GodlyCow203:ServerEssentials:api:$VERSION"