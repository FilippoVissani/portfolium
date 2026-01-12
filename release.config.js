const config = require('semantic-release-preconfigured-conventional-commits')
const publishCommands = `
./gradlew --no-daemon build || exit 1
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md || exit 2
git push --force origin \${nextRelease.version} || exit 3
`
const releaseBranches = ["main"]
config.branches = releaseBranches
config.plugins.push(
    ["@semantic-release/exec", {
        "prepareCmd": "sed -i 's/^version = \".*\"/version = \"${nextRelease.version}\"/' build.gradle.kts",
        "publishCmd": publishCommands,
    }],
    ["@semantic-release/github", {
        "assets": [
            { "path": "build/libs/*.jar" },
            { "path": "build/native/nativeCompile/portfolium-linux-amd64" },
            { "path": "build/native/nativeCompile/portfolium-macos-amd64" },
            { "path": "build/native/nativeCompile/portfolium-windows-amd64.exe" }
        ]
    }],
    ["@semantic-release/git", {
        "assets": ["CHANGELOG.md", "package.json", "build.gradle.kts"],
        "message": "chore(release)!: [skip ci] ${nextRelease.version} released"
    }],
)
module.exports = config