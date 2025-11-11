package servlets;

import java.util.List;

import client.TonApiClientImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.math.BigInteger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.json.JSONObject;

import service.SwapServiceImpl;

import wrappers.WalletImpl;


@WebServlet("/swap/desust")
public class SwapServlet extends HttpServlet {

    private final List<String> mnemonic = List.of(
        "crush", "claim", "fire", "riot", "piano", "dog", "train", "local",
        "update", "wise", "helmet", "caution", "judge", "stove", "census",
        "pride", "tonight", "eternal", "cruel", "chaos", "arrive", "planet",
        "poverty", "museum"
    );

    private final WalletImpl wallet = new WalletImpl(mnemonic, false);
    private final SwapServiceImpl swapServiceImpl = new SwapServiceImpl(wallet, new TonApiClientImpl());

    @Operation(summary = "Совершить swap", description = "Выполняет обмен jetton токенов")
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
            
            String jettonA = jsonRequest.optString("jettonA", null);
            String jettonB = jsonRequest.optString("jettonB", null);
            String swapType = jsonRequest.optString("swapType", null);
            String jettonAmountStr = jsonRequest.optString("jettonAmount", null);

            if (jettonA == null || jettonB == null || swapType == null || jettonAmountStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing parameters\"}");
                return;
            }

            BigInteger jettonAmount = new BigInteger(jettonAmountStr);
            swapServiceImpl.desustSwapBuy(jettonA, jettonB, swapType, jettonAmount);

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "success");
            jsonResponse.put("jettonA", jettonA);
            jsonResponse.put("jettonB", jettonB);
            jsonResponse.put("swapType", swapType);
            jsonResponse.put("jettonAmount", jettonAmount);

            out.println(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", e.getMessage());
            out.println(error.toString());
        }
    }
}