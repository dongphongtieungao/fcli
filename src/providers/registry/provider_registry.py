from app.providers.privategpt.provider import PrivateGPTProvider

class ProviderRegistry:
    def __init__(self):
        self.providers = {
            "privategpt": PrivateGPTProvider()
        }

    def resolve(self, model: str):
        return self.providers["privategpt"]