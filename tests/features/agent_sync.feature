# language: en

@UC-003 @FR005 @FR006
Feature: Synchronize the workspace-scoped PrivateGPT Agent
  The Bridge requires a synchronized Agent for every conversation.

  Background:
    Given PrivateGPT authentication is valid
    And the internal model ID for "gemini-2.5-pro" is resolved
    And the Bridge has the workspace context needed to derive its workspace hash

  Scenario: Create and cache a missing workspace Agent
    Given no matching workspace Agent exists in PrivateGPT
    When the developer runs "privategpt-adapter agent sync"
    Then the Bridge creates a workspace-scoped PrivateGPT Agent
    And the Agent instruction is OpenCode-compatible
    And the Agent instruction does not require IntelliJ ToolRegistry or "<tool_call>"
    And the Bridge caches the agent ID, model ID, instruction hash, and update time

  Scenario: Reuse a current workspace Agent
    Given a workspace Agent exists with the resolved model ID and current instruction hash
    When the Bridge synchronizes the Agent
    Then the Bridge reuses the existing Agent binding
    And the Bridge does not recreate the Agent for the request

  Scenario: Fail safely when Agent synchronization fails
    Given the PrivateGPT Agent API cannot complete the required Agent synchronization
    When the Bridge synchronizes the Agent
    Then the Bridge reports an explicit Agent synchronization error
    And the Bridge does not use an inline instruction fallback

