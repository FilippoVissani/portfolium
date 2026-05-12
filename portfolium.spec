# -*- mode: python ; coding: utf-8 -*-
"""
PyInstaller spec file for Portfolium.
Builds a standalone executable that bundles Python, PySide6, and all dependencies.

Usage:
    pyinstaller portfolium.spec
"""

import sys
from PyInstaller.utils.hooks import collect_data_files

block_cipher = None

# Collect hidden imports required by dependencies
hidden_imports = [
    'PySide6',
    'PySide6.QtCore',
    'PySide6.QtGui',
    'PySide6.QtWidgets',
    'yfinance',
    'numpy',
    'pandas',
    'matplotlib',
    'matplotlib.backends.backend_qtagg',
    'pyqtgraph',
    'yaml',
]

# Collect data files from dependencies (fonts, plugins, etc.)
datas = [
    ('example_data', 'example_data'),  # Include example YAML files
]

# Add PySide6 data files
datas += collect_data_files('PySide6')

a = Analysis(
    ['run_portfolium.py'],
    pathex=[],
    binaries=[],
    datas=datas,
    hiddenimports=hidden_imports,
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludedimports=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.zipfiles,
    a.datas,
    [],
    name='portfolium',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,  # No console window on Windows
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=None,  # Optional: set to 'portfolium-logo.ico' if icon file exists
)

# For macOS: create an app bundle
if sys.platform == 'darwin':
    app = BUNDLE(
        exe,
        name='Portfolium.app',
        icon=None,  # Optional: set to 'portfolium-logo.icns' if icon file exists
        bundle_identifier='com.filippo.portfolium',
        info_plist={
            'NSPrincipalClass': 'NSApplication',
            'NSHighResolutionCapable': 'True',
        },
    )


