const config = require('semantic-release-preconfigured-conventional-commits')
const publishCommands = `
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md || exit 1
git push --force origin \${nextRelease.version} || exit 2
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
            { "path": "build/libs/*.jar" }
        ]
    }],
    ["@semantic-release/git", {
        "assets": ["CHANGELOG.md", "package.json", "build.gradle.kts"],
        "message": "chore(release)!: [skip ci] ${nextRelease.version} released"
    }],
)
module.exports = config