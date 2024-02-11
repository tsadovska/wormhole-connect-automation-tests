Feature: EVM automatic testnet

  Scenario Outline: EVM automatic testnet
    Given I launch testnet browser
    Given I open wormhole-connect testnet
    And I enter page password
    And I check native balance on "<to_network>" using "<to_wallet>"
    Given I open wormhole-connect testnet
    And I prepare to send "<amount>" "<asset>" from "<from_network>"("<from_wallet>") to "<to_network>"("<to_wallet>") with "<route>" route
    And I move slider
    When I click on Approve button
    When I approve wallet notifications
    Then I should see Send From link
    Then I should see Send To link
    Then I check balance has increased on destination chain

    Examples:
      | route     | amount | asset | from_network | to_network | from_wallet | to_wallet |
#      | automatic | 0.2    | MATIC | Mumbai       | Alfajores  | MetaMask    | MetaMask  |
      | automatic | 0.008  | BNB   | BSC          | Goerli     | MetaMask    | MetaMask  |
