# language: en

@UC-001 @FR001 @FR004 @FR005
Feature: Prepare the local OpenCode PrivateGPT Bridge
  The developer needs a ready local Bridge before OpenCode can request a chat completion.
  The Bridge remains an AI integration boundary and does not perform workspace actions.

  Scenario: Start the Bridge with a valid authenticated workspace Agent
    Given the local Bridge configuration and secure token store are available
    And PrivateGPT authentication is valid
    And the internal model ID for "gemini-2.5-pro" can be resolved
    And the workspace Agent can be synchronized
    When the developer runs "privategpt-adapter start"
    Then the Bridge exposes a local OpenAI-compatible endpoint at the configured localhost address
    And the Bridge exposes the health endpoint
    And the Bridge reports the OpenCode provider endpoint
    And the Bridge does not access workspace files, run commands, or execute tools

  @UC-002 @FR002
  Scenario: Require browser login when no valid login exists
    Given the developer starts the Bridge
    And no valid PrivateGPT login is available
    When the Bridge checks authentication status
    Then the Bridge reports that login is required
    And the developer can start the browser login flow
    And a successful login stores the refresh token securely
    And the access token is retained only in memory

  @FR004
  Scenario: Stop startup when the required model cannot be resolved
    Given the developer starts the Bridge
    And PrivateGPT authentication is valid
    And the internal model ID for "gemini-2.5-pro" cannot be resolved
    When the Bridge evaluates model readiness
    Then the Bridge returns a clear model error
    And the Bridge does not substitute another model
    And the local chat endpoint is not reported as ready

