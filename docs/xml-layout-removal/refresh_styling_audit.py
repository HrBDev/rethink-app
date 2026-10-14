#!/usr/bin/env python3
"""Rebuild the immutable baseline/snapshot audit including lossless compact storage."""
import pathlib, subprocess, sys
D = pathlib.Path(__file__).resolve().parent
for script in ('generate_styling_audit.py', 'annotate_styling_findings.py'):
    subprocess.run([sys.executable, str(D / script)], check=True)
