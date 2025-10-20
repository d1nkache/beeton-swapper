package client;

import java.net.URI;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.ton.ton4j.address.Address;


public class TonApiClientImpl implements TonApiClient {
    private static final String BASE_URL = "https://tonapi.io/v2";
    private final HttpClient client = HttpClient.newHttpClient();


    public String getJettonWalletAddress(String jettonMinterAddress, Address walletAddress) {
        String body = String.format("""
                {
                  "args": [
                    {
                      "type": "slice",
                      "value": "%s"
                    }
                  ]
                }
                """, jettonMinterAddress
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(
                BASE_URL + "blockchain/accounts/" + jettonMinterAddress + "/methods/get_vault_address?decode=true")
            )
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        try {
            String response = this.client.send(request, HttpResponse.BodyHandlers.ofString()).toString();
            
            JSONObject obj = new JSONObject(response);
            String jettonWalletAddress = obj
                    .getJSONObject("decoded")
                    .getString("jetton_wallet_address");

            System.out.println("Wallet address: " + jettonWalletAddress);

            return jettonWalletAddress;
        }
        catch(IOException | InterruptedException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            return "ERROR";
        }
    }

    
    public String getVaultAddress(String dedustContractAddress, String jettonMinterAddress) {
        String body = String.format("""
                {
                  "args": [
                    {
                      "type": "slice",
                      "value": "%s"
                    }
                  ]
                }
                """, jettonMinterAddress
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(
                BASE_URL + "blockchain/accounts/" + dedustContractAddress + "/methods/get_vault_address?decode=true")
            )
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        try {
            String response = this.client.send(request, HttpResponse.BodyHandlers.ofString()).toString();
            
            JSONObject obj = new JSONObject(response);
            String vaultAddress = obj
                    .getJSONObject("decoded")
                    .getString("vault_addr");

            System.out.println("Vault address: " + vaultAddress);

            return vaultAddress;
        }
        catch(IOException | InterruptedException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            return "ERROR";
        }
    }


    public String getPoolAddress(String dedustContractAddress, String from, String to) {
        int poolType = 0; // 0 = VOLATILE, 1 = STABLE

        String body = String.format("""
                {
                "args": [
                    {
                    "type": "slice",
                    "value": "%d"
                    },
                    {
                    "type": "slice",
                    "value": "%s"
                    },
                    {
                    "type": "slice",
                    "value": "%s"
                    }
                ]
                }
                """, poolType, from, to
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(
                BASE_URL + "blockchain/accounts/" + dedustContractAddress + "/methods/get_pool_address?decode=true")
            )
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        try {
            String response = this.client.send(request, HttpResponse.BodyHandlers.ofString()).toString();
            
            JSONObject obj = new JSONObject(response);
            String poolAddress = obj
                    .getJSONObject("decoded")
                    .getString("pool_address");

            System.out.println("pool_address: " + poolAddress);

            return poolAddress;
        }
        catch(IOException | InterruptedException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            return "ERROR";
        }
    }
}
