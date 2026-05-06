const config = require('semantic-release-preconfigured-conventional-commits')
const releaseBranches = ["main"]
config.branches = releaseBranches
config.plugins.push(
    ["@semantic-release/exec", {
        "prepareCmd": "sed -i 's/^__version__ = \".*\"/__version__ = \"${nextRelease.version}\"/' portfolium/__init__.py",
    }],
    ["@semantic-release/github", {
        "assets": [
            { "path": "dist/portfolium-source.zip" }
        ]
    }],
    ["@semantic-release/git", {
        "assets": ["CHANGELOG.md", "package.json", "portfolium/__init__.py"],
        "message": "chore(release)!: [skip ci] ${nextRelease.version} released"
    }],
)
module.exports = config