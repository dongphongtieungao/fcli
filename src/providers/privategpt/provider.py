from app.providers.base.provider import BaseProvider

class PrivateGPTProvider(BaseProvider):

    async def chat_completion(self, request):
        return {
            "model": request.model,
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": "mock response from PrivateGPT"
                    }
                }
            ]
        }