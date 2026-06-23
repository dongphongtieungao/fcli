# language: en

@UC-004 @FR007 @FR008 @FR009 @FR010
Feature: Return OpenAI-compatible chat completions through the synchronized Agent
  OpenCode receives PrivateGPT assistant content while retaining responsibility for tools and approvals.

  Background:
    Given the local Bridge is running
    And PrivateGPT authentication is valid
    And a workspace Agent is synchronized and has an agent ID
    And OpenCode is configured to use "privategpt/gemini-2.5-pro"

  Scenario: Return a non-streaming chat completion
    Given OpenCode has messages for a coding task
    When OpenCode sends "POST /v1/chat/completions" with model "gemini-2.5-pro" and stream set to false
    Then the Bridge validates the request and model
    And the Bridge sends a PrivateGPT ChatRequest with the internal model ID and "metadata.agent_id"
    And the Bridge sends an empty tools list to PrivateGPT
    And the Bridge returns visible assistant content in an OpenAI-compatible non-stream response
    And the Bridge does not execute any tool requested or emitted during the interaction

  Scenario: Relay a streaming chat completion
    Given OpenCode has messages for a coding task
    When OpenCode sends "POST /v1/chat/completions" with model "gemini-2.5-pro" and stream set to true
    Then the Bridge maps and relays OpenAI-compatible SSE chunks from PrivateGPT
    And the Bridge ends the stream with "[DONE]"

  @FR004
  Scenario: Reject an unsupported model without fallback
    Given OpenCode sends a chat completion request with an unsupported model
    When the Bridge validates the requested model
    Then the Bridge rejects the request as "model_not_supported"
    And the Bridge does not call PrivateGPT with a different model

  @FR012
  Scenario: Reject an unsupported tool requirement without executing it
    Given OpenCode sends a tool requirement that cannot be safely ignored
    When the Bridge validates the tool requirement
    Then the Bridge rejects the request clearly
    And the Bridge does not execute the tool

  @FR009
  Scenario: Retry only before the first response chunk
    Given PrivateGPT has not produced a relayed response chunk
    And a transient upstream failure occurs
    When the Bridge applies its bounded resilience policy
    Then the Bridge may retry the request before relaying output
    But the Bridge does not retry the whole request after the first response chunk has been relayed

  @FR009
  Scenario: Close the upstream stream when the client cancels
    Given the Bridge is relaying a PrivateGPT response stream
    When OpenCode cancels the chat completion request or disconnects
    Then the Bridge closes the upstream stream
    And the Bridge releases the associated connection
    And the Bridge does not leave background work running
