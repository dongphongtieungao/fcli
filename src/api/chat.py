from fastapi import APIRouter
from app.schemas.openai import ChatRequest
from app.services.agent import AgentService

router = APIRouter()
service = AgentService()

@router.post("/v1/chat/completions")
async def chat(req: ChatRequest):
    return await service.handle_chat(req)