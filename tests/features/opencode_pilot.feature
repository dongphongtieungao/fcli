# language: en

@UC-005 @FR013
Feature: Validate the Bridge by direct smoke test and controlled OpenCode pilot
  The developer can prove the local integration before using it for a coding task.

  Scenario: Run the documented direct HTTP smoke test
    Given the Bridge is running with valid login and a synchronized workspace Agent
    When the developer sends the documented simple non-streaming request to "/v1/chat/completions" using "gemini-2.5-pro"
    Then the Bridge returns a compatible assistant response to the simple prompt
    And the request is routed through the synchronized Agent

  @UC-006 @FR011 @FR012
  Scenario: Perform a non-destructive analysis in OpenCode
    Given the Bridge and OpenCode provider configuration are ready
    When the developer selects "privategpt/gemini-2.5-pro" and asks OpenCode to analyze repository structure without editing
    Then OpenCode receives analysis through the Bridge
    And the Bridge does not execute file operations or commands

  @UC-006 @FR011
  Scenario: Require OpenCode approval for a proposed small fix
    Given OpenCode has received a response through the Bridge
    When the developer asks OpenCode to propose a small code fix
    Then OpenCode requests approval before an edit action
    And OpenCode requests approval before a test or command action
    And the Bridge does not perform an alternate tool action

  @UC-006
  Scenario: Respect a declined OpenCode approval
    Given OpenCode has requested approval for an edit or command action
    When the developer declines the approval
    Then OpenCode does not perform the declined action
    And the Bridge does not execute the declined action

