const config = require('semantic-release-preconfigured-conventional-commits')
const releaseBranches = ["main"]
config.branches = releaseBranches
config.plugins.push(
    ["@semantic-release/github", {
        "assets": [
            { "path": "dist/portfolium-source.zip" },
            { "path": "dist/portfolium-windows.exe", "label": "Windows Executable" },
            { "path": "dist/portfolium-macos.dmg", "label": "macOS Application" },
            { "path": "dist/portfolium-linux", "label": "Linux Executable" }
        ]
    }],
    ["@semantic-release/git", {
        "assets": ["CHANGELOG.md", "package.json"],
        "message": "chore(release)!: [skip ci] ${nextRelease.version} released"
    }],
)
module.exports = config