package servlets;

import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.math.BigInteger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.json.JSONObject;

import client.TonApiClientImpl;
import service.SwapServiceImpl;
import wrappers.WalletImpl;
import model.SwapResponse;


public class SwapServlet extends HttpServlet {

    private final List<String> mnemonic = List.of(
    );

    private final WalletImpl wallet = new WalletImpl(mnemonic, false);
    private final SwapServiceImpl swapServiceImpl = new SwapServiceImpl(wallet, new TonApiClientImpl());

    @Operation(summary = "Совершить swap", description = "Выполняет обмен jetton-токенов")
    @ApiResponse(responseCode = "200", description = "Успешный обмен")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter(); 

        try {
            StringBuilder sb = new StringBuilder();
            
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONObject jsonRequest = new JSONObject(sb.toString());

            String jettonA         = jsonRequest.optString("jettonA", null);
            String jettonB         = jsonRequest.optString("jettonB", null);
            String direction       = jsonRequest.optString("direction", null);
            String route           = jsonRequest.optString("route", null);
            String jettonAmountStr = jsonRequest.optString("jettonAmount", null);

            if (jettonA == null || jettonB == null || route == null || direction == null || jettonAmountStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                SwapResponse resp = new SwapResponse();
                resp.setStatus("error");
                resp.setMessage("Missing parameters: jettonA, jettonB, direction, route, jettonAmount");
                out.println(new JSONObject(resp).toString());
                return;
            }

            if (!(direction.equals("buy") || direction.equals("sell"))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                SwapResponse resp = new SwapResponse();
                resp.setStatus("error");
                resp.setMessage("Invalid direction (must be 'buy' or 'sell')");
                out.println(new JSONObject(resp).toString());
                return;
            }

            if (!(route.equals("native") || route.equals("multi"))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                SwapResponse resp = new SwapResponse();
                resp.setStatus("error");
                resp.setMessage("Invalid route (must be 'native' or 'multi')");
                out.println(new JSONObject(resp).toString());
                return;
            }

            BigInteger jettonAmount   = new BigInteger(jettonAmountStr);
            SwapResponse swapResponse = null;

            if (direction.equals("buy")) {
                swapResponse = swapServiceImpl.desustSwapBuy(jettonA, jettonB, route, jettonAmount);
            } else {
                swapResponse = swapServiceImpl.desustSwapSell(jettonA, jettonB, route, jettonAmount);
            }

            out.println(new JSONObject(swapResponse).toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            SwapResponse error = new SwapResponse();
            error.setStatus("error");
            error.setMessage(e.getMessage());
            out.println(new JSONObject(error).toString());
        }
    }
}