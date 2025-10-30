package client;

import java.net.URI;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.ton.ton4j.address.Address;


public class TonApiClientImpl implements TonApiClient {
    private static final String BASE_URL = "https://tonapi.io/v2/";
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
                """, walletAddress.toString(false)
        );

        String url = BASE_URL + "blockchain/accounts/" + jettonMinterAddress + "/methods/get_wallet_address?decode=true";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            // Отправляем запрос
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) {
                return "ERROR";
            }

            JSONObject obj = new JSONObject(response.body());
            String jettonWalletAddress = obj
                    .getJSONObject("decoded")
                    .getString("jetton_wallet_address");

            return jettonWalletAddress;

        } catch (IOException | InterruptedException ex) {
            System.out.println("\n[ERROR] Network/IO exception:");
            ex.printStackTrace();
            return "ERROR";
        } catch (Exception ex) {
            System.out.println("\n[ERROR] Unexpected exception:");
            ex.printStackTrace();
            return "ERROR";
        }
    }

    public String getVaultAddress(String dedustContractAddress, String jettonHexArg) {
        String url = String.format(
            "%sblockchain/accounts/%s/methods/get_vault_address?args=%s",
            BASE_URL, dedustContractAddress, jettonHexArg
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("accept", "application/json")
            .GET()
            .build();

        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Request failed: " + response.statusCode());
                System.out.println(response.body());
                return "ERROR";
            }

            JSONObject obj = new JSONObject(response.body());

            if (obj.has("decoded")) {
                return obj.getJSONObject("decoded").optString("vault_addr", "UNKNOWN");
            }

            return response.body();
        }
        catch (IOException | InterruptedException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            return "ERROR";
        }
    }
    
    public String getPoolAddress(String dedustContractAddress, String from, String to) {
        int poolType = 0;

        String url = BASE_URL
                + "blockchain/accounts/"
                + dedustContractAddress
                + "/methods/get_pool_address?decode=true"
                + "&args=" + java.net.URLEncoder.encode(String.valueOf(poolType), java.nio.charset.StandardCharsets.UTF_8)
                + "&args=" + java.net.URLEncoder.encode(from,java.nio.charset.StandardCharsets.UTF_8)
                + "&args=" + java.net.URLEncoder.encode(to,java.nio.charset.StandardCharsets.UTF_8);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .GET()
                .build();
        
        try {
            HttpResponse<String> httpResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();

            System.out.println("GET " + url);
            System.out.println("HTTP " + httpResponse.statusCode() + " :: " + response);

            if (httpResponse.statusCode() != 200) {
                return "ERROR_HTTP_" + httpResponse.statusCode();
            }

            org.json.JSONObject obj = new org.json.JSONObject(response);
            String poolAddress = obj.getJSONObject("decoded").getString("pool_address");
            System.out.println("pool_address: " + poolAddress);
            return poolAddress;
        } catch (IOException | InterruptedException ex) {
            System.out.println("ERROR - " + ex.getMessage());
            return "ERROR";
        }
    }

}
