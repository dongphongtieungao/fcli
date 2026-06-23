"""Auth token management."""

class InMemoryAccessTokenProvider:
    def __init__(self, initial_token: str | None = None):
        self._token = initial_token

    def get_token(self) -> str | None:
        return self._token

    def set_token(self, token: str) -> None:
        self._token = token
