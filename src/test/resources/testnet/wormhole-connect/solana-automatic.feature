Feature: Automatic route support to and from Solana on testnet

  Scenario Outline:
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
      | route                  | amount | asset | from_network | to_network | from_wallet | to_wallet |
      | xlabs-bridge-automatic | 0.03   | SOL   | Solana       | Fuji       | Phantom     | MetaMask  |
      | xlabs-bridge-automatic | 0.01   | SOL   | Solana       | Fantom     | Phantom     | MetaMask  |
      | xlabs-bridge-automatic | 0.04   | AVAX  | Fuji         | Solana     | MetaMask    | Phantom   |
      | xlabs-bridge-automatic | 2      | CELO  | Alfajores    | Solana     | MetaMask    | Phantom   |


    @smoke
    Examples:
      | route                  | amount | asset | from_network | to_network | from_wallet | to_wallet |
      | xlabs-bridge-automatic | 0.03   | SOL   | Solana       | Fuji       | Phantom     | MetaMask  |
      | xlabs-bridge-automatic | 0.04   | AVAX  | Fuji         | Solana     | MetaMask    | Phantom   |


