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
- Native executables (Windows, macOS, Linux) are built and attached to the GitHub release.

For release maintainers only:

- Node.js `24.15` is required for the semantic-release tooling.

## Building native executables

Portfolium can be compiled into standalone native executables using PyInstaller. This bundles Python, PySide6, and all dependencies into a single executable that requires no runtime Python installation.

### Local build

```bash
# Install build dependencies
pip install -r requirements.txt

# Build executables for your platform
python -m PyInstaller --clean --noconfirm portfolium.spec
```

Outputs:
- **Windows**: `dist/portfolium-windows.exe`
- **macOS**: `dist/Portfolium.app` (or `dist/portfolium-macos.dmg` after DMG creation)
- **Linux**: `dist/portfolium-linux`

### Automated CI/CD builds

When a release is tagged on `main` (via semantic-release), the `.github/workflows/release.yml` workflow:
1. Updates `portfolium/__init__.py` with the new version
2. Builds native executables for all platforms
3. Attaches them to the GitHub release
4. Commits version bump and CHANGELOG updates

The release script (`scripts/prepare_release.sh`) handles:
- Version string updates
- Cross-platform PyInstaller builds
- Output renaming for distribution (e.g., `portfolium.exe` → `portfolium-windows.exe`)
- macOS DMG creation from app bundles

### Known limitations

- **File size**: Executables are ~150–250 MB due to bundled Python runtime and dependencies.
- **First startup**: May be slower than running from source (Python bytecode compilation on first run).
- **Code obfuscation**: Not supported with PySide6 GUI executables.
- **Platform-specific**: Must build on each target OS; cross-compilation requires additional tooling.

### Distribution & code signing

For production releases, consider:
- **Windows**: Authenticode signing certificate to prevent "unknown publisher" warnings
- **macOS**: Apple Developer certificate + notarization for Gatekeeper
- **Linux**: Package in `.deb`/`.rpm` formats or provide checksums for integrity verification

These are optional but improve user trust and reduce security warnings.

