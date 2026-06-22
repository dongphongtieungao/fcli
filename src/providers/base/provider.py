from abc import ABC, abstractmethod

class BaseProvider(ABC):

    @abstractmethod
    async def chat_completion(self, request):
        pass