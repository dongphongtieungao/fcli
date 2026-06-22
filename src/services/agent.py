from app.providers.registry import ProviderRegistry

class AgentService:
    def __init__(self):
        self.registry = ProviderRegistry()

    async def handle_chat(self, req):
        provider = self.registry.resolve(req.model)
        return await provider.chat_completion(req)