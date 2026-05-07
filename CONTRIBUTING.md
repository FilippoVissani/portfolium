# Contributing

Thanks for your interest in improving Portfolium!

## Development setup

- Python 3.14+ is required.
- Create and activate a virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

- Install dependencies:

```bash
pip install -r requirements.txt
```

- Run lint and tests locally before submitting a PR:

```bash
ruff check .
pytest tests -v
```

## Commit messages

This repo uses Conventional Commits. Please format commit messages like:

- `feat(ui): add planned-expense progress card`
- `fix(yaml): handle missing transaction date gracefully`
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

Releases are automated by semantic-release from `main`.

- Conventional Commit messages determine version bumps.
- During release, `portfolium/__init__.py` version is updated automatically.
- Release notes are generated and published to GitHub.

For release maintainers only:

- Node.js `24.15` is required for the semantic-release tooling.
