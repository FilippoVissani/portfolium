# Contributing

Thanks for your interest in improving Portfolium!

## Development setup

- Java 21 and the Gradle Wrapper are required (wrapper included).
- Run tests locally before submitting a PR:

```zsh
./gradlew test
```

## Commit messages

This repo uses Conventional Commits. Please format commit messages like:

- `feat(cli): add support for custom price CSV`
- `fix(csv): handle invalid dates gracefully`
- `docs: update README with usage examples`

Breaking changes should include a `BREAKING CHANGE:` footer or use `!` after type/scope:

```
feat!: rename investment ticker column

BREAKING CHANGE: the `symbol` column is now `ticker`.
```

## Pull requests

- Keep PRs focused and small when possible
- Include tests for new behavior or bug fixes
- Update documentation (README/CHANGELOG) when relevant

## Release process

Releases are automated by semantic-release. When Conventional Commits are merged into `main`, a release will be created with notes and the built JAR attached.

