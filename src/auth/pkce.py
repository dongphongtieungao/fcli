"""OAuth2 PKCE helper — mirrors PkceHelper.java from the Java plugin.

The Java plugin uses PKCE with S256 code_challenge_method. This module
generates a cryptographically random code_verifier and derives the
code_challenge from it, matching the exact algorithm in PkceHelper.java.
"""

from __future__ import annotations

import base64
import hashlib
import os


def generate_code_verifier() -> str:
    """Generate a 32-byte random verifier, base64url-encoded (no padding).

    Matches: PkceHelper.generateCodeVerifier()
    """
    raw = os.urandom(32)
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode("ascii")


def generate_code_challenge(code_verifier: str) -> str:
    """SHA-256 hash of the verifier, base64url-encoded (no padding).

    Matches: PkceHelper.generateCodeChallenge(verifier)
    """
    digest = hashlib.sha256(code_verifier.encode("ascii")).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")
