"""Provider Registry."""

from src.errors import BridgeError
from src.providers.spi import ProviderAdapter


class ProviderRegistry:
    def __init__(self, default_provider: ProviderAdapter):
        self._providers: dict[str, ProviderAdapter] = {"privategpt": default_provider}

    def get_provider(self, name: str) -> ProviderAdapter:
        if name not in self._providers:
            raise BridgeError("provider_not_found", f"Provider {name} not registered.", status_code=500)
        return self._providers[name]
